package com.angcyo.library.unit

import android.util.TypedValue
import com.angcyo.library.annotation.Pixel
import com.angcyo.library.ex.ceil
import com.angcyo.library.ex.decimal
import kotlin.math.max
import kotlin.math.nextDown
import kotlin.math.roundToInt

/**
 * 像素点转换成 mm/inch 毫米/英寸
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/24
 */
interface IValueUnit {

    //region---刻度相关方法---

    /**获取关键刻度索引的位置
     * 毫米单位:每10个点, 绘制一次大刻度;每5个点, 绘制一次中刻度
     * 英寸单位:每16个点, 绘制一次大刻度;每8个点, 绘制一次中刻度
     * */
    fun getGraduatedIndexGap(): Int = when (this) {
        is InchValueUnit -> 16
        is PixelValueUnit -> 100
        else -> 10
    }

    /**获取每个刻度之间最小的间隙, 像素*/
    @Pixel
    fun getGraduatedMinGap(): Double = when (this) {
        is InchValueUnit -> convertValueToPixel(1.0) / getGraduatedIndexGap()
        else -> convertValueToPixel(1.0)
    }

    /**获取刻度尺刻度之间的间隔像素距离
     * [scale] 当前缩放的比例*/
    @Pixel
    fun getGraduatedScaleGap(scale: Float): Double {
        var inScaleStep = 5.0 //每放大5的倍数, 处理一次
        var deScaleStep = 2.0 //每缩小2的倍数, 处理一次
        if (this is PixelValueUnit) {
            inScaleStep = 5.0
            deScaleStep = 1.0
        }
        return getGraduatedScaleGap(getGraduatedMinGap(), scale, inScaleStep, deScaleStep)
    }

    /**[getGraduatedScaleGap]*/
    @Pixel
    fun getGraduatedScaleGap(
        value: Double,
        scale: Float,
        inScaleStep: Double,
        deScaleStep: Double
    ): Double {
        val inStep = 1.0
        return if (scale > 1) {
            val m = (scale / inStep).ceil() // 放大的倍数
            val inScale = max(1.0, (m / inScaleStep).ceil()) //每放大5的倍数, 处理一次
            value / inScale //间隙缩小
        } else if (scale < 1) {
            val m = (inStep / scale).ceil() //缩小的倍数
            val deScale = max(1.0, (m / deScaleStep).ceil()) //每缩小5的倍数, 处理一次
            value * deScale //间隙缩小
        } else {
            value //不变
        }
    }

    /**获取第[index]个刻度的Label标签
     * [gap] 每个刻度之间的间隙, 像素*/
    fun getGraduatedLabel(index: Int, @Pixel gap: Double): String {
        val pixel = index * gap
        val value = convertValueToPixel(1.0)

        val graduatedIndexGap = getGraduatedIndexGap()
        if (index % graduatedIndexGap == 0) {
        } else if (index % (graduatedIndexGap / 2) == 0 && this is InchValueUnit) {
            return (pixel / value).unitDecimal(1, ensureInt = true)
        }
        return "${(pixel / value).roundToInt()}"
    }

    //endregion---刻度相关方法---

    //region---值转换相关方法---

    @Pixel
    fun convertValueToPixel(value: Float): Float = convertValueToPixel(value.toDouble()).toFloat()

    /**获取每个单位间隔刻度对应的像素大小
     * 将1个单位的值, 转换成屏幕像素点数值
     * [TypedValue.COMPLEX_UNIT_MM]*/
    @Pixel
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

    //endregion---值转换相关方法---
}

/**保留小数点后几位
 * [ensureInt] 如果是整数, 优先使用整数*/
fun Double.unitDecimal(
    digit: Int = 2,
    fadedUp: Boolean = true,
    ensureInt: Boolean = false
): String {
    if (ensureInt) {
        val int = toInt()
        if (this == int.toDouble()) {
            return "$int"
        }
    }
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