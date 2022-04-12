package com.angcyo.gcode

/**
 * GCode 每一行的数据
 * 例如: G0 X-7.050000 Y21.600000
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/12
 */
data class GCodeLineData(
    /**一行中的所有指令*/
    val list: List<GCodeCmd>,
    /**这一行的注释*/
    val comment: String? = null
)

fun GCodeLineData.isGCodeMoveDirective(): Boolean {
    return list.firstOrNull()?.cmd?.isGCodeMoveDirective() ?: false
}

fun GCodeLineData.getGCodeX(): Float {
    return list.find { it.cmd.startsWith("X") }?.pixel ?: 0f
}

fun GCodeLineData.getGCodeY(): Float {
    return list.find { it.cmd.startsWith("Y") }?.pixel ?: 0f
}