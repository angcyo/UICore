package com.angcyo.library.unit

import android.util.DisplayMetrics
import android.util.TypedValue
import com.angcyo.library.app
import com.angcyo.library.isPlaceholderApplication

/**
 * Point pt单位
 *
 * px = pt * DPI / 72
 * pt = px * 72 / DPI
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/09/19
 */
class PointValueUnit : IValueUnit {

    /**[TypedValue.COMPLEX_UNIT_PT]*/
    override fun convertValueToPixel(value: Double): Double {
        val app = app()
        if (app.isPlaceholderApplication()) {
            return value
        }
        val dm: DisplayMetrics = app.resources.displayMetrics
        //1毫米等于多少像素                             //21.176456
        //1英寸等于多少像素, 1英寸=2.54厘米=25.4毫米      //537.882
        //1pt = 7.4705834px
        //1 * dm.densityDpi / 72 = 7.0277777px
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PT, value.toFloat(), dm).toDouble()
    }

    override fun formattedValueUnit(value: Double): String = "${value.unitDecimal(2)}${getUnit()}"

    override fun getUnit(): String = "pt"
}