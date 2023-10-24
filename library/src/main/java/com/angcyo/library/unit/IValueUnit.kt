package com.angcyo.library.unit

import android.graphics.PointF
import android.util.TypedValue
import com.angcyo.library.annotation.Pixel
import com.angcyo.library.ex.ceil
import com.angcyo.library.ex.decimal
import com.angcyo.library.unit.IValueUnit.Companion.MM_UNIT
import kotlin.math.max
import kotlin.math.nextDown
import kotlin.math.roundToInt

/**
 * 像素点转换成 mm/inch 毫米/英寸
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/24
 */
interface IValueUnit {

    companion object {
        /**毫米单位计算, Canvas*/
        val MM_UNIT = MmValueUnit()

        /**毫米单位计算, Canvas2*/
        val MM_RENDER_UNIT = MmRenderUnit()
    }

    //region---刻度相关方法---

    /**获取关键刻度索引的位置
     * 毫米单位:每10个点, 绘制一次大刻度;每5个点, 绘制一次中刻度
     * 英寸单位:每16个点, 绘制一次大刻度;每8个点, 绘制一次中刻度
     * */
    fun getGraduatedIndexGap(scale: Float): Int = when (this) {
        is InchValueUnit -> 16
        is PixelValueUnit -> 100
        else -> 10
    }

    /**获取每个刻度之间最小的间隙, 像素*/
    @Pixel
    fun getGraduatedMinGap(scale: Float): Double = when (this) {
        is InchValueUnit -> convertValueToPixel(1.0) / getGraduatedIndexGap(scale)
        else -> convertValueToPixel(1.0)
    }

    /**获取刻度尺刻度之间的间隔像素距离
     * [scale] 当前缩放的比例*/
    @Pixel
    fun getGraduatedScaleGap(scale: Float): Double {
        var inScaleStep = 5.0 //每放大5的倍数, 处理一次
        var deScaleStep = 1.0 //每缩小2的倍数, 处理一次
        if (this is PixelValueUnit) {
            inScaleStep = 5.0
            deScaleStep = 1.0
        }
        return getGraduatedScaleGap(getGraduatedMinGap(scale), scale, inScaleStep, deScaleStep)
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

        val graduatedIndexGap = getGraduatedIndexGap(1f)
        if (index % graduatedIndexGap == 0) {
        } else if (index % (graduatedIndexGap / 2) == 0 && this is InchValueUnit) {
            return (pixel / value).unitDecimal(1)
        }
        return "${(pixel / value).roundToInt()}"
    }

    //endregion---刻度相关方法---

    //region---值转换相关方法---

    @Pixel
    fun convertValueToPixel(value: Float): Float

    /**获取每个单位间隔刻度对应的像素大小
     * 将1个单位的值, 转换成屏幕像素点数值
     * [TypedValue.COMPLEX_UNIT_MM]*/
    @Pixel
    fun convertValueToPixel(value: Double): Double = convertValueToPixel(value.toFloat()).toDouble()

    fun convertPixelToValue(pixel: Float): Float {
        val unit = convertValueToPixel(1.0f)
        val result = pixel / unit
        if (result.isFinite()) {
            return result
        }
        return result.nextDown()
    }

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
    fun formattedValueUnit(value: Double, ensureInt: Boolean = true): String

    fun formattedValueUnit(value: Float, ensureInt: Boolean = true): String

    /**获取描述的单位字符创*/
    fun getUnit(): String

    //endregion---值转换相关方法---
}

/**保留小数点后几位
 * [ensureInt] 如果是整数, 优先使用整数*/
fun Float.unitDecimal(
    digit: Int = 2,
    fadedUp: Boolean = true,
    ensureInt: Boolean = true
): String {
    return decimal(digit, ensureInt, fadedUp)
}

fun Double.unitDecimal(
    digit: Int = 2,
    fadedUp: Boolean = true,
    ensureInt: Boolean = true
): String {
    return decimal(digit, ensureInt, fadedUp)
}

/**[Float]*/
fun IValueUnit.convertPixelToValueUnit(pixel: Float?, ensureInt: Boolean = true): String {
    if (pixel == null) {
        return formattedValueUnit(0.0, ensureInt)
    }
    val value = convertPixelToValue(pixel)
    return formattedValueUnit(value, ensureInt)
}

/**[convertPixelToValue]
 * [formattedValueUnit]
 * [Double]*/
fun IValueUnit.convertPixelToValueUnit(pixel: Double?): String {
    if (pixel == null) {
        return formattedValueUnit(0.0)
    }
    val value = convertPixelToValue(pixel)
    return formattedValueUnit(value)
}

/**1毫米转换成像素*/
fun Int?.toPixel() = this?.toFloat().toPixel()

/**1毫米转像素*/
fun Float?.toPixel(unit: IValueUnit = MM_UNIT) = unit.convertValueToPixel(this ?: 0f)

/**1毫米转像素带单位*/
fun Float?.toPixelUnit(unit: IValueUnit = MM_UNIT) =
    unit.formattedValueUnit(unit.convertValueToPixel(this ?: 0f))

/**1毫米转像素*/
fun Double?.toPixel(unit: IValueUnit = MM_UNIT) = unit.convertValueToPixel(this ?: 0.0)

/**1像素转换成毫米
 * [com.angcyo.library.unit.MmValueUnit]
 * */
fun Int.toMm() = toFloat().toMm()

/**1像素转毫米*/
fun Float?.toMm() = MM_UNIT.convertPixelToValue(this ?: 0f)

/**1像素转毫米*/
fun Double?.toMm() = MM_UNIT.convertPixelToValue(this ?: 0.0)

fun Float?.toPt() = PointValueUnit().convertPixelToValue(this ?: 0f)

/**直接修改值*/
fun PointF.toMm() = this.apply {
    x = MM_UNIT.convertPixelToValue(x)
    y = MM_UNIT.convertPixelToValue(y)
}
