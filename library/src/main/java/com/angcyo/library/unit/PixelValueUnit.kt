package com.angcyo.library.unit

/**
 * 像素单位, px
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/05/18
 */
class PixelValueUnit : IValueUnit {

    /**10个像素为1个刻度距离*/
    override fun getGraduatedScaleGap(): Float = convertValueToPixel(1f) * 10

    override fun getGraduatedLabel(index: Int): String = "${(index * 10f).toInt()}"

    override fun convertValueToPixel(value: Float): Float = 1f

    override fun formattedValueUnit(value: Float): String = "${value.toInt()}px"
}