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

    /**
     * 将pt单位转换成px单位
     * [TypedValue.COMPLEX_UNIT_PT]*/
    override fun convertValueToPixel(value: Float): Float {
        val app = app()
        if (app.isPlaceholderApplication()) {
            return value
        }
        val dm: DisplayMetrics = app.resources.displayMetrics
        //1毫米等于多少像素                             //21.176456
        //1英寸等于多少像素, 1英寸=2.54厘米=25.4毫米      //537.882
        //1pt = 7.4705834px
        //1 * dm.densityDpi / 72 = 7.0277777px
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PT, value, dm)
    }

    override fun formattedValueUnit(value: Double, ensureInt: Boolean): String =
        "${value.unitDecimal(2, ensureInt = ensureInt)}${getUnit()}"

    override fun formattedValueUnit(value: Float, ensureInt: Boolean): String =
        "${value.unitDecimal(2, ensureInt = ensureInt)}${getUnit()}"

    override fun getUnit(): String = "pt"
}