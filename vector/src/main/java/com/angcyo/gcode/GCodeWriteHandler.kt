package com.angcyo.gcode

import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import com.angcyo.library.annotation.MM
import com.angcyo.library.component.hawk.LibLpHawkKeys
import com.angcyo.library.component.pool.acquireTempMatrix
import com.angcyo.library.component.pool.acquireTempPath
import com.angcyo.library.component.pool.acquireTempPointF
import com.angcyo.library.component.pool.release
import com.angcyo.library.ex.angle
import com.angcyo.library.ex.eachPath
import com.angcyo.library.ex.mapPoint
import com.angcyo.library.ex.rotate
import com.angcyo.library.ex.toLossyFloat
import com.angcyo.library.unit.InchValueUnit
import com.angcyo.vector.VectorWriteHandler
import kotlin.math.atan
import kotlin.math.tan

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/06/13
 */
class GCodeWriteHandler : VectorWriteHandler() {

    companion object {
        @MM
        const val DEFAULT_CUT_WIDTH = 0.3f

        @MM
        const val DEFAULT_CUT_HEIGHT = 0.03f
    }

    /**是否要收集点位信息, 开启后[_collectPointList]集合会有数据
     * 0x30的数据类型时要的点位信息
     * */
    var isCollectPoint = false

    /**返回收集到的所有点集合*/
    var _collectPointList: MutableList<CollectPoint>? = null

    /**是否使用自动控制CNC, 即M03/M05使用M04*/
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

    /**是否激活GCode切割数据算法
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
    var cutLimitRect: RectF? = null

    /**是否需要使用M2关闭gcode文件*/
    var needCloseGcodeFile = true

    /**GCode结束是否需要使用G0移动到原点*/
    var needMoveToOrigin = false

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
        if (isPathFinish) {
            //整个路径结束
            if (needMoveToOrigin) {
                writer?.appendLine("G0 X0 Y0")
            }
            if (needCloseGcodeFile) {
                writer?.appendLine("M2") //程序结束
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
                    } else {
                        append("$cmd ")
                        append("X${x.toFloatValueString()} Y${y.toFloatValueString()} ")
                        append("I${i.toFloatValueString()} J${j.toFloatValueString()}")
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

        val xStartStr = startXValue.toDoubleValueString()
        val yStartStr = startYValue.toDoubleValueString()
        val iValueStr = iValue.toDoubleValueString()
        val jValueStr = jValue.toDoubleValueString()

        writer?.apply {
            closeCnc()
            appendLine("G0 X$xStartStr Y$yStartStr")
            openCnc()
            appendLine("G2 X$xStartStr Y$yStartStr I${iValueStr} J${jValueStr}")
            if (isCollectPoint) {
                collectPoint(true, startXValue, startYValue)

                val path = acquireTempPath()
                path.addCircle(cx.toFloat(), cy.toFloat(), radius.toFloat(), Path.Direction.CW)

                path.eachPath(pathStep) { _, _, _, posArray, _ ->
                    var x = posArray[0]
                    var y = posArray[1]

                    if (isPixelValue && unit != null) {
                        x = unit?.convertPixelToValue(x) ?: x
                        y = unit?.convertPixelToValue(y) ?: y
                    }
                    collectPoint(false, x.toDouble(), y.toDouble())
                }

                path.release()
            }
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
                writer?.append("M05")//S电压控制 M05关闭主轴
            }
            writer?.appendLine("S0")
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
                writer?.append("M03")
            }
            writer?.appendLine("S255")
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
    fun fillCutGCodeByZ(startX: Double, startY: Double, endX: Double, endY: Double) {
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
    }

    private fun limitCutPoint(point: PointF) {
        cutLimitRect?.let {
            point.x = point.x.coerceIn(it.left, it.right)
            point.y = point.y.coerceIn(it.top, it.bottom)
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