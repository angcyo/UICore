package com.angcyo.canvas.render.unit

import android.util.DisplayMetrics
import android.util.TypedValue
import com.angcyo.library.app
import com.angcyo.library.isPlaceholderApplication

/**
 * mm毫米渲染单位
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/03/13
 */
class MmRenderUnit : IRenderUnit {

    override fun getUnit(): String = "mm"

    override fun getGap(scale: Float): Float {
        //1个格子1个mm
        val base = convertValueToPixel(1f)
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

    override fun convertValueToPixel(value: Float): Float {
        val app = app()
        if (app.isPlaceholderApplication()) {
            return value
        }
        val dm: DisplayMetrics = app.resources.displayMetrics
        //1毫米等于多少像素                             //21.176456
        //1英寸等于多少像素, 1英寸=2.54厘米=25.4毫米      //537.882
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, value, dm)
    }
}