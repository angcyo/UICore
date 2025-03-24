package com.angcyo.opengl

import android.content.Context
import android.opengl.GLSurfaceView
import android.opengl.GLSurfaceView.RENDERMODE_WHEN_DIRTY
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @date 2025/03/17
 *
 * https://developer.android.com/develop/ui/views/graphics/opengl/about-opengl?hl=zh-cn
 *
 * https://developer.android.com/develop/ui/views/graphics/opengl/environment?hl=zh-cn
 */
class OpenGLSurfaceView(context: Context, attr: AttributeSet?) : GLSurfaceView(context, attr),
    IOpenGLSurface {

    /**渲染器*/
    //var glRenderer = OpenGLRenderer()

    protected var mIsTransparent: Boolean = false

    init {
        /*setEGLContextFactory(OpenGLContextFactory())
        //setEGLContextClientVersion(glVersion.toInt())
        setRenderer(glRenderer)

        //renderMode = RENDERMODE_WHEN_DIRTY
        //Android Extension Pack (AEP)
        //https://developer.android.com/develop/ui/views/graphics/opengl/about-opengl?hl=zh-cn#aep
        val packageManager = context.packageManager
        val deviceSupportsAEP: Boolean =
            packageManager.hasSystemFeature(PackageManager.FEATURE_OPENGLES_EXTENSION_PACK)
        L.i("deviceSupportsAEP: $deviceSupportsAEP")*/
    }

    private fun initialize() {
        setEGLContextClientVersion(2)
        /*val glesMajorVersion: Int = Capabilities.getGLESMajorVersion()
        setEGLContextClientVersion(glesMajorVersion)

        if (mIsTransparent) {
            setEGLConfigChooser(
                RajawaliEGLConfigChooser(
                    glesMajorVersion, mAntiAliasingConfig, mMultiSampleCount,
                    8, 8, 8, 8, mBitsDepth
                )
            )

            holder.setFormat(PixelFormat.TRANSLUCENT)
            setZOrderOnTop(true)
        } else {
            setEGLConfigChooser(
                RajawaliEGLConfigChooser(
                    glesMajorVersion, mAntiAliasingConfig, mMultiSampleCount,
                    mBitsRed, mBitsGreen, mBitsBlue, mBitsAlpha, mBitsDepth
                )
            )

            holder.setFormat(PixelFormat.RGBA_8888)
            setZOrderOnTop(false)
        }*/
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        super.onTouchEvent(event)
        when (event?.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                //glRenderer.backgroundColor = randomColor()
            }
        }
        return true
    }

    //region --surface--

    internal var mFrameRate: Double = 60.0
    internal var mRenderMode: Int = RENDERMODE_WHEN_DIRTY
    internal var mAntiAliasingConfig: ANTI_ALIASING_CONFIG = ANTI_ALIASING_CONFIG.NONE
    internal var mMultiSampleCount: Int = 0
    internal var mRendererDelegate: SurfaceRendererDelegate? = null

    override fun setFrameRate(rate: Double) {
        mFrameRate = rate
        if (mRendererDelegate != null) {
            mRendererDelegate!!.mRenderer.setFrameRate(rate)
        }
    }

    override fun getRenderMode(): Int {
        return if (mRendererDelegate != null) {
            super.getRenderMode()
        } else {
            mRenderMode
        }
    }

    override fun setRenderMode(mode: Int) {
        mRenderMode = mode
        if (mRendererDelegate != null) {
            super.setRenderMode(mRenderMode)
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
        check(mRendererDelegate == null) { "A renderer has already been set for this view." }
        initialize()
        val delegate = SurfaceRendererDelegate(renderer, this)
        super.setRenderer(delegate)
        mRendererDelegate =
            delegate // Done to make sure we dont publish a reference before its safe.
        // Render mode cant be set until the GL thread exists
        renderMode = mRenderMode
        onPause() // We want to halt the surface view until we are ready
    }

    override fun requestRenderUpdate() {
        requestRender()
    }

    //endregion --surface--

    //region --lifecycle--

    override fun onPause() {
        super.onPause()
        if (mRendererDelegate != null) mRendererDelegate?.mRenderer?.onPause()
    }

    override fun onResume() {
        check(mRendererDelegate != null) { "请先设置渲染器[setSurfaceRenderer]" }
        super.onResume()
        if (mRendererDelegate != null) mRendererDelegate?.mRenderer?.onResume()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        if (visibility == GONE || visibility == INVISIBLE) {
            onPause()
        } else {
            onResume()
        }
        super.onVisibilityChanged(changedView, visibility)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!isInEditMode) {
            onResume()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mRendererDelegate?.mRenderer?.onRenderSurfaceDestroyed(null)
    }

    //endregion --lifecycle--
}

/**
 * Delegate used to translate between [GLSurfaceView.Renderer] and [ISurfaceRenderer].
 *
 * @author Jared Woolston (jwoolston@tenkiv.com)
 */
internal class SurfaceRendererDelegate(
    val mRenderer: IOpenGLRenderer,
    val surfaceView: OpenGLSurfaceView
) : GLSurfaceView.Renderer {

    init {
        mRenderer.setFrameRate(if (surfaceView.mRenderMode == RENDERMODE_WHEN_DIRTY) surfaceView.mFrameRate else 0.0)
        mRenderer.setAntiAliasingMode(surfaceView.mAntiAliasingConfig)
        mRenderer.setRenderSurface(surfaceView)
    }

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        mRenderer.onRenderSurfaceCreated(config, gl, -1, -1)
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        mRenderer.onRenderSurfaceSizeChanged(gl, width, height)
    }

    override fun onDrawFrame(gl: GL10) {
        mRenderer.onRenderFrame(gl)
    }
}

