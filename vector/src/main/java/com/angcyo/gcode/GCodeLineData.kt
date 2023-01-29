package com.angcyo.gcode

import com.angcyo.gcode.GCodeHelper.SPINDLE_AUTO
import com.angcyo.gcode.GCodeHelper.SPINDLE_OFF
import com.angcyo.gcode.GCodeHelper.SPINDLE_ON

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
    /**这一行的命令字符*/
    val cmdString: String,
    /**这一行的注释*/
    val comment: String? = null,

    //---

    /**[cmdString]分解的所有指令, 一行中的所有指令*/
    val cmdList: List<GCodeCmd>,
    /**是否全文中没有发现M03/04/05指令, 只在第一行数据中赋值
     * [com.angcyo.gcode.GCodeHelper.parseGCodeLineList]
     * */
    var notFoundMCmd: Boolean = false,
)

fun GCodeLineData.isGCodeMoveDirective(): Boolean {
    return cmdList.firstOrNull()?.cmd?.isGCodeMoveDirective() ?: false
}

/**是否是Gxx指令*/
fun GCodeLineData.isGCmd(): Boolean {
    return cmdList.find { it.cmd.startsWith("G") } != null
}

/**是否具有点位信息*/
fun GCodeLineData.haveXYZ(): Boolean {
    return cmdList.find { it.cmd.startsWith("X") || it.cmd.startsWith("Y") || it.cmd.startsWith("Z") } != null
}

fun GCodeLineData.getGCodeX(): Double? {
    return getGCodePixel("X")
}

fun GCodeLineData.getGCodeY(): Double? {
    return getGCodePixel("Y")
}

/**获取指令对应的像素值*/
fun GCodeLineData.getGCodePixel(cmd: String): Double? {
    return getGCodeCmd(cmd)?.pixel
}

fun GCodeLineData.getGCodeCmd(cmd: String): GCodeCmd? {
    return cmdList.find { it.cmd.startsWith(cmd) }
}

/**程序是否关闭, 比如到了文件尾部, 或者遇到了M2*/
fun GCodeLineData.isClose(): Boolean {
    return cmdList.find { it.cmd.startsWith("M", true) && it.number.toInt() == 2 } != null
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

/**主轴类型
 *  [SPINDLE_ON]
 *  [SPINDLE_OFF]
 *  [SPINDLE_AUTO]
 *  */
fun GCodeLineData.spindleType(def: Int? = null): Int? {
    var result = def
    cmdList.forEach { cmdData ->
        val cmdString = cmdData.cmd
        if (cmdString.startsWith("M", true)) {
            //M05指令:主轴关闭, M03:主轴打开 M04:自动
            result = when (cmdData.number.toInt()) {
                5 -> SPINDLE_OFF
                3 -> SPINDLE_ON
                //自动CNC
                4 -> SPINDLE_AUTO
                else -> def
            }
        }
    }
    return result
}