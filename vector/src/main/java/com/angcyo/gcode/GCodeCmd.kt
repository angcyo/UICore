package com.angcyo.gcode

/**
 * GCode 指令
 * 例如: G1 G0 X-7.050000 Y21.600000 等
 *
 * //1毫米等于多少像素
 * val dm: DisplayMetrics = resources.displayMetrics
 * val mmPixel = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, 1f, dm) //21.176456
 *
 * //1英寸等于多少像素, 1英寸=2.54厘米=25.4毫米
 * val inPixel = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_IN, 1f, dm) //537.882
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/12
 */
data class GCodeCmd(
    /**指令完整字符串, G21 G20*/
    val code: String,
    /**指令, 比如:G M X Y*/
    val cmd: String,
    /**指令跟随的数字*/
    val number: Double = 0.0,

    /**G指令时[number]数值需要放大的倍数, 受[G20]英寸单位 [G21]毫米单位 指令影响
     * [pixel] = [number] * [ratio] */
    val ratio: Double = 1.0,
    /**G指令时[number]对应的像素点单位*/
    val pixel: Double = 0.0
)

/**大写字母A~Z*/
fun Char.isGCodeCmdChar() = code in 65..90

/**当前指令是否是移动指令, 此指令一般和坐标有关*/
fun String.isGCodeMoveDirective(): Boolean {
    return startsWith("G")
}