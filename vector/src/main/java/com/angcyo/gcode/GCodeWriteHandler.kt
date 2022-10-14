package com.angcyo.gcode

import com.angcyo.library.unit.InchValueUnit
import com.angcyo.vector.VectorWriteHandler

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/06/13
 */
class GCodeWriteHandler : VectorWriteHandler() {

    /**是否使用自动控制CNC, 即M03 M05使用M04*/
    var isAutoCnc = false

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
        writer?.appendLine("M8") //开启水冷系统
        writer?.appendLine("G1 F12000") //F进料速度
        if (isAutoCnc) {
            writer?.appendLine("M04 S255")
        }
        writer?.appendLine("G0 X${x} Y${y}")
    }

    override fun onPathEnd() {
        super.onPathEnd()
        closeCnc()
        if (isAutoCnc) {
            writer?.appendLine("S0 M5")
        }
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
            if (isAutoCnc) {
                //no op
            } else {
                writer?.appendLine("S0 M5")//S电压控制 M5关闭主轴
            }
            isClosedCnc = true
        }
    }

    /**打开CNC
     * M03:主轴打开*/
    fun openCnc() {
        if (isClosedCnc) {
            if (isAutoCnc) {
                //no op
            } else {
                writer?.appendLine("M03 S255")
            }
            isClosedCnc = false
        }
    }

    //endregion

}