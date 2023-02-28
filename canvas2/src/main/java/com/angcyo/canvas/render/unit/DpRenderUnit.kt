package com.angcyo.canvas.render.unit

import com.angcyo.library.ex.dp

/**
 * 设备dp渲染单位
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/17
 */
class DpRenderUnit : IRenderUnit {

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

    override fun getRenderType(index: Int, scale: Float): Int {
        return when {
            index % 10 == 0 -> IRenderUnit.AXIS_TYPE_PRIMARY or IRenderUnit.AXIS_TYPE_LABEL
            index % 5 == 0 -> if (scale >= 3 || scale <= 0.75f) {
                IRenderUnit.AXIS_TYPE_SECONDARY or IRenderUnit.AXIS_TYPE_LABEL
            } else {
                IRenderUnit.AXIS_TYPE_SECONDARY
            }
            else -> IRenderUnit.AXIS_TYPE_NORMAL
        }
    }

    override fun getUnit(): String = "dp"

    override fun convertValueToPixel(value: Float): Float = value * dp

    override fun convertPixelToValue(pixel: Float): Float = pixel / dp
}