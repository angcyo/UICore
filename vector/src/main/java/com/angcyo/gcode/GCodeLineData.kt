package com.angcyo.gcode

/**
 * GCode 每一行的数据
 * 例如: G0 X-7.050000 Y21.600000
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/12
 */
data class GCodeLineData(
    /**整行的数据=[cmdString]+[comment]*/
    val lineCode: String,
    /**只包含命令字符*/
    val cmdString: String,
    /**[cmdString]分解的所有指令, 一行中的所有指令*/
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

/**最后一条有效指令的索引*/
fun List<GCodeLineData>.lastValidIndex(): Int {
    for ((index, line) in withIndex().reversed()) {
        if (line.cmdList.isNotEmpty()) {
            return index
        }
    }
    return -1
}

/**主轴是否打开*/
fun GCodeLineData.isSpindleOn(def: Boolean): Boolean {
    val firstCmd = cmdList.firstOrNull()
    val firstCmdString = firstCmd?.cmd
    if (firstCmdString?.startsWith("M") == true) {
        var isSpindleOn = true //M05指令:主轴关闭, M03:主轴打开
        val number = firstCmd.number.toInt()
        when (number) {
            5 -> isSpindleOn = false
            3 -> isSpindleOn = true
            4 -> {
                //自动CNC
            }
        }
        return isSpindleOn
    }
    return def
}