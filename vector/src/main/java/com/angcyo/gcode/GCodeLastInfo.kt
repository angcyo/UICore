package com.angcyo.gcode

/**
 * 用来显示压缩GCode指令
 * [com.angcyo.library.component.hawk.LibLpHawkKeys.enableGCodeShrink]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/01/09
 */
data class GCodeLastInfo(
    var lastCmd: String? = null, //上一次的G指令 G1 G2
    var lastX: Float? = null,  //上一次的X
    var lastY: Float? = null,  //上一次的Y
    var lastI: Float? = null,  //上一次的I
    var lastJ: Float? = null,  //上一次的J
) {
    fun clear() {
        lastCmd = null
        lastX = null
        lastY = null
        lastI = null
        lastJ = null
    }
}
