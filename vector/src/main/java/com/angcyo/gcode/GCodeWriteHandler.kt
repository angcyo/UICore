package com.angcyo.gcode

import com.angcyo.library.unit.InchValueUnit
import com.angcyo.vector.VectorWriteHandler

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/06/13
 */
class GCodeWriteHandler : VectorWriteHandler() {

    /**是否关闭了CNC, 如果关闭了CNC所有G操作都变成G0操作*/
    var isClosedCnc = false

    override fun onPathStart(x: Float, y: Float) {
        //[G20]英寸单位 [G21]毫米单位
        //[G90]绝对位置 [G91]相对位置
        writer?.appendLine("G90")
        if (unit is InchValueUnit) {
            writer?.appendLine("G20")
        } else {
            writer?.appendLine("G21")
        }
        writer?.appendLine("G1 F12000")

        closeCnc()
        writer?.appendLine("G0 X${x} Y${y}")
    }

    override fun onPathEnd() {
        super.onPathEnd()
        closeCnc()
        writer?.append("G0 X0 Y0")
    }

    override fun onLineToPoint(x: Float, y: Float) {
        openCnc()
        writer?.appendLine("G1 X${x} Y${y}")
    }

    //region ---core---

    /**关闭CNC
     * M05指令:主轴关闭, M03:主轴打开*/
    fun closeCnc() {
        if (!isClosedCnc) {
            writer?.appendLine("M05 S0")
            isClosedCnc = true
        }
    }

    /**打开CNC
     * M03:主轴打开*/
    fun openCnc() {
        if (isClosedCnc) {
            writer?.appendLine("M03 S255")
            isClosedCnc = false
        }
    }

    //endregion

}