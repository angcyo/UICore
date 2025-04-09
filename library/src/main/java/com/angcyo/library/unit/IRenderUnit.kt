package com.angcyo.library.unit

import com.angcyo.library.annotation.Pixel
import kotlin.math.nextDown

/**
 * 渲染的单位毫米(mm) 英寸(inch)
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/14
 */
interface IRenderUnit {

    companion object {

        /**正常的刻度*/
        const val AXIS_TYPE_NORMAL = 0x1

        /**次要的刻度*/
        const val AXIS_TYPE_SECONDARY = AXIS_TYPE_NORMAL shl 1

        /**主要的刻度*/
        const val AXIS_TYPE_PRIMARY = AXIS_TYPE_SECONDARY shl 1

        /**需要绘制Label的刻度*/
        const val AXIS_TYPE_MASK = 0xff

        /**需要绘制Label的刻度*/
        const val AXIS_TYPE_LABEL = 0b100000000
    }

    //region---绘制相关方法---

    /**获取单位对应的小数点后几位
     * [formatValue]*/
    fun getDecimal(): Int = 2

    /**获取描述的单位字符串*/
    fun getUnit(): String

    /**在指定的缩放比例[scale]情况下, 每个刻度之间的间隙像素值*/
    @Pixel
    fun getGap(scale: Float): Float

    /**获取当前第几个刻度[index] (从0开始), 的绘制类型*/
    fun getRenderType(index: Int, scale: Float): Int {
        return when {
            index % 10 == 0 -> AXIS_TYPE_PRIMARY or AXIS_TYPE_LABEL
            index % 5 == 0 -> AXIS_TYPE_SECONDARY
            else -> AXIS_TYPE_NORMAL
        }
    }

    //endregion---绘制相关方法---

    //region---转换相关方法---

    /**将对应单位的值, 转换成像素*/
    @Pixel
    fun convertValueToPixel(value: Float): Float

    /**将像素值,转换成对应的值*/
    fun convertPixelToValue(@Pixel pixel: Float): Float {
        val unit = convertValueToPixel(1.0f)
        val result = pixel / unit
        if (result.isFinite()) {
            return result
        }
        return result.nextDown()
    }

    /**格式化值[value]
     * [ensureInt] 是否要确保整数, 比如: 15.0 转换成 15
     * [unit] 是否需要返回单位*/
    fun formatValue(value: Float, ensureInt: Boolean, unit: Boolean): String {
        val valueStr = value.unitDecimal(getDecimal(), true, ensureInt)
        if (unit) {
            return valueStr + getUnit()
        }
        return valueStr
    }

    //endregion---转换相关方法---
}