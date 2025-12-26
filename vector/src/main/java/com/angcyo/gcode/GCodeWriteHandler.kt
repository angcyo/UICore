package com.angcyo.gcode

import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import com.angcyo.library.annotation.MM
import com.angcyo.library.annotation.Pixel
import com.angcyo.library.annotation.Unit
import com.angcyo.library.component.hawk.LibLpHawkKeys
import com.angcyo.library.component.pool.acquireTempPath
import com.angcyo.library.component.pool.acquireTempPointF
import com.angcyo.library.component.pool.release
import com.angcyo.library.ex.eachPath
import com.angcyo.library.ex.toLossyFloat
import com.angcyo.library.unit.InchValueUnit
import com.angcyo.library.unit.toMm
import com.angcyo.vector.VectorWriteHandler

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/06/13
 */

const val kGCodeSpace = " "

const val kGCodeFooter = "M9\nM5\nG0S0\nM2\n"

class GCodeWriteHandler : VectorWriteHandler() {

    companion object {
        @MM
        const val DEFAULT_CUT_WIDTH = 0.3f

        @MM
        const val DEFAULT_CUT_HEIGHT = 0.03f

        fun gcodeHeader(
            power: Int? = null,
            speed: Int? = null,
            auto: Boolean = false,
            space: String = kGCodeSpace
        ): String {
            val actualPower = power ?: 255
            val actualSpeed = speed ?: 12000
            // 灰度图片, 通过这个最大功率, 和每个点的当前功率, 计算出当前的灰度值
            return "G90\nG21\nM5\n${if (auto) "M4" else "M3"}${space}S$actualPower\nG0${space}F$actualSpeed\n"
        }

        /// GCode 尾部
        fun gcodeFooter(auto: Boolean = false): String = kGCodeFooter
    }

    /**是否要收集点位信息, 开启后[_collectPointList]集合会有数据
     * 0x30的数据类型时要的点位信息
     * */
    var isCollectPoint = false

    /**返回收集到的所有点集合*/
    var _collectPointList: MutableList<CollectPoint>? = null

    /**是否使用自动控制CNC, 即M03/M05使用M04*/
    var isAutoCnc = false

    /**自动激光下, 是否设置了功率参数*/
    var isSetPower = false

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

    /**是否激活GCode切割数据算法, 螺旋数据
     * ```
     * A->B => A->B B->A A->B
     * ```
     * */
    var enableGCodeCut: Boolean = false
        set(value) {
            field = value
            if (value) {
                isSinglePath = true
            }
        }

    /**GCode切割数据循环次数*/
    var cutLoopCount: Int = 1

    /**切割数据的宽度*/
    @MM
    var cutGCodeWidth = DEFAULT_CUT_WIDTH

    /**切割数据的高度*/
    @MM
    var cutGCodeHeight = DEFAULT_CUT_HEIGHT

    /**切割数据限制范围*/
    @Pixel
    var cutLimitRect: RectF? = null

    /**是否需要使用M2关闭gcode文件*/
    var needCloseGcodeFile = true

    /**GCode结束是否需要使用G0移动到原点*/
    var needMoveToOrigin = LibLpHawkKeys.enableGCodeEndG0

    /**强制指定gcode头/尾字符串, 不为null生效*/
    var gcodeHeader: String? = null
    var gcodeFooter: String? = null

    /**强制指定开关激光字符串,不为null生效*/
    var turnOn: String? = null
    var turnOff: String? = null

    //上一次的信息
    private var lastInfo: GCodeLastInfo = GCodeLastInfo()

    fun writeGCodeHeader(writer: Appendable?) {
        //[G20]英寸单位 [G21]毫米单位
        //[G90]绝对位置 [G91]相对位置
        writer?.appendLine("G90")
        if (unit is InchValueUnit) {
            writer?.appendLine("G20")
        } else {
            writer?.appendLine("G21")
        }
        writer?.appendLine("M8") //开启水冷系统
        if (isAutoCnc) {
            //自动激光 S255F12000 需要放在第一个G1指令后面
            writer?.appendLine("M04")
        } else {
            writer?.appendLine("G1F12000") //F进料速度
        }
        //文件头
        writer?.appendLine(";gcode_header")
    }

    override fun onPathStart() {
        isSetPower = false
        if (gcodeHeader != null) {
            if (gcodeHeader!!.isNotEmpty()) {
                writer?.appendLine(gcodeHeader!!)
            }
        } else {
            writeGCodeHeader(writer)
        }
    }

    fun writeGCodeFooter(writer: Appendable?) {
        if (isAutoCnc) {
            writer?.appendLine("S0")
        }
        //文件头
        writer?.appendLine(";gcode_footer")
        //整个路径结束
        if (needMoveToOrigin) {
            writer?.appendLine("G0 X0 Y0")
        }
        if (needCloseGcodeFile) {
            writer?.appendLine("M2") //程序结束
        }
    }

    override fun onPathEnd(isPathFinish: Boolean) {
        super.onPathEnd(isPathFinish)
        closeCnc()
        if (gcodeFooter != null) {
            if (gcodeFooter!!.isNotEmpty()) {
                writer?.appendLine(gcodeFooter!!)
            }
        } else {
            if (isAutoCnc) {
                writer?.appendLine("S0")
            }
            if (isPathFinish) {
                writeGCodeFooter(writer)
            }
        }
    }

    override fun onNewPoint(x: Double, y: Double) {
        if (!isAutoCnc) {
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
                    append("X${xFloat.toFloatValueString()}")
                }
                if (isFirst || lastInfo.lastY != yFloat) {
                    append("Y${yFloat.toFloatValueString()}")
                }
            })
        } else {
            writer?.appendLine(buildString {
                append(cmd)
                append(" X${xFloat.toFloatValueString()}")
                append(" Y${yFloat.toFloatValueString()}")
            })
        }
        lastInfo.lastCmd = cmd
        lastInfo.lastX = xFloat
        lastInfo.lastY = yFloat

        //2023-10-23
        collectPoint(true, x, y)
    }

    override fun onLineToPoint(point: VectorPoint) {
        if (enableGCodeCut) {
            for (i in 0 until cutLoopCount) {
                //fillCutGCodeByZ(lastWriteX, lastWriteY, point.x, point.y)
                fillCutGCodeByCircle(lastWriteX, lastWriteY, point.x, point.y)
                if (i != cutLoopCount - 1) {
                    //多次循环数据的话, 需要移动到起点
                    onNewPoint(lastWriteX, lastWriteY)
                }
            }
        } else {
            _lineToPoint(point)
        }
    }

    private fun _lineToPoint(point: VectorPoint) {
        if (point.pointType == POINT_TYPE_CIRCLE) {
            //原
            val first = _pointList.firstOrNull()
            val circle = first?.circle
            if (first == null || circle == null) {
                onLineToPoint(point.x, point.y)
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
                            append("X${x.toFloatValueString()}")
                        }
                        if (isFirst || lastInfo.lastY != y) {
                            append("Y${y.toFloatValueString()}")
                        }
                        if (isFirst || lastInfo.lastI != i) {
                            append("I${i.toFloatValueString()}")
                        }
                        if (isFirst || lastInfo.lastJ != j) {
                            append("J${j.toFloatValueString()}")
                        }
                        if (!isSetPower) {
                            append("S255F12000")
                            isSetPower = true
                        }
                    } else {
                        append("$cmd ")
                        append("X${x.toFloatValueString()} Y${y.toFloatValueString()} ")
                        append("I${i.toFloatValueString()} J${j.toFloatValueString()}")
                        if (!isSetPower) {
                            append(" S255F 12000")
                            isSetPower = true
                        }
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
            onLineToPoint(point.x, point.y)
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
                    append("X${xFloat.toFloatValueString()}")
                }
                if (isFirst || lastInfo.lastY != yFloat) {
                    append("Y${yFloat.toFloatValueString()}")
                }
                if (!isSetPower) {
                    append("S255F12000")
                    isSetPower = true
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
                append(" X${xValue.toDoubleValueString()}")
                append(" Y${yValue.toDoubleValueString()}")
                if (!isSetPower) {
                    append(" S255 F12000")
                    isSetPower = true
                }
            })
        }

        //2023-10-23
        collectPoint(false, xValue, yValue)
    }

    /**添加一个GCode圆
     * [startX] [startY] 起始点坐标
     * [cx] [cy] 圆心坐标
     * [diameter] 圆的直径
     * */
    fun circleTo(@MM cx: Double, @MM cy: Double, @MM diameter: Double) {
        val radius = diameter / 2
        val startX = cx - radius
        val startY = cy - radius

        var startXValue = startX
        var startYValue = startY
        var iValue = radius
        var jValue = radius

        if (isPixelValue && unit != null) {
            startXValue = unit?.convertPixelToValue(startX) ?: startX
            startYValue = unit?.convertPixelToValue(startY) ?: startY
            iValue = unit?.convertPixelToValue(iValue) ?: iValue
            jValue = unit?.convertPixelToValue(jValue) ?: jValue
        }

        @Unit
        val xStartStr = startXValue.toDoubleValueString()
        val yStartStr = startYValue.toDoubleValueString()
        val iValueStr = iValue.toDoubleValueString()
        val jValueStr = jValue.toDoubleValueString()

        val tempPointF = acquireTempPointF()

        writer?.apply {
            closeCnc()
            if (enableGCodeShrink) {
                appendLine("G0X${xStartStr}Y$yStartStr")
            } else {
                appendLine("G0 X$xStartStr Y$yStartStr")
            }
            openCnc()
            if (enableGCodeShrink) {
                append("G2X${xStartStr}Y${yStartStr}I${iValueStr}J${jValueStr}")
            } else {
                append("G2 X$xStartStr Y$yStartStr I${iValueStr} J${jValueStr}")
            }
            if (!isSetPower) {
                if (enableGCodeShrink) {
                    append("S255F12000")
                } else {
                    append(" S255 F12000")
                }
                isSetPower = true
            }
            appendLine()
            if (isCollectPoint) {
                collectPoint(true, startXValue, startYValue)

                @MM
                val path = acquireTempPath()
                path.addCircle(cx.toFloat(), cy.toFloat(), radius.toFloat(), Path.Direction.CW)

                path.eachPath(pathStep) { _, _, _, posArray, _ ->
                    @MM
                    val x = posArray[0]
                    val y = posArray[1]
                    tempPointF.set(x, y)
                    limitCutPoint(tempPointF)
                    collectPoint(false, tempPointF.x.toDouble(), tempPointF.y.toDouble())
                }

                path.release()
            }
        }

        tempPointF.release()

        //清空上一次指令信息
        lastInfo.clear()
    }

    //region ---core---

    fun writeTurnOff(writer: Appendable?) {
        if (!isAutoCnc) {
            writer?.appendLine("M05S0")
        }
    }

    /**关闭CNC
     * M05指令:主轴关闭, M03:主轴打开
     * [onPathEnd]
     * */
    fun closeCnc() {
        isSetPower = false
        if (!isClosedCnc) {
            if (isAutoCnc) {
                //no op
                //writer?.appendLine("S0") 在一段路径结束之后, 才关闭激光
            } else {
                if (turnOff != null) {
                    if (turnOff!!.isNotEmpty()) {
                        writer?.appendLine(turnOff!!)
                    }
                } else {
                    //writer?.appendLine("M05S0")//S电压控制 M05关闭主轴
                    writeTurnOff(writer)
                }
            }
            isClosedCnc = true
        }
    }

    fun writeTurnOn(writer: Appendable?) {
        if (!isAutoCnc) {
            writer?.appendLine("M03S255")
        }
    }

    /**打开CNC
     * M03:主轴打开*/
    fun openCnc() {
        if (isClosedCnc) {
            if (isAutoCnc) {
                //no op
            } else {
                if (turnOn != null) {
                    if (turnOn!!.isNotEmpty()) {
                        writer?.appendLine(turnOn!!)
                    }
                } else {
                    writeTurnOn(writer)
                }
            }
            isClosedCnc = false
        }
    }

    //endregion

    /**填充切割数据
     * 圆形填充算法*/
    fun fillCutGCodeByCircle(
        @MM startX: Double,
        @MM startY: Double,
        @MM endX: Double,
        @MM endY: Double
    ) {
        //圆的直径
        val diameter = cutGCodeWidth
        //圆心移动步长
        val step = cutGCodeHeight

        val path = Path()
        path.moveTo(startX.toFloat(), startY.toFloat())
        path.lineTo(endX.toFloat(), endY.toFloat())

        path.eachPath(step) { index, ratio, contourIndex, posArray, _ ->
            circleTo(posArray[0].toDouble(), posArray[1].toDouble(), diameter.toDouble())
        }
    }

    /**填充切割数据
     * Z字填充算法*/
    /*fun fillCutGCodeByZ(startX: Double, startY: Double, endX: Double, endY: Double) {
        //计算2个点之间的角度
        val angle = angle(startX, startY, endX, endY)

        val width = cutGCodeWidth
        val stepHeight = cutGCodeHeight

        val reverseMatrix = acquireTempMatrix()
        val tempPointF = acquireTempPointF()

        val rotateAngle = (90f - angle).toFloat()
        reverseMatrix.setRotate(-rotateAngle, startX.toFloat(), startY.toFloat())

        tempPointF.set(endX.toFloat(), endY.toFloat())
        tempPointF.rotate(rotateAngle, startX.toFloat(), startY.toFloat())

        val startX = startX - width / 2
        val startY = startY
        val endX = tempPointF.x + width / 2 + 0.0
        val endY = tempPointF.y + 0.0

        //计算填充角度
        val fillAngle = Math.toDegrees(atan(stepHeight / width).toDouble())

        //使用指定角度的线填充矩形
        var x = startX
        var y = startY

        while (y <= endY) {
            tempPointF.set(x.toFloat(), y.toFloat())
            reverseMatrix.mapPoint(tempPointF)
            limitCutPoint(tempPointF)

            if (x == startX && y == startY) {
                //第一个点
                onLineToPoint(tempPointF.x.toDouble(), tempPointF.y.toDouble())
                x = endX
                continue
            }
            onLineToPoint(tempPointF.x.toDouble(), tempPointF.y.toDouble())

            if (x >= endX) {
                x = startX
            } else {
                x = endX
            }
            y += tan(Math.toRadians(fillAngle)).toFloat() * width
        }
        tempPointF.release()
        reverseMatrix.release()
    }*/

    private fun limitCutPoint(@MM point: PointF) {
        cutLimitRect?.let {
            var left = it.left
            var top = it.top
            var right = it.right
            var bottom = it.bottom
            if (isPixelValue && unit != null) {
                left = unit?.convertPixelToValue(left) ?: left
                top = unit?.convertPixelToValue(top) ?: top
                right = unit?.convertPixelToValue(right) ?: right
                bottom = unit?.convertPixelToValue(bottom) ?: bottom
            } else {
                left = left.toMm()
                top = top.toMm()
                right = right.toMm()
                bottom = bottom.toMm()
            }

            point.x = point.x.coerceIn(left, right)
            point.y = point.y.coerceIn(top, bottom)
        }
    }

    //region ---CollectPoint---

    private fun collectPoint(newPoint: Boolean, x: Double, y: Double) {
        if (isCollectPoint) {
            if (_collectPointList == null) {
                _collectPointList = mutableListOf()
            }

            if (newPoint) {
                _collectPointList?.add(CollectPoint())
            }

            _collectPointList?.lastOrNull()?.pointList?.add(PointF(x.toFloat(), y.toFloat()))
        }
    }

    //endregion ---CollectPoint---

}