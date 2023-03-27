package com.angcyo.library.unit

import android.util.DisplayMetrics
import android.util.TypedValue
import com.angcyo.library.app
import com.angcyo.library.isPlaceholderApplication

/**
 * inch英寸渲染单位
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/03/13
 */
class InchRenderUnit : IRenderUnit {

    override fun getUnit(): String = "inch"

    override fun getGap(scale: Float): Float {
        //1个格子1个inch
        val base = convertValueToPixel(0.125f)
        return if (scale >= 4) {
            //放大4倍后
            base / 8
        } else if (scale >= 2) {
            //放大4倍后
            base / 4
        } else if (scale <= 0.1f) {
            base * 4
        } else if (scale <= 0.25f) {
            //缩小4倍后
            base * 2
        } else if (scale <= 0.75f) {
            base * 1
        } else {
            base
        }
    }

    override fun getRenderType(index: Int, scale: Float): Int {
        return when {
            index % 8 == 0 -> IRenderUnit.AXIS_TYPE_PRIMARY or IRenderUnit.AXIS_TYPE_LABEL
            index % 4 == 0 -> IRenderUnit.AXIS_TYPE_SECONDARY
            else -> IRenderUnit.AXIS_TYPE_NORMAL
        }
    }

    override fun convertValueToPixel(value: Float): Float {
        val app = app()
        if (app.isPlaceholderApplication()) {
            return value
        }
        val dm: DisplayMetrics = app.resources.displayMetrics
        //1毫米等于多少像素                             //21.176456
        //1英寸等于多少像素, 1英寸=2.54厘米=25.4毫米      //537.882
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_IN, value, dm)
    }
}