package com.angcyo.library.unit

import kotlin.math.roundToInt

/**
 * 像素单位, px
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/05/18
 */
class PixelValueUnit : IValueUnit {

    override fun convertValueToPixel(value: Float): Float = value

    override fun formattedValueUnit(value: Double, ensureInt: Boolean): String =
        "${value.roundToInt()}${getUnit()}"

    override fun formattedValueUnit(value: Float, ensureInt: Boolean): String =
        "${value.roundToInt()}${getUnit()}"

    override fun getUnit(): String = "px"
}