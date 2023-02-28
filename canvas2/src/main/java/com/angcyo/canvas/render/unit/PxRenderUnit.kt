package com.angcyo.canvas.render.unit

/**
 * 像素渲染单位
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/14
 */
class PxRenderUnit : IRenderUnit {

    override fun getGap(scale: Float): Float {
        val base = convertValueToPixel(10f)
        return if (scale >= 4) {
            //放大4倍后
            base / 2
        } else if (scale <= 0.1f) {
            base * 50
        } else if (scale <= 0.25f) {
            //缩小4倍后
            base * 10
        } else if (scale <= 0.75f) {
            base * 5
        } else {
            base
        }
    }

    override fun getUnit(): String = "px"

    override fun convertPixelToValue(pixel: Float): Float = pixel

    override fun convertValueToPixel(value: Float): Float = value
}