package com.angcyo.opengl.core

import android.opengl.GLDebugHelper
import android.opengl.GLSurfaceView
import android.util.Log
import com.angcyo.opengl.core.EglHelper.Companion.LOG_PAUSE_RESUME
import com.angcyo.opengl.core.EglHelper.Companion.LOG_RENDERER
import com.angcyo.opengl.core.EglHelper.Companion.LOG_RENDERER_DRAW_FRAME
import com.angcyo.opengl.core.EglHelper.Companion.LOG_SURFACE
import com.angcyo.opengl.core.EglHelper.Companion.LOG_THREADS
import com.angcyo.opengl.core.EglHelper.Companion.TAG
import java.io.Writer
import java.lang.Thread.currentThread
import java.lang.ref.WeakReference
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGL11
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLContext
import javax.microedition.khronos.egl.EGLDisplay
import javax.microedition.khronos.egl.EGLSurface
import javax.microedition.khronos.opengles.GL
import javax.microedition.khronos.opengles.GL10

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @date 2025/03/24
 *
 * [android.opengl.GLSurfaceView.GLThread]
 */
internal class OpenGLTextureThread : Thread {

    companion object {
        internal val sGLThreadManager: OpenGLThreadManager = OpenGLThreadManager()
    }

    constructor(glSurfaceViewWeakRef: WeakReference<OpenGLTextureView>) : super() {
        mWidth = 0
        mHeight = 0
        mRequestRender = true
        mRenderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        mWantRenderNotification = false
        mGLSurfaceViewWeakRef = glSurfaceViewWeakRef
    }

    override fun run() {
        name = "GLThread $id"
        if (LOG_THREADS) {
            Log.i("GLThread", "starting tid=$id")
        }

        try {
            guardedRun()
        } catch (e: InterruptedException) {
            // fall thru and exit normally
        } finally {
            sGLThreadManager.threadExiting(this)
        }
    }

    /**
     * This private method should only be called inside a
     * synchronized(sGLThreadManager) block.
     */
    private fun stopEglSurfaceLocked() {
        if (mHaveEglSurface) {
            mHaveEglSurface = false
            mEglHelper?.destroySurface()
        }
    }

    /**
     * This private method should only be called inside a
     * synchronized(sGLThreadManager) block.
     */
    private fun stopEglContextLocked() {
        if (mHaveEglContext) {
            mEglHelper?.finish()
            mHaveEglContext = false
            sGLThreadManager.releaseEglContextLocked(this)
        }
    }

    @Throws(InterruptedException::class)
    private fun guardedRun() {
        mEglHelper = EglHelper(mGLSurfaceViewWeakRef)
        mHaveEglContext = false
        mHaveEglSurface = false
        mWantRenderNotification = false

        try {
            var gl: GL10? = null
            var createEglContext = false
            var createEglSurface = false
            var createGlInterface = false
            var lostEglContext = false
            var sizeChanged = false
            var wantRenderNotification = false
            var doRenderNotification = false
            var askedToReleaseEglContext = false
            var w = 0
            var h = 0
            var event: Runnable? = null
            var finishDrawingRunnable: Runnable? = null

            while (true) {
                synchronized(sGLThreadManager) {
                    while (true) {
                        if (mShouldExit) {
                            return
                        }

                        if (mEventQueue.isNotEmpty()) {
                            event = mEventQueue.removeAt(0)
                            break
                        }

                        // Update the pause state.
                        var pausing = false
                        if (mPaused != mRequestPaused) {
                            pausing = mRequestPaused
                            mPaused = mRequestPaused
                            (sGLThreadManager as Object).notifyAll()
                            if (LOG_PAUSE_RESUME) {
                                Log.i("GLThread", "mPaused is now $mPaused tid=$id")
                            }
                        }

                        // Do we need to give up the EGL context?
                        if (mShouldReleaseEglContext) {
                            if (LOG_SURFACE) {
                                Log.i(
                                    "GLThread",
                                    "releasing EGL context because asked to tid=$id"
                                )
                            }
                            stopEglSurfaceLocked()
                            stopEglContextLocked()
                            mShouldReleaseEglContext = false
                            askedToReleaseEglContext = true
                        }

                        // Have we lost the EGL context?
                        if (lostEglContext) {
                            stopEglSurfaceLocked()
                            stopEglContextLocked()
                            lostEglContext = false
                        }

                        // When pausing, release the EGL surface:
                        if (pausing && mHaveEglSurface) {
                            if (LOG_SURFACE) {
                                Log.i(
                                    "GLThread",
                                    "releasing EGL surface because paused tid=$id"
                                )
                            }
                            stopEglSurfaceLocked()
                        }

                        // When pausing, optionally release the EGL Context:
                        if (pausing && mHaveEglContext) {
                            val view = mGLSurfaceViewWeakRef!!.get()
                            val preserveEglContextOnPause =
                                view?.mPreserveEGLContextOnPause ?: false
                            if (!preserveEglContextOnPause) {
                                stopEglContextLocked()
                                if (LOG_SURFACE) {
                                    Log.i(
                                        "GLThread",
                                        "releasing EGL context because paused tid=$id"
                                    )
                                }
                            }
                        }

                        // Have we lost the SurfaceView surface?
                        if ((!mHasSurface) && (!mWaitingForSurface)) {
                            if (LOG_SURFACE) {
                                Log.i("GLThread", "noticed surfaceView surface lost tid=$id")
                            }
                            if (mHaveEglSurface) {
                                stopEglSurfaceLocked()
                            }
                            mWaitingForSurface = true
                            mSurfaceIsBad = false
                            (sGLThreadManager as Object).notifyAll()
                        }

                        // Have we acquired the surface view surface?
                        if (mHasSurface && mWaitingForSurface) {
                            if (LOG_SURFACE) {
                                Log.i(
                                    "GLThread",
                                    "noticed surfaceView surface acquired tid=$id"
                                )
                            }
                            mWaitingForSurface = false
                            (sGLThreadManager as Object).notifyAll()
                        }

                        if (doRenderNotification) {
                            if (LOG_SURFACE) {
                                Log.i("GLThread", "sending render notification tid=$id")
                            }
                            mWantRenderNotification = false
                            doRenderNotification = false
                            mRenderComplete = true
                            (sGLThreadManager as Object).notifyAll()
                        }

                        if (mFinishDrawingRunnable != null) {
                            finishDrawingRunnable = mFinishDrawingRunnable
                            mFinishDrawingRunnable = null
                        }

                        // Ready to draw?
                        if (readyToDraw()) {
                            // If we don't have an EGL context, try to acquire one.

                            if (!mHaveEglContext) {
                                if (askedToReleaseEglContext) {
                                    askedToReleaseEglContext = false
                                } else {
                                    try {
                                        mEglHelper?.start()
                                    } catch (t: RuntimeException) {
                                        sGLThreadManager.releaseEglContextLocked(this)
                                        throw t
                                    }
                                    mHaveEglContext = true
                                    createEglContext = true

                                    (sGLThreadManager as Object).notifyAll()
                                }
                            }

                            if (mHaveEglContext && !mHaveEglSurface) {
                                mHaveEglSurface = true
                                createEglSurface = true
                                createGlInterface = true
                                sizeChanged = true
                            }

                            if (mHaveEglSurface) {
                                if (mSizeChanged) {
                                    sizeChanged = true
                                    w = mWidth
                                    h = mHeight
                                    mWantRenderNotification = true
                                    if (LOG_SURFACE) {
                                        Log.i(
                                            "GLThread",
                                            "noticing that we want render notification tid="
                                                    + id
                                        )
                                    }

                                    // Destroy and recreate the EGL surface.
                                    createEglSurface = true

                                    mSizeChanged = false
                                }
                                mRequestRender = false
                                (sGLThreadManager as Object).notifyAll()
                                if (mWantRenderNotification) {
                                    wantRenderNotification = true
                                }
                                break
                            }
                        } else {
                            if (finishDrawingRunnable != null) {
                                Log.w(
                                    TAG, "Warning, !readyToDraw() but waiting for " +
                                            "draw finished! Early reporting draw finished."
                                )
                                finishDrawingRunnable!!.run()
                                finishDrawingRunnable = null
                            }
                        }
                        // By design, this is the only place in a GLThread thread where we wait().
                        if (LOG_THREADS) {
                            Log.i(
                                "GLThread", ("waiting tid=" + id
                                        + " mHaveEglContext: " + mHaveEglContext
                                        + " mHaveEglSurface: " + mHaveEglSurface
                                        + " mFinishedCreatingEglSurface: " + mFinishedCreatingEglSurface
                                        + " mPaused: " + mPaused
                                        + " mHasSurface: " + mHasSurface
                                        + " mSurfaceIsBad: " + mSurfaceIsBad
                                        + " mWaitingForSurface: " + mWaitingForSurface
                                        + " mWidth: " + mWidth
                                        + " mHeight: " + mHeight
                                        + " mRequestRender: " + mRequestRender
                                        + " mRenderMode: " + mRenderMode)
                            )
                        }
                        (sGLThreadManager as Object).wait()
                    }
                }  // end of synchronized(sGLThreadManager)

                if (event != null) {
                    event!!.run()
                    event = null
                    continue
                }

                if (createEglSurface) {
                    if (LOG_SURFACE) {
                        Log.w("GLThread", "egl createSurface")
                    }
                    if (mEglHelper?.createSurface() == true) {
                        synchronized(sGLThreadManager) {
                            mFinishedCreatingEglSurface = true
                            (sGLThreadManager as Object).notifyAll()
                        }
                    } else {
                        synchronized(sGLThreadManager) {
                            mFinishedCreatingEglSurface = true
                            mSurfaceIsBad = true
                            (sGLThreadManager as Object).notifyAll()
                        }
                        continue
                    }
                    createEglSurface = false
                }

                if (createGlInterface) {
                    gl = mEglHelper?.createGL() as GL10?

                    createGlInterface = false
                }

                if (createEglContext) {
                    if (LOG_RENDERER) {
                        Log.w("GLThread", "onSurfaceCreated")
                    }
                    val view = mGLSurfaceViewWeakRef!!.get()
                    if (view != null) {
                        /*try {
                            Trace.traceBegin(Trace.TRACE_TAG_VIEW, "onSurfaceCreated")
                            view.mRenderer.onSurfaceCreated(gl, mEglHelper.mEglConfig)
                        } finally {
                            Trace.traceEnd(Trace.TRACE_TAG_VIEW)
                        }*/
                        view.mRendererDelegate?.mRenderer?.onRenderSurfaceCreated(
                            mEglHelper?.mEglConfig,
                            gl,
                            -1,
                            -1
                        )
                    }
                    createEglContext = false
                }

                if (sizeChanged) {
                    if (LOG_RENDERER) {
                        Log.w("GLThread", "onSurfaceChanged($w, $h)")
                    }
                    val view = mGLSurfaceViewWeakRef?.get()
                    if (view != null) {
                        /*try {
                            Trace.traceBegin(Trace.TRACE_TAG_VIEW, "onSurfaceChanged")
                            view.mRenderer.onSurfaceChanged(gl, w, h)
                        } finally {
                            Trace.traceEnd(Trace.TRACE_TAG_VIEW)
                        }*/
                        view.mRendererDelegate?.mRenderer?.onRenderSurfaceSizeChanged(gl, w, h)
                    }
                    sizeChanged = false
                }

                if (LOG_RENDERER_DRAW_FRAME) {
                    Log.w("GLThread", "onDrawFrame tid=$id")
                }
                run {
                    val view = mGLSurfaceViewWeakRef!!.get()
                    if (view != null) {
                        /*try {
                            Trace.traceBegin(Trace.TRACE_TAG_VIEW, "onDrawFrame")
                            view.mRenderer.onDrawFrame(gl)
                            if (finishDrawingRunnable != null) {
                                finishDrawingRunnable!!.run()
                                finishDrawingRunnable = null
                            }
                        } finally {
                            Trace.traceEnd(Trace.TRACE_TAG_VIEW)
                        }*/
                        view.mRendererDelegate?.mRenderer?.onRenderFrame(gl)
                    }
                }
                val swapError = mEglHelper?.swap() ?: 0
                when (swapError) {
                    EGL10.EGL_SUCCESS -> {}
                    EGL11.EGL_CONTEXT_LOST -> {
                        if (LOG_SURFACE) {
                            Log.i("GLThread", "egl context lost tid=$id")
                        }
                        lostEglContext = true
                    }

                    else -> {
                        // Other errors typically mean that the current surface is bad,
                        // probably because the SurfaceView surface has been destroyed,
                        // but we haven't been notified yet.
                        // Log the error to help developers understand why rendering stopped.
                        EglHelper.logEglErrorAsWarning(
                            "GLThread",
                            "eglSwapBuffers",
                            swapError
                        )

                        synchronized(sGLThreadManager) {
                            mSurfaceIsBad = true
                            (sGLThreadManager as Object).notifyAll()
                        }
                    }
                }

                if (wantRenderNotification) {
                    doRenderNotification = true
                    wantRenderNotification = false
                }
            }
        } finally {
            /*
                 * clean-up everything...
                 */
            synchronized(sGLThreadManager) {
                stopEglSurfaceLocked()
                stopEglContextLocked()
            }
        }
    }

    fun ableToDraw(): Boolean {
        return mHaveEglContext && mHaveEglSurface && readyToDraw()
    }

    private fun readyToDraw(): Boolean {
        return (!mPaused) && mHasSurface && (!mSurfaceIsBad)
                && (mWidth > 0) && (mHeight > 0)
                && (mRequestRender || (mRenderMode == GLSurfaceView.RENDERMODE_CONTINUOUSLY))
    }

    fun setRenderMode(renderMode: Int) {
        require((GLSurfaceView.RENDERMODE_WHEN_DIRTY <= renderMode) && (renderMode <= GLSurfaceView.RENDERMODE_CONTINUOUSLY)) { "renderMode" }
        synchronized(sGLThreadManager) {
            mRenderMode = renderMode
            (sGLThreadManager as Object).notifyAll()
        }
    }

    fun getRenderMode(): Int {
        synchronized(sGLThreadManager) {
            return mRenderMode
        }
    }

    fun requestRender() {
        synchronized(sGLThreadManager) {
            mRequestRender = true
            (sGLThreadManager as Object).notifyAll()
        }
    }

    fun requestRenderAndNotify(finishDrawing: Runnable?) {
        synchronized(sGLThreadManager) {
            // If we are already on the GL thread, this means a client callback
            // has caused reentrancy, for example via updating the SurfaceView parameters.
            // We will return to the client rendering code, so here we don't need to
            // do anything.
            if (currentThread() === this) {
                return
            }

            mWantRenderNotification = true
            mRequestRender = true
            mRenderComplete = false
            val oldCallback = mFinishDrawingRunnable
            mFinishDrawingRunnable = Runnable {
                oldCallback?.run()
                finishDrawing?.run()
            }
            (sGLThreadManager as Object).notifyAll()
        }
    }

    fun surfaceCreated() {
        synchronized(sGLThreadManager) {
            if (LOG_THREADS) {
                Log.i("GLThread", "surfaceCreated tid=$id")
            }
            mHasSurface = true
            mFinishedCreatingEglSurface = false
            (sGLThreadManager as Object).notifyAll()
            while (mWaitingForSurface
                && !mFinishedCreatingEglSurface && !mExited
            ) {
                try {
                    (sGLThreadManager as Object).wait()
                } catch (e: InterruptedException) {
                    currentThread().interrupt()
                }
            }
        }
    }

    fun surfaceCreated(w: Int, h: Int) {
        synchronized(sGLThreadManager) {
            if (LOG_THREADS) {
                Log.i("RajawaliGLThread", "surfaceCreated tid=$id")
            }
            mHasSurface = true
            mWidth = w
            mHeight = h
            mFinishedCreatingEglSurface = false
            (sGLThreadManager as Object).notifyAll()
            while (mWaitingForSurface
                && !mFinishedCreatingEglSurface && !mExited
            ) {
                try {
                    (sGLThreadManager as Object).wait()
                } catch (e: InterruptedException) {
                    currentThread().interrupt()
                }
            }
        }
    }

    fun surfaceDestroyed() {
        synchronized(sGLThreadManager) {
            if (LOG_THREADS) {
                Log.i("GLThread", "surfaceDestroyed tid=$id")
            }
            mHasSurface = false
            (sGLThreadManager as Object).notifyAll()
            while ((!mWaitingForSurface) && (!mExited)) {
                try {
                    (sGLThreadManager as Object).wait()
                } catch (e: InterruptedException) {
                    currentThread().interrupt()
                }
            }
        }
    }

    fun onPause() {
        synchronized(sGLThreadManager) {
            if (LOG_PAUSE_RESUME) {
                Log.i("GLThread", "onPause tid=$id")
            }
            mRequestPaused = true
            (sGLThreadManager as Object).notifyAll()
            while ((!mExited) && (!mPaused)) {
                if (LOG_PAUSE_RESUME) {
                    Log.i("Main thread", "onPause waiting for mPaused.")
                }
                try {
                    (sGLThreadManager as Object).wait()
                } catch (ex: InterruptedException) {
                    currentThread().interrupt()
                }
            }
        }
    }

    fun onResume() {
        synchronized(sGLThreadManager) {
            if (LOG_PAUSE_RESUME) {
                Log.i("GLThread", "onResume tid=$id")
            }
            mRequestPaused = false
            mRequestRender = true
            mRenderComplete = false
            (sGLThreadManager as Object).notifyAll()
            while ((!mExited) && mPaused && (!mRenderComplete)) {
                if (LOG_PAUSE_RESUME) {
                    Log.i("Main thread", "onResume waiting for !mPaused.")
                }
                try {
                    (sGLThreadManager as Object).wait()
                } catch (ex: InterruptedException) {
                    currentThread().interrupt()
                }
            }
        }
    }

    fun onWindowResize(w: Int, h: Int) {
        synchronized(sGLThreadManager) {
            mWidth = w
            mHeight = h
            mSizeChanged = true
            mRequestRender = true
            mRenderComplete = false

            // If we are already on the GL thread, this means a client callback
            // has caused reentrancy, for example via updating the SurfaceView parameters.
            // We need to process the size change eventually though and update our EGLSurface.
            // So we set the parameters and return so they can be processed on our
            // next iteration.
            if (currentThread() === this) {
                return
            }

            (sGLThreadManager as Object).notifyAll()

            // Wait for thread to react to resize and render a frame
            while (!mExited && !mPaused && !mRenderComplete && ableToDraw()) {
                if (LOG_SURFACE) {
                    Log.i(
                        "Main thread",
                        "onWindowResize waiting for render complete from tid=$id"
                    )
                }
                try {
                    (sGLThreadManager as Object).wait()
                } catch (ex: InterruptedException) {
                    currentThread().interrupt()
                }
            }
        }
    }

    fun requestExitAndWait() {
        // don't call this from GLThread thread or it is a guaranteed
        // deadlock!
        synchronized(sGLThreadManager) {
            mShouldExit = true
            (sGLThreadManager as Object).notifyAll()
            while (!mExited) {
                try {
                    (sGLThreadManager as Object).wait()
                } catch (ex: InterruptedException) {
                    currentThread().interrupt()
                }
            }
        }
    }

    fun requestReleaseEglContextLocked() {
        mShouldReleaseEglContext = true
        (sGLThreadManager as Object).notifyAll()
    }

    /**
     * Queue an "event" to be run on the GL rendering thread.
     * @param r the runnable to be run on the GL rendering thread.
     */
    fun queueEvent(r: Runnable?) {
        requireNotNull(r) { "r must not be null" }
        synchronized(sGLThreadManager) {
            mEventQueue.add(r)
            (sGLThreadManager as Object).notifyAll()
        }
    }

    // Once the thread is started, all accesses to the following member
    // variables are protected by the sGLThreadManager monitor
    internal var mShouldExit = false
    internal var mExited = false
    internal var mRequestPaused = false
    internal var mPaused = false
    internal var mHasSurface = false
    internal var mSurfaceIsBad = false
    internal var mWaitingForSurface = false
    internal var mHaveEglContext = false
    internal var mHaveEglSurface = false
    internal var mFinishedCreatingEglSurface = false
    internal var mShouldReleaseEglContext = false
    internal var mWidth = 0
    internal var mHeight = 0
    internal var mRenderMode = 0
    internal var mRequestRender = false
    internal var mWantRenderNotification = false
    internal var mRenderComplete = false
    internal val mEventQueue = ArrayList<Runnable>()
    internal var mSizeChanged = true
    internal var mFinishDrawingRunnable: Runnable? = null


    // End of member variables protected by the sGLThreadManager monitor.
    private var mEglHelper: EglHelper? = null

    /**
     * Set once at thread construction time, nulled out when the parent view is garbage
     * called. This weak reference allows the GLSurfaceView to be garbage collected while
     * the GLThread is still alive.
     */
    private var mGLSurfaceViewWeakRef: WeakReference<OpenGLTextureView>? = null
}

/**
 * An EGL helper class.
 */
internal class EglHelper(private val mGLSurfaceViewWeakRef: WeakReference<OpenGLTextureView>?) {
    /**
     * Initialize EGL for a given configuration spec.
     * @param configSpec
     */
    fun start() {
        if (LOG_EGL) {
            Log.w("EglHelper", "start() tid=" + currentThread().id)
        }
        /*
         * Get an EGL instance
         */
        mEgl = EGLContext.getEGL() as EGL10

        /*
         * Get to the default display.
         */
        mEglDisplay = mEgl!!.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY)

        if (mEglDisplay === EGL10.EGL_NO_DISPLAY) {
            throw java.lang.RuntimeException("eglGetDisplay failed")
        }

        /*
         * We can now initialize EGL for that display
         */
        val version = IntArray(2)
        if (!mEgl!!.eglInitialize(mEglDisplay, version)) {
            throw java.lang.RuntimeException("eglInitialize failed")
        }
        val view = mGLSurfaceViewWeakRef?.get()
        if (view == null) {
            mEglConfig = null
            mEglContext = null
        } else {
            mEglConfig = view.mEGLConfigChooser?.chooseConfig(mEgl, mEglDisplay)

            /*
            * Create an EGL context. We want to do this as rarely as we can, because an
            * EGL context is a somewhat heavy object.
            */
            mEglContext = view.mEGLContextFactory?.createContext(mEgl, mEglDisplay, mEglConfig)
        }
        if (mEglContext == null || mEglContext === EGL10.EGL_NO_CONTEXT) {
            mEglContext = null
            throwEglException("createContext")
        }
        if (LOG_EGL) {
            Log.w("EglHelper", "createContext " + mEglContext + " tid=" + currentThread().id)
        }

        mEglSurface = null
    }

    /**
     * Create an egl surface for the current SurfaceHolder surface. If a surface
     * already exists, destroy it before creating the new surface.
     *
     * @return true if the surface was created successfully.
     */
    fun createSurface(): Boolean {
        if (LOG_EGL) {
            Log.w("EglHelper", "createSurface()  tid=" + currentThread().id)
        }
        /*
         * Check preconditions.
         */
        if (mEgl == null) {
            throw java.lang.RuntimeException("egl not initialized")
        }
        if (mEglDisplay == null) {
            throw java.lang.RuntimeException("eglDisplay not initialized")
        }
        if (mEglConfig == null) {
            throw java.lang.RuntimeException("mEglConfig not initialized")
        }

        /*
         *  The window size has changed, so we need to create a new
         *  surface.
         */
        destroySurfaceImp()

        /*
         * Create an EGL surface we can render into.
         */
        val view = mGLSurfaceViewWeakRef?.get()
        mEglSurface = view?.mEGLWindowSurfaceFactory?.createWindowSurface(
            mEgl,
            mEglDisplay, mEglConfig, view.surfaceTexture
        )

        if (mEglSurface == null || mEglSurface === EGL10.EGL_NO_SURFACE) {
            val error = mEgl!!.eglGetError()
            if (error == EGL10.EGL_BAD_NATIVE_WINDOW) {
                Log.e("EglHelper", "createWindowSurface returned EGL_BAD_NATIVE_WINDOW.")
            }
            return false
        }

        /*
         * Before we can issue GL commands, we need to make sure
         * the context is current and bound to a surface.
         */
        if (!mEgl!!.eglMakeCurrent(mEglDisplay, mEglSurface, mEglSurface, mEglContext)) {
            /*
             * Could not make the context current, probably because the underlying
             * SurfaceView surface has been destroyed.
             */
            logEglErrorAsWarning("EGLHelper", "eglMakeCurrent", mEgl!!.eglGetError())
            return false
        }

        return true
    }

    /**
     * Create a GL object for the current EGL context.
     * @return
     */
    fun createGL(): GL {
        var gl = mEglContext!!.gl
        val view = mGLSurfaceViewWeakRef?.get()
        if (view != null) {
            if (view.mGLWrapper != null) {
                gl = view.mGLWrapper!!.wrap(gl)
            }

            if ((view.mDebugFlags and (GLSurfaceView.DEBUG_CHECK_GL_ERROR or GLSurfaceView.DEBUG_LOG_GL_CALLS)) != 0) {
                var configFlags = 0
                var log: Writer? = null
                if ((view.mDebugFlags and GLSurfaceView.DEBUG_CHECK_GL_ERROR) != 0) {
                    configFlags = configFlags or GLDebugHelper.CONFIG_CHECK_GL_ERROR
                }
                if ((view.mDebugFlags and GLSurfaceView.DEBUG_LOG_GL_CALLS) != 0) {
                    log = LogWriter()
                }
                gl = GLDebugHelper.wrap(gl, configFlags, log)
            }
        }
        return gl
    }

    /**
     * Display the current render surface.
     * @return the EGL error code from eglSwapBuffers.
     */
    fun swap(): Int {
        if (!mEgl!!.eglSwapBuffers(mEglDisplay, mEglSurface)) {
            return mEgl!!.eglGetError()
        }
        return EGL10.EGL_SUCCESS
    }

    fun destroySurface() {
        if (LOG_EGL) {
            Log.w("EglHelper", "destroySurface()  tid=" + currentThread().id)
        }
        destroySurfaceImp()
    }

    fun destroySurfaceImp() {
        if (mEglSurface != null && mEglSurface !== EGL10.EGL_NO_SURFACE) {
            mEgl!!.eglMakeCurrent(
                mEglDisplay, EGL10.EGL_NO_SURFACE,
                EGL10.EGL_NO_SURFACE,
                EGL10.EGL_NO_CONTEXT
            )
            val view = mGLSurfaceViewWeakRef?.get()
            view?.mEGLWindowSurfaceFactory?.destroySurface(mEgl, mEglDisplay, mEglSurface)
            mEglSurface = null
        }
    }

    fun finish() {
        if (LOG_EGL) {
            Log.w("EglHelper", "finish() tid=" + currentThread().id)
        }
        if (mEglContext != null) {
            val view = mGLSurfaceViewWeakRef?.get()
            view?.mEGLContextFactory?.destroyContext(mEgl, mEglDisplay, mEglContext)
            mEglContext = null
        }
        if (mEglDisplay != null) {
            mEgl!!.eglTerminate(mEglDisplay)
            mEglDisplay = null
        }
    }

    fun throwEglException(function: String) {
        throwEglException(function, mEgl!!.eglGetError())
    }

    var mEgl: EGL10? = null
    var mEglDisplay: EGLDisplay? = null
    var mEglSurface: EGLSurface? = null
    var mEglConfig: EGLConfig? = null

    var mEglContext: EGLContext? = null

    companion object {

        const val LOG_ATTACH_DETACH: Boolean = false
        const val LOG_THREADS: Boolean = false
        const val LOG_PAUSE_RESUME: Boolean = false
        const val LOG_SURFACE: Boolean = false
        const val LOG_RENDERER: Boolean = false
        const val LOG_RENDERER_DRAW_FRAME: Boolean = false
        const val LOG_EGL: Boolean = false
        const val TAG: String = "angcyo"

        fun throwEglException(function: String, error: Int) {
            val message = formatEglError(function, error)
            if (LOG_THREADS) {
                Log.e(
                    "EglHelper", ("throwEglException tid=" + currentThread().id + " "
                            + message)
                )
            }
            throw java.lang.RuntimeException(message)
        }

        fun logEglErrorAsWarning(tag: String?, function: String, error: Int) {
            Log.w(tag, formatEglError(function, error))
        }

        fun formatEglError(function: String, error: Int): String {
            return function + " failed: " + getErrorString(error)
        }

        fun getErrorString(error: Int): String {
            return when (error) {
                EGL10.EGL_SUCCESS -> "EGL_SUCCESS"
                EGL10.EGL_NOT_INITIALIZED -> "EGL_NOT_INITIALIZED"
                EGL10.EGL_BAD_ACCESS -> "EGL_BAD_ACCESS"
                EGL10.EGL_BAD_ALLOC -> "EGL_BAD_ALLOC"
                EGL10.EGL_BAD_ATTRIBUTE -> "EGL_BAD_ATTRIBUTE"
                EGL10.EGL_BAD_CONFIG -> "EGL_BAD_CONFIG"
                EGL10.EGL_BAD_CONTEXT -> "EGL_BAD_CONTEXT"
                EGL10.EGL_BAD_CURRENT_SURFACE -> "EGL_BAD_CURRENT_SURFACE"
                EGL10.EGL_BAD_DISPLAY -> "EGL_BAD_DISPLAY"
                EGL10.EGL_BAD_MATCH -> "EGL_BAD_MATCH"
                EGL10.EGL_BAD_NATIVE_PIXMAP -> "EGL_BAD_NATIVE_PIXMAP"
                EGL10.EGL_BAD_NATIVE_WINDOW -> "EGL_BAD_NATIVE_WINDOW"
                EGL10.EGL_BAD_PARAMETER -> "EGL_BAD_PARAMETER"
                EGL10.EGL_BAD_SURFACE -> "EGL_BAD_SURFACE"
                EGL11.EGL_CONTEXT_LOST -> "EGL_CONTEXT_LOST"
                else -> getHex(error)
            }
        }

        private fun getHex(value: Int): String {
            return "0x" + Integer.toHexString(value)
        }
    }
}

internal class OpenGLThreadManager {
    @Synchronized
    fun threadExiting(thread: OpenGLTextureThread) {
        if (LOG_THREADS) {
            Log.i("GLThread", "exiting tid=" + thread.getId())
        }
        thread.mExited = true
        (this as Object).notifyAll()
    }

    /*
     * Releases the EGL context. Requires that we are already in the
     * sGLThreadManager monitor when this is called.
     */
    fun releaseEglContextLocked(thread: OpenGLTextureThread?) {
        (this as Object).notifyAll()
    }

    companion object {
        private const val TAG = "GLThreadManager"
    }
}

internal class LogWriter : Writer() {
    override fun close() {
        flushBuilder()
    }

    override fun flush() {
        flushBuilder()
    }

    override fun write(buf: CharArray, offset: Int, count: Int) {
        for (i in 0..<count) {
            val c = buf[offset + i]
            if (c == '\n') {
                flushBuilder()
            } else {
                mBuilder.append(c)
            }
        }
    }

    private fun flushBuilder() {
        if (mBuilder.length > 0) {
            Log.v("GLSurfaceView", mBuilder.toString())
            mBuilder.delete(0, mBuilder.length)
        }
    }

    private val mBuilder = StringBuilder()
}