package com.angcyo.library.unit

/**
 * 像素单位, px
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/05/18
 */
class PixelValueUnit : IValueUnit {

    /**10个像素为1个刻度距离*/
    override fun getGraduatedScaleGap(): Double = convertValueToPixel(1.0) * 10

    override fun getGraduatedLabel(index: Int): String = "${(index * 10f).toInt()}"

    override fun convertValueToPixel(value: Double): Double = 1.0

    override fun formattedValueUnit(value: Double): String = "${value.toInt()}${getUnit()}"

    override fun getUnit(): String = "px"
}