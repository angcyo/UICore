package com.angcyo.library.unit

import android.util.DisplayMetrics
import android.util.TypedValue
import com.angcyo.library.app
import com.angcyo.library.isPlaceholderApplication

/**
 * 公制单位, 毫米
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/04/04
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class MmValueUnit : IValueUnit {

    /**获取每个单位间隔刻度对应的像素大小
     * 将1个单位的值, 转换成屏幕像素点数值
     * [TypedValue.COMPLEX_UNIT_MM]*/
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

    /**将value转换成对应单位的文本*/
    override fun formattedValueUnit(value: Double, ensureInt: Boolean): String {
        return when {
            //value.abs() / 100 > 1 -> "${(value / 100).decimal(2)}m"
            //value.abs() / 10 > 1 -> "${(value / 10).decimal(2)}cm"
            else -> "${value.unitDecimal(2, ensureInt = ensureInt)}${getUnit()}"
        }
    }

    override fun formattedValueUnit(value: Float, ensureInt: Boolean): String {
        return when {
            //value.abs() / 100 > 1 -> "${(value / 100).decimal(2)}m"
            //value.abs() / 10 > 1 -> "${(value / 10).decimal(2)}cm"
            else -> "${value.unitDecimal(2, ensureInt = ensureInt)}${getUnit()}"
        }
    }

    override fun getUnit(): String = "mm"
}