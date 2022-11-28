package com.angcyo.library.unit

/**
 * 像素单位, px
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/05/18
 */
class PixelValueUnit : IValueUnit {

    override fun convertValueToPixel(value: Double): Double = 1.0

    override fun formattedValueUnit(value: Double): String = "${value.toInt()}${getUnit()}"

    override fun getUnit(): String = "px"
}