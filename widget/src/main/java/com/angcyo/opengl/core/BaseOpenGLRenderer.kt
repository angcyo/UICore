package com.angcyo.opengl.core

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.view.WindowManager
import com.angcyo.library.L
import com.angcyo.library.annotation.ThreadDes
import java.util.Collections
import java.util.LinkedList
import java.util.Locale
import java.util.Queue
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLContext
import javax.microedition.khronos.opengles.GL10

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @date 2025/03/24
 *
 * 在[GLThread]线程中调度
 * [OpenGLTextureThread]
 *
 */
abstract class BaseOpenGLRenderer(val context: Context) : IOpenGLRenderer {

    companion object {
        var supportsUIntBuffers: Boolean = false

        /**
         * Indicates whether the OpenGL context is still alive or not.
         *
         * @return `boolean` True if the OpenGL context is still alive.
         */
        fun hasGLContext(): Boolean {
            val egl = EGLContext.getEGL() as EGL10
            val eglContext = egl.eglGetCurrentContext()
            return eglContext !== EGL10.EGL_NO_CONTEXT
        }
    }

    // Listener to notify of new FPS values.
    var mFPSUpdateListener: OnFPSUpdateListener? = null

    protected var mFrameRate: Double = 0.0 // Target frame rate to render at
    protected var mScenes: MutableList<OpenGLScene>
    /*protected val mRenderTargets: List<RenderTarget>? =
        null //List of all render targets this renderer is aware of.
    private val mFrameTaskQueue: Queue<AFrameTask>? = null
    private var mAntiAliasingConfig: ANTI_ALIASING_CONFIG? = null*/


    protected var mSceneInitialized: Boolean = false //This applies to all scenes
    private var mCurrentScene: OpenGLScene
    private var mAntiAliasingConfig: ANTI_ALIASING_CONFIG? = null

    private val mHaveRegisteredForResources = false

    /**
     * Scene caching stores all textures and relevant OpenGL-specific
     * data. This is used when the OpenGL context needs to be restored.
     * The context typically needs to be restored when the application
     * is re-activated or when a live wallpaper is rotated.
     */
    var mSceneCachingEnabled = true //This applies to all scenes

    init {
        mFrameRate = getRefreshRate()

        mScenes = Collections.synchronizedList(CopyOnWriteArrayList())
        /*mRenderTargets =
            Collections.synchronizedList<RenderTarget>(CopyOnWriteArrayList<RenderTarget>())
        mFrameTaskQueue = LinkedList<AFrameTask>()*/

        val defaultScene: OpenGLScene = getNewDefaultScene()
        mScenes.add(defaultScene)
        mCurrentScene = defaultScene

        clearOverrideViewportDimensions()
    }

    //--

    override fun setRenderSurface(surface: IOpenGLSurface?) {
        mSurface = surface
    }

    //In case we cannot parse the version number, assume OpenGL ES 2.0
    protected var mGLES_Major_Version: Int = 2 // The GL ES major version of the surface
    protected var mGLES_Minor_Version: Int = 0 // The GL ES minor version of the surface

    override fun onRenderSurfaceCreated(config: EGLConfig?, gl: GL10?, width: Int, height: Int) {
        //Capabilities.getInstance()

        //[ "OpenGL", "ES", "3.2", "v1.r51p0-00eac0..." ]
        val versionString = (GLES20.glGetString(GLES20.GL_VERSION)).split(" ".toRegex())
            .dropLastWhile { it.isEmpty() }.toTypedArray()
        L.d("Open GL ES Version String: " + GLES20.glGetString(GLES20.GL_VERSION))
        if (versionString.size >= 3) {
            val versionParts =
                versionString[2].split("\\.".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
            if (versionParts.size >= 2) {
                mGLES_Major_Version = versionParts[0].toInt()
                versionParts[1] = versionParts[1].replace("([^0-9].+)".toRegex(), "")
                mGLES_Minor_Version = versionParts[1].toInt()
            }
        }
        L.d(
            String.format(
                Locale.US,
                "Derived GL ES Version: %d.%d",
                mGLES_Major_Version,
                mGLES_Minor_Version
            )
        )//Derived GL ES Version: 3.2

        supportsUIntBuffers =
            GLES20.glGetString(GLES20.GL_EXTENSIONS).contains("GL_OES_element_index_uint")

        /*if (!mHaveRegisteredForResources) {
            mTextureManager.registerRenderer(this)
            mMaterialManager.registerRenderer(this)
        }*/

        //gl?.glShadeModel(GL10.GL_FLAT)//GL10.GL_FLAT //GL10.GL_SMOOTH
    }

    override fun onRenderSurfaceSizeChanged(gl: GL10?, width: Int, height: Int) {
        mDefaultViewportWidth = width
        mDefaultViewportHeight = height

        val wViewport =
            if (mOverrideViewportWidth > -1) mOverrideViewportWidth else mDefaultViewportWidth
        val hViewport =
            if (mOverrideViewportHeight > -1) mOverrideViewportHeight else mDefaultViewportHeight
        setViewPort(wViewport, hViewport)

        if (!mSceneInitialized) {
            getCurrentScene().resetGLState()
            initScene()
            getCurrentScene().initScene()
        }

        if (!mSceneCachingEnabled) {
            /*mTextureManager.reset()
            mMaterialManager.reset()
            clearScenes()*/
        } else if (mSceneCachingEnabled && mSceneInitialized) {
            /*var i = 0
            val j: Int = mRenderTargets.size
            while (i < j) {
                if (mRenderTargets.get(i).getFullscreen()) {
                    mRenderTargets.get(i).setWidth(mDefaultViewportWidth)
                    mRenderTargets.get(i).setHeight(mDefaultViewportHeight)
                }
                ++i
            }
            mTextureManager.taskReload()
            mMaterialManager.taskReload()
            reloadScenes()
            reloadRenderTargets()*/
            reloadScenes()
        }
        mSceneInitialized = true
        startRendering()
    }

    override fun onRenderSurfaceDestroyed(surface: SurfaceTexture?) {
        stopRendering()
        synchronized(mScenes) {
            /*if (mTextureManager != null) {
                mTextureManager.unregisterRenderer(this)
                mTextureManager.taskReset(this)
            }
            if (mMaterialManager != null) {
                mMaterialManager.taskReset(this)
                mMaterialManager.unregisterRenderer(this)
            }*/
            var i = 0
            val j = mScenes.size
            while (i < j) {
                mScenes[i].destroyScene()
                ++i
            }
        }
    }

    private val mNextSceneLock = Object() //Scene switching lock

    protected var mFrameCount: Int = 0 // Used for determining FPS
    private var mStartTime = System.nanoTime() // Used for determining FPS
    protected var mLastMeasuredFPS: Double = 0.0 // Last measured FPS value

    override fun onRenderFrame(gl: GL10?) {
        performFrameTasks() //Execute any pending frame tasks
        /*synchronized(mNextSceneLock) {
            //Check if we need to switch the scene, and if so, do it.
            if (mNextScene != null) {
                switchSceneDirect(mNextScene!!)
                mNextScene = null
            }
        }*/

        val currentTime = System.nanoTime()
        val elapsedRenderTime = currentTime - mRenderStartTime
        val deltaTime = (currentTime - mLastRender) / 1e9
        mLastRender = currentTime

        onRender(elapsedRenderTime, deltaTime)

        ++mFrameCount
        if (mFrameCount % 50 == 0) {
            val now = System.nanoTime()
            val elapsedS: Double = (now - mStartTime) / 1.0e9
            val msPerFrame: Double = (1000 * elapsedS / mFrameCount)
            mLastMeasuredFPS = 1000 / msPerFrame

            mFrameCount = 0
            mStartTime = now

            if (mFPSUpdateListener != null) mFPSUpdateListener?.onFPSUpdate(mLastMeasuredFPS) //Update the FPS listener
        }
    }

    override fun onPause() {
        stopRendering()
    }

    override fun onResume() {
        if (mSceneInitialized) {
            getCurrentScene().resetGLState()
            startRendering()
        }
    }

    override fun getFrameRate(): Double {
        return mFrameRate
    }

    override fun setFrameRate(rate: Int) {
        setFrameRate(rate.toDouble())
    }

    override fun setFrameRate(rate: Double) {
        mFrameRate = rate
        if (stopRendering()) {
            // Restart timer with new frequency
            startRendering()
        }
    }

    override fun setAntiAliasingMode(config: ANTI_ALIASING_CONFIG) {
        mAntiAliasingConfig = config
        synchronized(mScenes) {
            var i = 0
            val j = mScenes.size
            while (i < j) {
                mScenes[i].setAntiAliasingConfig(config)
                ++i
            }
        }
    }

    //region --core--

    protected var mTimer: ScheduledExecutorService? = null // Timer used to schedule drawing
    private var mRenderStartTime: Long = 0
    private var mLastRender: Long = 0 // Time of last rendering. Used for animation delta time
    protected var mSurface: IOpenGLSurface? = null // The rendering surface

    /**
     * Fetches the [OpenGLScene] currently being being displayed.
     * Note that the scene is not thread safe so this should be used
     * with extreme caution.
     *
     * @return [OpenGLScene] object currently used for the scene.
     * @see {@link Renderer.mCurrentScene}
     */
    fun getCurrentScene(): OpenGLScene {
        return mCurrentScene
    }

    /**
     * Called to reload the scenes.
     */
    protected fun reloadScenes() {
        synchronized(mScenes) {
            var i = 0
            val j = mScenes.size
            while (i < j) {
                mScenes[i].reload()
                ++i
            }
        }
    }

    /**
     * Stop rendering the scene.
     *
     * @return true if rendering was stopped, false if rendering was already
     * stopped (no action taken)
     */
    fun stopRendering(): Boolean {
        if (mTimer != null) {
            mTimer?.shutdownNow()
            mTimer = null
            return true
        }
        return false
    }

    fun startRendering() {
        L.d("startRendering()")
        if (!mSceneInitialized) {
            return
        }
        mRenderStartTime = System.nanoTime()
        mLastRender = mRenderStartTime
        if (mTimer != null) return
        mTimer = Executors.newScheduledThreadPool(1)
        mTimer?.scheduleAtFixedRate(
            RequestRenderTask(),
            0,
            (1000 / mFrameRate).toLong(),
            TimeUnit.MILLISECONDS
        )
    }

    private val mFrameTaskQueue: Queue<OpenGLFrameTask> = LinkedList()
    protected fun performFrameTasks() {
        synchronized(mFrameTaskQueue) {
            //Fetch the first task
            var task: OpenGLFrameTask? = mFrameTaskQueue.poll()
            while (task != null) {
                task.run()
                //Retrieve the next task
                task = mFrameTaskQueue.poll()
            }
        }
    }

    internal fun internalOfferTask(task: OpenGLFrameTask): Boolean {
        synchronized(mFrameTaskQueue) {
            return mFrameTaskQueue.offer(task)
        }
    }

    protected var mCurrentViewportWidth: Int = 0
    protected var mCurrentViewportHeight: Int = 0 // The current width and height of the GL viewport
    protected var mDefaultViewportWidth: Int = 0
    protected var mDefaultViewportHeight: Int = 0 // The default width and height of the GL viewport
    protected var mOverrideViewportWidth: Int = 0
    protected var mOverrideViewportHeight: Int =
        0 // The overridden width and height of the GL viewport

    /**
     * Sets the GL Viewport used. User code is free to override this method, so long as the viewport
     * is set somewhere (and the projection matrix updated).
     *
     * @param width `int` The viewport width in pixels.
     * @param height `int` The viewport height in pixels.
     */
    fun setViewPort(width: Int, height: Int) {
        mCurrentViewportWidth = width
        mCurrentViewportHeight = height
        mCurrentScene.updateProjectionMatrix(width, height)
        GLES20.glViewport(0, 0, width, height)
    }

    fun getDefaultViewportWidth(): Int {
        return mDefaultViewportWidth
    }

    fun getDefaultViewportHeight(): Int {
        return mDefaultViewportHeight
    }

    fun clearOverrideViewportDimensions() {
        mOverrideViewportWidth = -1
        mOverrideViewportHeight = -1
        setViewPort(mDefaultViewportWidth, mDefaultViewportHeight)
    }

    fun setOverrideViewportDimensions(width: Int, height: Int) {
        mOverrideViewportWidth = width
        mOverrideViewportHeight = height
    }

    fun getOverrideViewportWidth(): Int {
        return mOverrideViewportWidth
    }

    fun getOverrideViewportHeight(): Int {
        return mOverrideViewportHeight
    }

    fun getViewportWidth(): Int {
        return mCurrentViewportWidth
    }

    fun getViewportHeight(): Int {
        return mCurrentViewportHeight
    }

    internal inner class RequestRenderTask : Runnable {
        override fun run() {
            mSurface?.requestRenderUpdate()
        }
    }

    //endregion --core--

    //region --override--

    /**
     * Scene construction should happen here, not in onSurfaceCreated()
     */
    @ThreadDes("GLThread")
    protected abstract fun initScene()

    /**
     * Return a new instance of the default initial scene for the [Renderer] instance. This method is only
     * intended to be called one time by the renderer itself and should not be used elsewhere.
     *
     * @return [OpenGLScene] The default scene.
     */
    protected fun getNewDefaultScene(): OpenGLScene {
        return OpenGLScene(this)
    }

    /**
     * Called by [.onRenderFrame] to render the next frame. This is
     * called prior to the current scene's [RajawaliScene.render] method.
     *
     * @param ellapsedRealtime `long` The total ellapsed rendering time in milliseconds.
     * @param deltaTime        `double` The time passes since the last frame, in seconds.
     *
     * [onRenderFrame]
     */
    protected fun onRender(ellapsedRealtime: Long, deltaTime: Double) {
        render(ellapsedRealtime, deltaTime)
    }

    /**
     * Called by [.onRender] to render the next frame.
     *
     * @param ellapsedRealtime `long` Render ellapsed time in milliseconds.
     * @param deltaTime        `double` Time passed since last frame, in seconds.
     */
    protected fun render(ellapsedRealtime: Long, deltaTime: Double) {
        mCurrentScene.render(ellapsedRealtime, deltaTime)
    }

    //endregion --override--

    //region --other--

    fun getRefreshRate(): Double {
        return (context
            .getSystemService(Context.WINDOW_SERVICE) as WindowManager)
            .defaultDisplay
            .refreshRate.toDouble()
    }

    //endregion --other--
}

interface OnFPSUpdateListener {
    fun onFPSUpdate(fps: Double)
}

internal abstract class OpenGLFrameTask : Runnable {
    protected abstract fun doTask()

    override fun run() {
        try {
            doTask()
        } catch (e: Exception) {
            L.e("Execution Failed: " + e.message)
        }
    }
}