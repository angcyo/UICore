package com.angcyo.canvas.core

import android.util.DisplayMetrics
import android.util.TypedValue
import com.angcyo.library.app
import com.angcyo.library.ex.abs
import com.angcyo.library.ex.decimal

/**
 * 像素点转换成 mm/inch 毫米/英寸
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/04/04
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class ValueUnit {

    /**绘制时使用的值类型, 最后要都要转换成像素, 在界面上绘制*/
    var valueType: Int = TypedValue.COMPLEX_UNIT_MM

    /**获取每个单位间隔刻度对应的像素大小
     * 将1个单位的值, 转换成屏幕像素点数值
     * [TypedValue.COMPLEX_UNIT_MM]*/
    fun convertValueToPixel(value: Float): Float {
        val dm: DisplayMetrics = app().resources.displayMetrics
        return TypedValue.applyDimension(valueType, value, dm)
    }

    /**将像素转换为单位数值*/
    fun convertPixelToValue(pixel: Float): Float {
        val unit = convertValueToPixel(1f)
        return pixel / unit
    }

    /**将value转换成对应单位的文本*/
    fun formattedValueUnit(value: Float): String {
        return if (valueType == TypedValue.COMPLEX_UNIT_MM) {
            when {
                //value.abs() / 100 > 1 -> "${(value / 100).decimal(2)}m"
                //value.abs() / 10 > 1 -> "${(value / 10).decimal(2)}cm"
                else -> "${value.decimal(2)}mm"
            }
        } else {
            "$value"
        }
    }

}