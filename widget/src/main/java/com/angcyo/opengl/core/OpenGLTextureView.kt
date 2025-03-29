package com.angcyo.opengl.core

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.EGL14
import android.opengl.EGLExt
import android.opengl.GLSurfaceView
import android.opengl.GLSurfaceView.EGLConfigChooser
import android.opengl.GLSurfaceView.EGLContextFactory
import android.opengl.GLSurfaceView.EGLWindowSurfaceFactory
import android.opengl.GLSurfaceView.GLWrapper
import android.opengl.GLSurfaceView.RENDERMODE_CONTINUOUSLY
import android.opengl.GLSurfaceView.RENDERMODE_WHEN_DIRTY
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.TextureView
import android.view.TextureView.SurfaceTextureListener
import android.view.View
import com.angcyo.opengl.core.EglHelper.Companion.LOG_ATTACH_DETACH
import com.angcyo.opengl.core.EglHelper.Companion.LOG_THREADS
import com.angcyo.opengl.core.EglHelper.Companion.TAG
import com.angcyo.opengl.gesture.OpenGL2DGesture
import com.angcyo.opengl.gesture.OpenGL2DGestureListener
import com.angcyo.tablayout.viewDrawHeight
import com.angcyo.tablayout.viewDrawWidth
import java.lang.ref.WeakReference
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLContext
import javax.microedition.khronos.egl.EGLDisplay
import javax.microedition.khronos.egl.EGLSurface

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @date 2025/03/24
 */
class OpenGLTextureView(context: Context, attr: AttributeSet?) : TextureView(context, attr),
    IOpenGLSurface {

    //var glRenderer:IOpenGLRenderer

    /**是否激活2D手势识别*/
    var enable2DGesture = false

    /**2D手势识别器*/
    var openGL2DGesture = OpenGL2DGesture().apply {
        listeners.add(object : OpenGL2DGestureListener {
            override fun onTranslateBy(dx: Float, dy: Float) {
                currentScene?.translateSceneBy(dx / viewDrawWidth, dy / viewDrawHeight)
            }

            override fun onScaleBy(sx: Float, sy: Float, px: Float, py: Float) {
                currentScene?.scaleSceneByView(sx, sy, 1f, px, py)
            }
        })
    }

    /**渲染器*/
    val renderer: IOpenGLRenderer?
        get() = rendererDelegate?.renderer

    /**渲染器当前的场景对象*/
    val currentScene: OpenGLScene?
        get() {
            val renderer = renderer
            if (renderer is BaseOpenGLRenderer) {
                return renderer.getCurrentScene()
            }
            return null
        }

    init {
        initialize()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        var handle = super.onTouchEvent(event)
        if (enable2DGesture) {
            handle = handle || openGL2DGesture.onTouch(this, event)
        }
        return handle || enable2DGesture
    }

    //region --GLSurfaceView--

    internal val mThisWeakRef = WeakReference(this)
    internal var mGLThread: OpenGLTextureThread? = null
    internal var mDetached = false
    internal var mEGLConfigChooser: EGLConfigChooser? = null
    internal var mEGLContextFactory: EGLContextFactory? = null
    internal var mEGLWindowSurfaceFactory: EGLWindowSurfaceFactory? = null
    internal var mGLWrapper: GLWrapper? = null
    internal var mDebugFlags = 0
    internal var mEGLContextClientVersion = 0
    internal var mPreserveEGLContextOnPause = false

    private fun initialize() {
        setEGLContextClientVersion(2)

        /*val glesMajorVersion: Int = Capabilities.getGLESMajorVersion()
        setEGLContextClientVersion(glesMajorVersion)*/

        /*setEGLConfigChooser(
            RajawaliEGLConfigChooser(
                glesMajorVersion, mAntiAliasingConfig, mMultiSampleCount,
                mBitsRed, mBitsGreen, mBitsBlue, mBitsAlpha, mBitsDepth
            )
        )*/
        setEGLConfigChooser(SimpleEGLConfigChooser(false))
    }

    fun setEGLContextClientVersion(version: Int) {
        checkRenderThreadState()
        mEGLContextClientVersion = version
    }

    /**
     * Install a custom EGLConfigChooser.
     *
     * If this method is
     * called, it must be called before [.setSurfaceRenderer]
     * is called.
     *
     *
     * If no setEGLConfigChooser method is called, then by default the
     * view will choose an EGLConfig that is compatible with the current
     * android.view.Surface, with a depth buffer depth of
     * at least 16 bits.
     *
     * @param configChooser [GLSurfaceView.EGLConfigChooser] The EGL Configuration chooser.
     */
    fun setEGLConfigChooser(configChooser: EGLConfigChooser) {
        checkRenderThreadState()
        mEGLConfigChooser = configChooser
    }


    /**
     * Install a custom EGLContextFactory.
     *
     * If this method is
     * called, it must be called before [.setSurfaceRenderer]
     * is called.
     *
     *
     * If this method is not called, then by default
     * a context will be created with no shared context and
     * with a null attribute list.
     */
    fun setEGLContextFactory(factory: EGLContextFactory) {
        checkRenderThreadState()
        mEGLContextFactory = factory
    }

    /**
     * Install a custom EGLWindowSurfaceFactory.
     *
     * If this method is
     * called, it must be called before [.setSurfaceRenderer]
     * is called.
     *
     *
     * If this method is not called, then by default
     * a window surface will be created with a null attribute list.
     */
    fun setEGLWindowSurfaceFactory(factory: EGLWindowSurfaceFactory) {
        checkRenderThreadState()
        mEGLWindowSurfaceFactory = factory
    }

    private fun checkRenderThreadState() {
        check(mGLThread == null) { "setRenderer has already been called for this instance." }
    }

    //endregion --GLSurfaceView--

    //region --Renderer--

    internal var mCleanupTexture: SurfaceTexture? = null
    internal var rendererDelegate: TextureRendererDelegate? = null

    internal fun setCleanupTexture(surface: SurfaceTexture) {
        mCleanupTexture = surface
    }

    /**
     * This method is part of the SurfaceTexture.Callback interface, and is
     * not normally called or subclassed by clients of TextureView.
     */
    internal fun surfaceCreated(width: Int, height: Int) {
        mGLThread?.surfaceCreated(width, height)
    }

    /**
     * This method is part of the SurfaceTexture.Callback interface, and is
     * not normally called or subclassed by clients of TextureView.
     */
    internal fun surfaceDestroyed() {
        // Surface will be destroyed when we return
        mGLThread?.surfaceDestroyed()
    }

    /**
     * This method is part of the SurfaceTexture.Callback interface, and is
     * not normally called or subclassed by clients of TextureView.
     */
    internal fun surfaceChanged(w: Int, h: Int) {
        mGLThread?.onWindowResize(w, h)
    }

    internal var mFrameRate: Double = 60.0
    internal var mRenderMode: Int = RENDERMODE_WHEN_DIRTY
    internal var mAntiAliasingConfig: ANTI_ALIASING_CONFIG = ANTI_ALIASING_CONFIG.NONE
    internal var mMultiSampleCount: Int = 0

    override fun setFrameRate(rate: Double) {
        mFrameRate = rate
        if (rendererDelegate != null) {
            rendererDelegate?.renderer?.setFrameRate(rate)
        }
    }

    override fun getRenderMode(): Int {
        return if (rendererDelegate != null) {
            getRenderModeInternal()
        } else {
            mRenderMode
        }
    }

    override fun setRenderMode(mode: Int) {
        mRenderMode = mode
        if (rendererDelegate != null) {
            setRenderModeInternal(mRenderMode)
        }
    }

    override fun setAntiAliasingMode(config: ANTI_ALIASING_CONFIG) {
        mAntiAliasingConfig = config
    }

    override fun setSampleCount(count: Int) {
        mMultiSampleCount = count
    }


    @Throws(IllegalStateException::class)
    override fun setSurfaceRenderer(renderer: IOpenGLRenderer) {
        check(rendererDelegate == null) { "A renderer has already been set for this view." }
        initialize()

        // Configure the EGL stuff
        checkRenderThreadState()
        checkNotNull(mEGLConfigChooser) { "You must set an EGL config before attempting to set a surface renderer." }
        if (mEGLContextFactory == null) {
            mEGLContextFactory = DefaultContextFactory()
        }
        if (mEGLWindowSurfaceFactory == null) {
            mEGLWindowSurfaceFactory = DefaultWindowSurfaceFactory()
        }
        // Create our delegate
        val delegate = TextureRendererDelegate(renderer, this)
        // Create the GL thread
        mGLThread = OpenGLTextureThread(mThisWeakRef)
        mGLThread?.start()
        // Render mode cant be set until the GL thread exists
        setRenderModeInternal(mRenderMode)
        // Register the delegate for callbacks
        rendererDelegate =
            delegate // Done to make sure we dont publish a reference before its safe.
        surfaceTextureListener = rendererDelegate
    }

    override fun requestRenderUpdate() {
        mGLThread?.requestRender()
    }

    //--

    /**
     * Set the rendering mode. When renderMode is
     * RENDERMODE_CONTINUOUSLY, the renderer is called
     * repeatedly to re-render the scene. When renderMode
     * is RENDERMODE_WHEN_DIRTY, the renderer only rendered when the surface
     * is created, or when [.requestRenderUpdate] is called. Defaults to RENDERMODE_CONTINUOUSLY.
     *
     *
     * Using RENDERMODE_WHEN_DIRTY can improve battery life and overall system performance
     * by allowing the GPU and CPU to idle when the view does not need to be updated.
     *
     *
     * This method can only be called after [.setSurfaceRenderer]
     *
     * @param renderMode one of the RENDERMODE_X constants
     *
     * @see .RENDERMODE_CONTINUOUSLY
     *
     * @see .RENDERMODE_WHEN_DIRTY
     */
    private fun setRenderModeInternal(renderMode: Int) {
        mGLThread?.setRenderMode(renderMode)
    }

    /**
     * Get the current rendering mode. May be called
     * from any thread. Must not be called before a renderer has been set.
     *
     * @return the current rendering mode.
     * @see .RENDERMODE_CONTINUOUSLY
     *
     * @see .RENDERMODE_WHEN_DIRTY
     */
    private fun getRenderModeInternal(): Int {
        return mGLThread?.getRenderMode() ?: 0
    }

    //endregion --Renderer--

    //region --lifecycle--

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        if (visibility == GONE || visibility == INVISIBLE) {
            onPause()
        } else {
            onResume()
        }
        super.onVisibilityChanged(changedView!!, visibility)
    }

    /**
     * Inform the view that the activity is paused. The owner of this view must
     * call this method when the activity is paused. Calling this method will
     * pause the rendering thread.
     * Must not be called before a renderer has been set.
     */
    fun onPause() {
        rendererDelegate?.renderer?.onPause()
        mGLThread?.onPause()
    }

    /**
     * Inform the view that the activity is resumed. The owner of this view must
     * call this method when the activity is resumed. Calling this method will
     * recreate the OpenGL display and resume the rendering
     * thread.
     * Must not be called before a renderer has been set.
     */
    fun onResume() {
        check(rendererDelegate != null) { "请先设置渲染器[setSurfaceRenderer]" }
        rendererDelegate?.renderer?.onResume()
        mGLThread?.onResume()
    }

    /**
     * This method is used as part of the View class and is not normally
     * called or subclassed by clients of TextureView.
     */
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (LOG_ATTACH_DETACH) {
            Log.d(TAG, "onAttachedToWindow reattach =$mDetached")
        }
        if (mDetached && (rendererDelegate != null)) {
            var renderMode: Int = RENDERMODE_CONTINUOUSLY
            if (mGLThread != null) {
                renderMode = mGLThread?.getRenderMode() ?: 0
            }
            mGLThread = OpenGLTextureThread(mThisWeakRef)
            if (renderMode != RENDERMODE_CONTINUOUSLY) {
                mGLThread?.setRenderMode(renderMode)
            }
            mGLThread?.start()
        }
        mDetached = false
    }

    override fun onDetachedFromWindow() {
        if (LOG_ATTACH_DETACH) {
            Log.d(TAG, "onDetachedFromWindow")
        }
        if (mGLThread != null) {
            mGLThread?.requestExitAndWait()
        }
        mDetached = true
        rendererDelegate?.renderer?.onRenderSurfaceDestroyed(null)
        super.onDetachedFromWindow()
    }


    @Throws(Throwable::class)
    fun finalize() {
        try {
            if (mGLThread != null) {
                // GLThread may still be running if this view was never
                // attached to a window.
                mGLThread?.requestExitAndWait()
            }
        } finally {
            if (mCleanupTexture != null) mCleanupTexture!!.release()
            mCleanupTexture = null
            //super.finalize()
        }
    }

    //endregion --lifecycle--

    inner class DefaultContextFactory : EGLContextFactory {
        internal val EGL_CONTEXT_CLIENT_VERSION = 0x3098

        override fun createContext(egl: EGL10, display: EGLDisplay, config: EGLConfig): EGLContext {
            val attrib_list = intArrayOf(
                EGL_CONTEXT_CLIENT_VERSION, mEGLContextClientVersion,
                EGL10.EGL_NONE
            )

            return egl.eglCreateContext(
                display, config, EGL10.EGL_NO_CONTEXT,
                if (mEGLContextClientVersion != 0) attrib_list else null
            )
        }

        override fun destroyContext(egl: EGL10, display: EGLDisplay, context: EGLContext) {
            if (!egl.eglDestroyContext(display, context)) {
                Log.e("DefaultContextFactory", "display:$display context: $context")
                if (LOG_THREADS) {
                    Log.i("DefaultContextFactory", "tid=" + Thread.currentThread().id)
                }
                EglHelper.throwEglException("eglDestroyContex", egl.eglGetError())
            }
        }
    }


    abstract inner class BaseConfigChooser
        (configSpec: IntArray) : EGLConfigChooser {
        override fun chooseConfig(egl: EGL10, display: EGLDisplay?): EGLConfig? {
            val num_config = IntArray(1)
            require(
                egl.eglChooseConfig(
                    display, mConfigSpec, null, 0,
                    num_config
                )
            ) { "eglChooseConfig failed" }

            val numConfigs = num_config[0]

            require(numConfigs > 0) { "No configs match configSpec" }

            val configs = arrayOfNulls<EGLConfig>(numConfigs)
            require(
                egl.eglChooseConfig(
                    display, mConfigSpec, configs, numConfigs,
                    num_config
                )
            ) { "eglChooseConfig#2 failed" }
            val config = chooseConfig(egl, display, configs)
            requireNotNull(config) { "No config chosen" }
            return config
        }

        abstract fun chooseConfig(
            egl: EGL10,
            display: EGLDisplay?,
            configs: Array<EGLConfig?>
        ): EGLConfig?

        protected var mConfigSpec: IntArray

        init {
            mConfigSpec = filterConfigSpec(configSpec)
        }

        fun filterConfigSpec(configSpec: IntArray): IntArray {
            if (mEGLContextClientVersion != 2 && mEGLContextClientVersion != 3) {
                return configSpec
            }
            /* We know none of the subclasses define EGL_RENDERABLE_TYPE.
                 * And we know the configSpec is well formed.
                 */
            val len = configSpec.size
            val newConfigSpec = IntArray(len + 2)
            System.arraycopy(configSpec, 0, newConfigSpec, 0, len - 1)
            newConfigSpec[len - 1] = EGL10.EGL_RENDERABLE_TYPE
            if (mEGLContextClientVersion == 2) {
                newConfigSpec[len] = EGL14.EGL_OPENGL_ES2_BIT /* EGL_OPENGL_ES2_BIT */
            } else {
                newConfigSpec[len] = EGLExt.EGL_OPENGL_ES3_BIT_KHR /* EGL_OPENGL_ES3_BIT_KHR */
            }
            newConfigSpec[len + 1] = EGL10.EGL_NONE
            return newConfigSpec
        }
    }

    /**
     * Choose a configuration with exactly the specified r,g,b,a sizes,
     * and at least the specified depth and stencil sizes.
     */
    open inner class ComponentSizeChooser(
        redSize: Int, greenSize: Int, blueSize: Int,
        alphaSize: Int, depthSize: Int, stencilSize: Int
    ) :
        BaseConfigChooser(
            intArrayOf(
                EGL10.EGL_RED_SIZE, redSize,
                EGL10.EGL_GREEN_SIZE, greenSize,
                EGL10.EGL_BLUE_SIZE, blueSize,
                EGL10.EGL_ALPHA_SIZE, alphaSize,
                EGL10.EGL_DEPTH_SIZE, depthSize,
                EGL10.EGL_STENCIL_SIZE, stencilSize,
                EGL10.EGL_NONE
            )
        ) {
        override fun chooseConfig(
            egl: EGL10,
            display: EGLDisplay?,
            configs: Array<EGLConfig?>
        ): EGLConfig? {
            for (config in configs) {
                val d = findConfigAttrib(
                    egl, display, config,
                    EGL10.EGL_DEPTH_SIZE, 0
                )
                val s = findConfigAttrib(
                    egl, display, config,
                    EGL10.EGL_STENCIL_SIZE, 0
                )
                if ((d >= mDepthSize) && (s >= mStencilSize)) {
                    val r = findConfigAttrib(
                        egl, display, config,
                        EGL10.EGL_RED_SIZE, 0
                    )
                    val g = findConfigAttrib(
                        egl, display, config,
                        EGL10.EGL_GREEN_SIZE, 0
                    )
                    val b = findConfigAttrib(
                        egl, display, config,
                        EGL10.EGL_BLUE_SIZE, 0
                    )
                    val a = findConfigAttrib(
                        egl, display, config,
                        EGL10.EGL_ALPHA_SIZE, 0
                    )
                    if ((r == mRedSize) && (g == mGreenSize)
                        && (b == mBlueSize) && (a == mAlphaSize)
                    ) {
                        return config
                    }
                }
            }
            return null
        }

        fun findConfigAttrib(
            egl: EGL10, display: EGLDisplay?,
            config: EGLConfig?, attribute: Int, defaultValue: Int
        ): Int {
            if (egl.eglGetConfigAttrib(display, config, attribute, mValue)) {
                return mValue[0]
            }
            return defaultValue
        }

        private val mValue = IntArray(1)

        // Subclasses can adjust these values:
        protected var mRedSize: Int = redSize
        protected var mGreenSize: Int = greenSize
        protected var mBlueSize: Int = blueSize
        protected var mAlphaSize: Int = alphaSize
        protected var mDepthSize: Int = depthSize
        protected var mStencilSize: Int = stencilSize
    }

    /**
     * This class will choose a RGB_888 surface with
     * or without a depth buffer.
     *
     */
    inner class SimpleEGLConfigChooser(withDepthBuffer: Boolean) :
        ComponentSizeChooser(8, 8, 8, 0, if (withDepthBuffer) 16 else 0, 0)
}

internal class TextureRendererDelegate(
    val renderer: IOpenGLRenderer,
    val textureView: OpenGLTextureView
) :
    SurfaceTextureListener {

    init {
        renderer.setFrameRate(if (textureView.mRenderMode == RENDERMODE_WHEN_DIRTY) textureView.mFrameRate else 0.0)
        renderer.setAntiAliasingMode(textureView.mAntiAliasingConfig)
        renderer.setRenderSurface(textureView)
        textureView.surfaceTextureListener = this
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        textureView.surfaceCreated(width, height)
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
        textureView.surfaceChanged(width, height)
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        textureView.setCleanupTexture(surface)
        textureView.surfaceDestroyed()
        return false
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
        // Do nothing
    }
}


internal class DefaultWindowSurfaceFactory : EGLWindowSurfaceFactory {
    override fun createWindowSurface(
        egl: EGL10, display: EGLDisplay,
        config: EGLConfig, nativeWindow: Any
    ): EGLSurface {
        var result: EGLSurface? = null
        try {
            result = egl.eglCreateWindowSurface(display, config, nativeWindow, null)
        } catch (e: IllegalArgumentException) {
            // This exception indicates that the surface flinger surface
            // is not valid. This can happen if the surface flinger surface has
            // been torn down, but the application has not yet been
            // notified via SurfaceTexture.Callback.surfaceDestroyed.
            // In theory the application should be notified first,
            // but in practice sometimes it is not. See b/4588890
            Log.e(TAG, "eglCreateWindowSurface", e)
        }
        return result!!
    }

    override fun destroySurface(
        egl: EGL10, display: EGLDisplay,
        surface: EGLSurface
    ) {
        egl.eglDestroySurface(display, surface)
    }
}