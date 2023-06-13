package com.angcyo.gcode

import android.graphics.Path
import com.angcyo.library.component.hawk.LibLpHawkKeys
import com.angcyo.library.ex.toLossyFloat
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

    /**追加的点位信息是否是像素
     *
     * 采样的时候用像素, 但是写入的时候用mm单位
     * 通常在图片转GCode的时候使用
     * */
    var isPixelValue = false

    /**是否激活GCode压缩指令*/
    var enableGCodeShrink: Boolean = LibLpHawkKeys.enableGCodeShrink

    //上一次的信息
    private var lastInfo: GCodeLastInfo = GCodeLastInfo()

    override fun onPathStart() {
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
    }

    override fun onPathEnd(isPathFinish: Boolean) {
        super.onPathEnd(isPathFinish)
        closeCnc()
        if (isAutoCnc) {
            writer?.appendLine("S0 M5")
        }
        if (isPathFinish) {
            //整个路径结束
            if (LibLpHawkKeys.enableGCodeEndG0) {
                writer?.appendLine("G0 X0 Y0")
            }
            writer?.append("M2") //程序结束
        }
    }

    override fun onNewPoint(x: Double, y: Double) {
        if (isAutoCnc) {
            writer?.appendLine("S255")
        } else {
            closeCnc()
        }

        var xValue = x
        var yValue = y
        if (isPixelValue && unit != null) {
            xValue = unit?.convertPixelToValue(x) ?: x
            yValue = unit?.convertPixelToValue(y) ?: y
        }

        val cmd = "G0"
        val xFloat = xValue.toLossyFloat()
        val yFloat = yValue.toLossyFloat()
        if (enableGCodeShrink) {
            writer?.appendLine(buildString {
                var isFirst = false
                if (lastInfo.lastCmd != cmd) {
                    isFirst = true
                    append(cmd)
                }
                if (isFirst || lastInfo.lastX != xFloat) {
                    append("X${xFloat.toValueString()}")
                }
                if (isFirst || lastInfo.lastY != yFloat) {
                    append("Y${yFloat.toValueString()}")
                }
            })
        } else {
            writer?.appendLine(buildString {
                append(cmd)
                append(" X${xFloat.toValueString()}")
                append(" Y${yFloat.toValueString()}")
            })
        }
        lastInfo.lastCmd = cmd
        lastInfo.lastX = xFloat
        lastInfo.lastY = yFloat
    }

    override fun onLineToPoint(point: VectorPoint) {
        if (point.pointType == POINT_TYPE_CIRCLE) {
            //原
            val first = _pointList.firstOrNull()
            val circle = first?.circle
            if (first == null || circle == null) {
                super.onLineToPoint(point)
            } else {
                //G2支持
                openCnc()

                var iValue = circle.x - first.x
                var jValue = circle.y - first.y

                var xValue = point.x
                var yValue = point.y
                if (isPixelValue && unit != null) {
                    xValue = unit?.convertPixelToValue(point.x) ?: point.x
                    yValue = unit?.convertPixelToValue(point.y) ?: point.y

                    iValue = (unit?.convertPixelToValue(circle.x)
                        ?: circle.x) - (unit?.convertPixelToValue(first.x) ?: first.x)
                    jValue = (unit?.convertPixelToValue(circle.y)
                        ?: circle.y) - (unit?.convertPixelToValue(first.y) ?: first.y)
                }
                //`G2` 顺时针画弧 -> Path.Direction.CCW
                //`G3` 逆时针画弧 -> Path.Direction.CW

                val gcode = buildString {
                    val cmd = if (first.circleDir == Path.Direction.CW) {
                        "G3"
                    } else {
                        "G2"
                    }
                    val x = xValue.toLossyFloat()
                    val y = yValue.toLossyFloat()
                    val i = iValue.toLossyFloat()
                    val j = jValue.toLossyFloat()

                    if (enableGCodeShrink) {
                        var isFirst = false
                        if (lastInfo.lastCmd != cmd) {
                            isFirst = true
                            append(cmd)
                        }
                        if (isFirst || lastInfo.lastX != x) {
                            append("X${x.toValueString()}")
                        }
                        if (isFirst || lastInfo.lastY != y) {
                            append("Y${y.toValueString()}")
                        }
                        if (isFirst || lastInfo.lastI != i) {
                            append("I${i.toValueString()}")
                        }
                        if (isFirst || lastInfo.lastJ != j) {
                            append("J${j.toValueString()}")
                        }
                    } else {
                        append("$cmd ")
                        append("X${x.toValueString()} Y${y.toValueString()} ")
                        append("I${i.toValueString()} J${j.toValueString()}")
                    }

                    lastInfo.lastCmd = cmd
                    lastInfo.lastX = x
                    lastInfo.lastY = y
                    lastInfo.lastI = i
                    lastInfo.lastJ = j
                }

                if (gcode.isNotBlank()) {
                    writer?.appendLine(gcode)
                }
            }
        } else {
            super.onLineToPoint(point)
        }
    }

    override fun onLineToPoint(x: Double, y: Double) {
        openCnc()

        var xValue = x
        var yValue = y
        if (isPixelValue && unit != null) {
            xValue = unit?.convertPixelToValue(x) ?: x
            yValue = unit?.convertPixelToValue(y) ?: y
        }

        if (enableGCodeShrink) {
            val cmd = "G1"
            val xFloat = xValue.toLossyFloat()
            val yFloat = yValue.toLossyFloat()
            val gcode = buildString {
                var isFirst = false
                if (lastInfo.lastCmd != cmd) {
                    isFirst = true
                    append(cmd)
                }
                if (isFirst || lastInfo.lastX != xFloat) {
                    append("X${xFloat.toValueString()}")
                }
                if (isFirst || lastInfo.lastY != yFloat) {
                    append("Y${yFloat.toValueString()}")
                }
            }
            if (gcode.isNotBlank()) {
                writer?.appendLine(gcode)
            }
            lastInfo.lastCmd = cmd
            lastInfo.lastX = xFloat
            lastInfo.lastY = yFloat
        } else {
            writer?.appendLine(buildString {
                append("G1")
                append(" X${xValue.toLossyFloat().toValueString()}")
                append(" Y${yValue.toLossyFloat().toValueString()}")
            })
        }
    }

    //region ---core---

    /**关闭CNC
     * M05指令:主轴关闭, M03:主轴打开*/
    fun closeCnc() {
        if (!isClosedCnc) {
            if (isAutoCnc) {
                //no op
            } else {
                writer?.appendLine("M05 S0")//S电压控制 M5关闭主轴
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