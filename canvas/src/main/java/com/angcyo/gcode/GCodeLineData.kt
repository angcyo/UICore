package com.angcyo.gcode

/**
 * GCode 每一行的数据
 * 例如: G0 X-7.050000 Y21.600000
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/12
 */
data class GCodeLineData(
    /**整行的数据*/
    val lineCode: String,
    /**一行中的所有指令*/
    val cmdList: List<GCodeCmd>,
    /**这一行的注释*/
    val comment: String? = null
)

fun GCodeLineData.isGCodeMoveDirective(): Boolean {
    return cmdList.firstOrNull()?.cmd?.isGCodeMoveDirective() ?: false
}

/**是否具有点位信息*/
fun GCodeLineData.haveXYZ(): Boolean {
    return cmdList.find { it.cmd.startsWith("X") || it.cmd.startsWith("Y") || it.cmd.startsWith("Z") } != null
}

fun GCodeLineData.getGCodeX(): Float? {
    return getGCodePixel("X")
}

fun GCodeLineData.getGCodeY(): Float? {
    return getGCodePixel("Y")
}

/**获取指令对应的像素值*/
fun GCodeLineData.getGCodePixel(cmd: String): Float? {
    return getGCodeCmd(cmd)?.pixel
}

fun GCodeLineData.getGCodeCmd(cmd: String): GCodeCmd? {
    return cmdList.find { it.cmd.startsWith(cmd) }
}
