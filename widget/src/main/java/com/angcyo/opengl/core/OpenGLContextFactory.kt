package com.angcyo.opengl.core

import android.opengl.GLSurfaceView
import com.angcyo.library.L
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLContext
import javax.microedition.khronos.egl.EGLDisplay

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @date 2025/03/17
 *
 *
 * [android.opengl.GLSurfaceView.DefaultContextFactory]
 * */
class OpenGLContextFactory : GLSurfaceView.EGLContextFactory {

    companion object {
        /**
         * https://developer.android.com/develop/ui/views/graphics/opengl/about-opengl?hl=zh-cn#version-check
         * */
        private const val EGL_CONTEXT_CLIENT_VERSION = 0x3098
        private const val glVersion = 2.0 //3.0
    }

    override fun createContext(
        egl: EGL10?,
        display: EGLDisplay?,
        eglConfig: EGLConfig?
    ): EGLContext? {
        L.w("creating OpenGL ES $glVersion context")
        return egl?.eglCreateContext(
            display,
            eglConfig,
            EGL10.EGL_NO_CONTEXT,
            intArrayOf(EGL_CONTEXT_CLIENT_VERSION, glVersion.toInt(), EGL10.EGL_NONE)
        ) // returns null if 3.0 is not supported
    }

    override fun destroyContext(
        egl: EGL10?,
        display: EGLDisplay?,
        context: EGLContext?
    ) {
        L.w("destroy OpenGL ES $glVersion context")
        egl?.eglDestroyContext(display, context)
    }
}