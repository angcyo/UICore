package com.angcyo.canvas.core

import android.util.TypedValue

/**
 * 像素点转换成 mm/inch 毫米/英寸
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/24
 */
interface IValueUnit {

    /**获取刻度尺刻度之间的间隔像素距离*/
    fun getGraduatedScaleGap(): Float

    /**获取第[index]个刻度的Label标签*/
    fun getGraduatedLabel(index: Int): String

    /**获取每个单位间隔刻度对应的像素大小
     * 将1个单位的值, 转换成屏幕像素点数值
     * [TypedValue.COMPLEX_UNIT_MM]*/
    fun convertValueToPixel(value: Float): Float

    /**将像素转换为单位数值*/
    fun convertPixelToValue(pixel: Float): Float {
        val unit = convertValueToPixel(1f)
        return pixel / unit
    }

    /**将value转换成对应单位的文本*/
    fun formattedValueUnit(value: Float): String
}

/**[convertPixelToValue]
 * [formattedValueUnit]*/
fun IValueUnit.convertPixelToValueUnit(pixel: Float): String {
    val value = convertPixelToValue(pixel)
    return formattedValueUnit(value)
}