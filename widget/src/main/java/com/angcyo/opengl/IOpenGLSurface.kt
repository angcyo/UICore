package com.angcyo.opengl

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @date 2025/03/24
 *
 * OpenGL Surface, 用来管理[IOpenGLRenderer].
 * 渲染器的容器
 *
 * https://github.com/Rajawali/Rajawali
 * https://github.com/Rajawali/RajawaliExamples
 * https://github.com/AndroidStudioIst/RajawaliExamples
 *
 */
interface IOpenGLSurface {

    /**
     * Sets the target frame rate in frames per second.
     *
     * @param rate `double` The target rate.
     */
    fun setFrameRate(rate: Double)

    /**
     * Gets the current rendering mode.
     *
     * @return `int` The current rendering mode.
     */
    fun getRenderMode(): Int

    /**
     * Sets the desired rendering mode
     *
     * @param mode `int` The desired rendering mode.
     */
    fun setRenderMode(mode: Int)

    /**
     * Called to enable/disable multisampling on this surface.
     * Must be called before [.setSurfaceRenderer].
     *
     * @param config [ANTI_ALIASING_CONFIG] The desired anti aliasing configuration.
     */
    fun setAntiAliasingMode(config: ANTI_ALIASING_CONFIG)

    /**
     * Sets the sample count to use. Only applies if multisample antialiasing is active.
     *
     * @param count `int` The sample count.
     */
    fun setSampleCount(count: Int)


    /**
     * Called to set the [ISurfaceRenderer] which will render on this surface.
     *
     * @param renderer [ISurfaceRenderer] instance.
     * @throws IllegalStateException Thrown if a renderer has already been set.
     */
    @Throws(IllegalStateException::class)
    fun setSurfaceRenderer(renderer: IOpenGLRenderer)

    /**
     * Called when a render request should be made.
     */
    fun requestRenderUpdate()
}

/**
 * Enum of available anti-aliasing configurations.
 */
enum class ANTI_ALIASING_CONFIG {
    NONE, MULTISAMPLING, COVERAGE;

    companion object {
        fun fromInteger(i: Int): ANTI_ALIASING_CONFIG {
            when (i) {
                0 -> return NONE
                1 -> return MULTISAMPLING
                2 -> return COVERAGE
            }
            return NONE
        }
    }
}