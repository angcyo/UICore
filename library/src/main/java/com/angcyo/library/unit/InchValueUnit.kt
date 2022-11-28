package com.angcyo.library.unit

import android.util.DisplayMetrics
import android.util.TypedValue
import com.angcyo.library.app
import com.angcyo.library.isPlaceholderApplication

/**
 * 英制单位, 英寸
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/04/04
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class InchValueUnit : IValueUnit {

    /**获取每个单位间隔刻度对应的像素大小
     * 将1个单位的值, 转换成屏幕像素点数值
     * [TypedValue.COMPLEX_UNIT_MM]*/
    override fun convertValueToPixel(value: Double): Double {
        val app = app()
        if (app.isPlaceholderApplication()) {
            return value
        }
        val dm: DisplayMetrics = app.resources.displayMetrics
        //1毫米等于多少像素                             //21.176456
        //1英寸等于多少像素, 1英寸=2.54厘米=25.4毫米      //537.882
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_IN, value.toFloat(), dm).toDouble()
    }

    /**将value转换成对应单位的文本*/
    override fun formattedValueUnit(value: Double): String {
        return when {
            //value.abs() / 100 > 1 -> "${(value / 100).decimal(2)}m"
            //value.abs() / 10 > 1 -> "${(value / 10).decimal(2)}cm"
            else -> "${value.unitDecimal(2)}${getUnit()}"
        }
    }

    override fun getUnit(): String = "inch"
}