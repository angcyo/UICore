package com.angcyo.opengl

import android.graphics.SurfaceTexture
import android.opengl.GLSurfaceView
import android.view.TextureView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @date 2025/03/24
 *
 * OpenGL 渲染器, 用来渲染每一帧的回调
 *
 * https://github.com/Rajawali/Rajawali
 * https://github.com/Rajawali/RajawaliExamples
 * https://github.com/AndroidStudioIst/RajawaliExamples
 *
 */
interface IOpenGLRenderer {

    //region --OpenGLThread--

    /**
     * This corresponds to [TextureView.SurfaceTextureListener.onSurfaceTextureAvailable]
     * and [GLSurfaceView.Renderer.onSurfaceCreated]. Unused parameters are passed as null or -1.
     *
     * @param config [config][EGLConfig]. This is used if the surface is [GL10] type (SurfaceView).
     * @param gl [GL10] for rendering.
     * @param width `width` The surface width in pixels.
     * @param height `height` The surface height in pixels.
     */
    fun onRenderSurfaceCreated(config: EGLConfig?, gl: GL10?, width: Int, height: Int)

    /**
     * This corresponds to [TextureView.SurfaceTextureListener.onSurfaceTextureSizeChanged]
     * and [GLSurfaceView.Renderer.onSurfaceChanged].
     *
     * @param gl [GL10] for rendering.
     * @param width `width` The surface width in pixels.
     * @param height `height` The surface height in pixels.
     */
    fun onRenderSurfaceSizeChanged(gl: GL10?, width: Int, height: Int)

    /**
     * Called when the renderer should draw its next frame.
     *
     * @param gl [GL10] for rendering.
     */
    fun onRenderFrame(gl: GL10?)

    //endregion --OpenGLThread--

    //region --lifecycle--

    /**
     * Called when the renderer should pause all of its rendering activities, such as frame draw requests.
     */
    fun onPause()

    /**
     * Called when the renderer should continue all of its rendering activities, such as frame draw requests.
     */
    fun onResume()

    /**
     * Called when the rendering surface has been destroyed, such as the view being detached from the window.
     *
     * @param surface [SurfaceTexture] The texture which was being rendered to.
     */
    fun onRenderSurfaceDestroyed(surface: SurfaceTexture?)

    //endregion --lifecycle--

    //region --renderer--

    /**
     * Fetch the current target frame rate in frames per second.
     *
     * @return `double` The target frame rate.
     */
    fun getFrameRate(): Double

    /**
     * Sets the target frame rate in frames per second.
     *
     * @param rate `int` The target rate.
     */
    fun setFrameRate(rate: Int)

    /**
     * Sets the target frame rate in frames per second.
     *
     * @param rate `double` The target rate.
     */
    fun setFrameRate(rate: Double)

    /**
     * Called to inform the renderer of the multisampling configuration on this surface.
     *
     * @param config [ISurface.ANTI_ALIASING_CONFIG] The desired anti aliasing configuration.
     */
    fun setAntiAliasingMode(config: ANTI_ALIASING_CONFIG)

    /**
     * Sets the [ISurface] which this implementation will be rendering on.
     *
     * @param surface [ISurface] The rendering surface.
     */
    fun setRenderSurface(surface: IOpenGLSurface?)

    //endregion --renderer--
}