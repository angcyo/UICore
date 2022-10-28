package com.angcyo.library.unit

import android.util.TypedValue
import com.angcyo.library.ex.decimal
import kotlin.math.nextDown

/**
 * 像素点转换成 mm/inch 毫米/英寸
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/24
 */
interface IValueUnit {

    /**获取刻度尺刻度之间的间隔像素距离*/
    fun getGraduatedScaleGap(): Double

    /**获取第[index]个刻度的Label标签*/
    fun getGraduatedLabel(index: Int): String

    fun convertValueToPixel(value: Float): Float = convertValueToPixel(value.toDouble()).toFloat()

    /**获取每个单位间隔刻度对应的像素大小
     * 将1个单位的值, 转换成屏幕像素点数值
     * [TypedValue.COMPLEX_UNIT_MM]*/
    fun convertValueToPixel(value: Double): Double

    fun convertPixelToValue(value: Float): Double = convertPixelToValue(value.toDouble())

    /**将像素转换为单位数值*/
    fun convertPixelToValue(pixel: Double): Double {
        val unit = convertValueToPixel(1.0)
        val result = pixel / unit
        if (result.isFinite()) {
            return result
        }
        return result.nextDown()
    }

    /**将value转换成对应单位的文本*/
    fun formattedValueUnit(value: Double): String

    fun formattedValueUnit(value: Float): String = formattedValueUnit(value.toDouble())

    /**获取描述的单位字符创*/
    fun getUnit(): String
}

/**保留小数点后几位*/
fun Double.unitDecimal(digit: Int = 2, fadedUp: Boolean = true): String {
    return decimal(digit, fadedUp)
}

fun IValueUnit.convertPixelToValueUnit(pixel: Float?): String =
    convertPixelToValueUnit(pixel?.toDouble())

/**[convertPixelToValue]
 * [formattedValueUnit]*/
fun IValueUnit.convertPixelToValueUnit(pixel: Double?): String {
    if (pixel == null) {
        return formattedValueUnit(0.0)
    }
    val value = convertPixelToValue(pixel)
    return formattedValueUnit(value)
}