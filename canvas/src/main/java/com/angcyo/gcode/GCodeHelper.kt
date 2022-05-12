package com.angcyo.gcode

import android.content.Context
import android.graphics.*
import android.util.DisplayMetrics
import android.util.TypedValue
import androidx.annotation.WorkerThread
import com.angcyo.canvas.core.CanvasEntryPoint
import com.angcyo.canvas.core.component.CanvasTouchHandler
import com.angcyo.canvas.utils.createPaint
import com.angcyo.library.L
import com.angcyo.library.ex.ceil
import com.angcyo.library.ex.dotDegrees
import kotlin.math.max
import kotlin.math.min

/**
 *
 * https://reprap.org/wiki/G-code/zh_cn
 * https://ncviewer.com/
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/12
 */
object GCodeHelper {

    //坐标单位对应的像素比例 (厘米, 英寸)
    private var _lastRatio = 1f

    /**修正GCode残缺指令
     * [1.6004 Y17.2065 I45.0088 J0.] -> [G2 X0 Y17.2065 I45.0088 J0.]
     * [1.6004 Y17.2065] -> [G1 X0 Y17.2065]
     * */
    var amendGCodeCmd: Boolean = true

    fun parseGCode(
        context: Context,
        text: String?,
        paint: Paint = createPaint(Color.BLACK)
    ): GCodeDrawable? {
        if (text.isNullOrEmpty()) {
            return null
        }
        //1毫米等于多少像素
        val dm: DisplayMetrics = context.resources.displayMetrics
        val mmPixel = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, 1f, dm) //21.176456

        //1英寸等于多少像素, 1英寸=2.54厘米=25.4毫米
        val inPixel = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_IN, 1f, dm) //537.882
        return parseGCode(text, mmPixel, inPixel, paint)
    }

    /**
     * [mmRatio] 毫米单位时, 需要放大的比例
     * [inRatio] 英寸单位时, 需要放大的比例
     * */
    @WorkerThread
    fun parseGCode(
        text: String,
        mmRatio: Float,
        inRatio: Float,
        paint: Paint = createPaint(Color.BLACK)
    ): GCodeDrawable {
        val gCodeLineDataList = mutableListOf<GCodeLineData>()
        _lastRatio = mmRatio // 默认使用毫米单位
        text.lines().forEach { line ->
            val gCodeLineData = _parseGCodeLine(line, mmRatio, inRatio)
            gCodeLineDataList.add(gCodeLineData)
        }
        return createGCodeDrawable(gCodeLineDataList, paint)
    }

    /**[GCodeDrawable]*/
    fun createGCodeDrawable(
        gCodeLineDataList: List<GCodeLineData>,
        paint: Paint = createPaint(Color.BLUE)
    ): GCodeDrawable {
        val gCodeHandler = GCodeHandler()
        val picture = gCodeHandler.parse(gCodeLineDataList, paint)
        return GCodeDrawable(picture).apply {
            gCodeBound.set(gCodeHandler.gCodeBounds)
        }
    }

    /**[GCodeLineData]*/
    fun _parseGCodeLine(line: String, mmRatio: Float, inRatio: Float): GCodeLineData {
        val cmdList = mutableListOf<GCodeCmd>()

        var ratio = _lastRatio

        //注释
        var comment: String? = null

        var cmdString: String = line
        val commentIndex = line.indexOf(";")
        if (commentIndex == -1) {
            //无注释
        } else {
            //有注释
            cmdString = line.substring(0, commentIndex)
            comment = line.substring(commentIndex + 1, line.length)
        }

        val cmdStringList = cmdString.split(' ')
        val lineFirstCmd: String = cmdStringList.firstOrNull() ?: ""
        cmdStringList.forEach { cmd ->
            if (cmd.isNotEmpty()) {
                val cmdBuilder = StringBuilder()
                val numberBuilder = StringBuilder()
                var isCmdChar: Boolean

                cmd.forEach { char ->
                    if (char.isGCodeCmdChar()) {
                        isCmdChar = true
                        cmdBuilder.append(char)
                    } else {
                        isCmdChar = false
                    }
                    if (!isCmdChar) {
                        numberBuilder.append(char)
                    }
                }

                val cmdStr = cmdBuilder.toString()
                //单位切换
                if (cmdStr == "G20") {
                    ratio = inRatio
                } else if (cmdStr == "G21") {
                    ratio = mmRatio
                }

                //转成数字后, 字符前面的0会丢失
                val number = numberBuilder.toString().toFloatOrNull() ?: 0f
                val gCodeCmd = if (lineFirstCmd.isGCodeMoveDirective()) {
                    GCodeCmd(cmd, cmdStr, number, ratio, ratio * number)
                } else {
                    GCodeCmd(cmd, cmdStr, number, ratio)
                }
                cmdList.add(gCodeCmd)
            }
        }
        _lastRatio = ratio

        //result
        val resultData = GCodeLineData(cmdString, cmdList, comment)
        if (amendGCodeCmd) {
            //修正指令
            val firstCmd = cmdList.firstOrNull()
            if (firstCmd != null) {
                if (firstCmd.cmd.isEmpty()) {
                    var newLine: String? = null
                    //第一个指令是空的
                    val x = resultData.getGCodeCmd("X")
                    val y = resultData.getGCodeCmd("Y")
                    val i = resultData.getGCodeCmd("I")
                    val j = resultData.getGCodeCmd("J")
                    if (i != null || j != null) {
                        //需要修正G2指令
                        newLine = buildString {
                            append("G2 ")
                            _fillCodeCmd(this, x, 'X')
                            _fillCodeCmd(this, y, 'Y')
                            _fillCodeCmd(this, i, 'I')
                            _fillCodeCmd(this, j, 'J')
                        }
                    } else if (x != null || y != null) {
                        //需要修正G1指令
                        newLine = buildString {
                            append("G1 ")
                            _fillCodeCmd(this, x, 'X')
                            _fillCodeCmd(this, y, 'Y')
                        }
                    }
                    if (!newLine.isNullOrEmpty()) {
                        return _parseGCodeLine(newLine, mmRatio, inRatio)
                    }
                }
            }
        }
        return resultData
    }

    fun _fillCodeCmd(builder: StringBuilder, cmd: GCodeCmd?, char: Char) {
        if (cmd == null) {
            builder.append("${char}0 ")
        } else {
            builder.append("${char}${cmd.number} ")
        }
    }

    /**处理类*/
    class GCodeHandler {

        val gCodeBounds = RectF()
        val path: Path = Path()

        //坐标单位是否是绝对位置, 否则就是相对位置, 相对于上一次的位置
        private var _isAbsolutePosition = true
        private var _isMoveTo = false
        private var _lastX = 0f
        private var _lastY = 0f

        /**入口, 开始解析[GCodeLineData]*/
        @CanvasEntryPoint
        fun parse(
            gCodeLineDataList: List<GCodeLineData>,
            paint: Paint = createPaint(Color.BLUE)
        ): Picture {
            parseGCodeBound(gCodeLineDataList)
            val picture = Picture().apply {
                //解析一行的数据
                gCodeLineDataList.forEach { lineData ->
                    parseGCodeLine(lineData)
                }

                //更新gCodeBounds
                val pathBounds = RectF()
                path.computeBounds(pathBounds, true)
                gCodeBounds.left = min(gCodeBounds.left, pathBounds.left)
                gCodeBounds.top = min(gCodeBounds.top, pathBounds.top)
                gCodeBounds.right = max(gCodeBounds.right, pathBounds.right)
                gCodeBounds.bottom = max(gCodeBounds.bottom, pathBounds.bottom)

                val canvas = beginRecording(
                    gCodeBounds.width().ceil().toInt(),
                    gCodeBounds.height().ceil().toInt()
                )
                canvas.translate(-gCodeBounds.left, -gCodeBounds.top)
                canvas.drawPath(path, paint)
                //结束
                endRecording()
            }
            return picture
        }

        /**解析GCode的bounds*/
        fun parseGCodeBound(gCodeLineDataList: List<GCodeLineData>) {
            var minX = 0f
            var maxX = 0f

            var minY = 0f
            var maxY = 0f

            gCodeLineDataList.forEach { line ->
                if (line.isGCodeMoveDirective()) {
                    val x = line.getGCodeX() ?: 0f
                    val y = line.getGCodeY() ?: 0f

                    minX = min(x, minX)
                    maxX = max(x, maxX)

                    minY = min(y, minY)
                    maxY = max(y, maxY)
                }
            }

            gCodeBounds.set(minX, minY, maxX, maxY)
        }

        fun parseGCodeLine(line: GCodeLineData) {
            val firstCmd = line.cmdList.firstOrNull()
            val firstCmdString = firstCmd?.cmd

            if (firstCmdString?.startsWith("G") == true) {
                //G指令
                val number = firstCmd.number.toInt()
                if (number == 0 || number == 1 || number == 2 || number == 3) {
                    //G0 G1 G2 G3
                    var x = line.getGCodeX()
                    var y = line.getGCodeY()

                    if (x == null || y == null) {
                        L.w("未找到x,y->${line.lineCode}")
                        return
                    }

                    if (!_isAbsolutePosition) {
                        x += _lastX
                        y += _lastY
                    }

                    when (number) {
                        0 -> { //G0
                            path.moveTo(x, y)
                            _onMoveTo(x, y)
                        }
                        1 -> { //G1
                            if (_isMoveTo) {
                                path.lineTo(x, y)
                                setLastLocation(x, y)
                            } else {
                                path.moveTo(x, y)
                                _onMoveTo(x, y)
                            }
                        }
                        2, 3 -> { //G2 G3, G2是顺时针圆弧移动，G3是逆时针圆弧移动。
                            if (_isMoveTo) {
                                val i = line.getGCodePixel("I") //圆心距离当前位置的x偏移量
                                val j = line.getGCodePixel("J") //圆心距离当前位置的y偏移量

                                if (i == null || j == null) {
                                    L.w("未找到i,j->${line.lineCode}")
                                    return
                                }

                                val circleX = _lastX + i
                                val circleY = _lastY + j

                                val r = CanvasTouchHandler.spacing(circleX, circleY, _lastX, _lastY)

                                val arcRect = RectF()
                                arcRect.set(circleX - r, circleY - r, circleX + r, circleY + r)

                                val lastPointAngle =
                                    CanvasTouchHandler.angle2(circleX, circleY, _lastX, _lastY)
                                val newPointAngle =
                                    CanvasTouchHandler.angle2(circleX, circleY, x, y)

                                val startAngle = if (number == 2) {
                                    //顺时针
                                    newPointAngle
                                } else {
                                    //逆时针
                                    lastPointAngle
                                }

                                //两个角度之间的角度
                                var sweepAngle = (newPointAngle - lastPointAngle).run {
                                    if (this < 0) {
                                        this + 360
                                    } else {
                                        this
                                    }
                                }

                                if (number == 2) {
                                    //顺时针
                                    sweepAngle = 360 - sweepAngle
                                }

                                path.addArc(arcRect, startAngle, sweepAngle)
                                dotDegrees(r, newPointAngle, circleX, circleY).apply {
                                    path.moveTo(this.x, this.y)
                                    _onMoveTo(this.x, this.y)
                                }
                            } else {
                                path.moveTo(x, y)
                                _onMoveTo(x, y)
                            }
                        }
                    }
                } else if (number == 90 || number == 91) {
                    //90: 绝对位置, 1: 相对位置
                    _isAbsolutePosition = number == 90
                } else {
                    L.w("忽略G指令:${line.lineCode}")
                }
            } else {
                L.w("跳过指令:${line.lineCode}")
            }
        }

        fun _onMoveTo(x: Float, y: Float) {
            _isMoveTo = true
            setLastLocation(x, y)
        }

        fun setLastLocation(x: Float, y: Float) {
            _lastX = x
            _lastY = y
        }
    }
}