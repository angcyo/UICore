package com.angcyo.gcode

import android.content.Context
import android.graphics.*
import android.util.DisplayMetrics
import android.util.TypedValue
import androidx.annotation.WorkerThread
import com.angcyo.library.L
import com.angcyo.library.annotation.CallPoint
import com.angcyo.library.app
import com.angcyo.library.ex.ceil
import com.angcyo.library.ex.dotDegrees
import com.angcyo.library.ex.emptyRectF
import com.angcyo.library.unit.InchValueUnit
import com.angcyo.library.unit.MmValueUnit
import com.angcyo.vector.VectorHelper

/**
 *
 * https://reprap.org/wiki/G-code/zh_cn
 * https://ncviewer.com/
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/12
 */


/**

# GCode 常用指令

- `G20` 英寸单位
- `G21` 毫米单位
- ---
- `G90` 绝对位置
- `G91` 相对位置
- ---
- `G0` moveTo
- `G1` lineTo
- `G2` 顺时针画弧
- `G3` 逆时针画弧
- ---
- `M05` 关闭主轴,所有`G`操作, 都变成`moveTo`
- `M03` 打开主轴

 * */
object GCodeHelper {

    //坐标单位对应的像素比例 (厘米, 英寸)
    private var _lastRatio = 1f

    /**修正GCode残缺指令
     * [1.6004 Y17.2065 I45.0088 J0.] -> [G2 X0 Y17.2065 I45.0088 J0.]
     * [1.6004 Y17.2065] -> [G1 X0 Y17.2065]
     * */
    var amendGCodeCmd: Boolean = true

    /**[GCodeParseConfig]*/
    fun gcodeParseConfig(context: Context, text: String): GCodeParseConfig {
        //1毫米等于多少像素
        val dm: DisplayMetrics = context.resources.displayMetrics
        val mmPixel = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, 1f, dm) //21.176456

        //1英寸等于多少像素, 1英寸=2.54厘米=25.4毫米
        val inPixel = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_IN, 1f, dm) //537.882

        val config = GCodeParseConfig(text, mmPixel, inPixel)
        return config
    }

    @WorkerThread
    fun parseGCode(
        text: String?,
        paint: Paint,
        context: Context = app(),
    ): GCodeDrawable? {
        if (text.isNullOrEmpty()) {
            return null
        }
        return parseGCode(gcodeParseConfig(context, text), paint)
    }

    /**将GCode转换成[Path]*/
    @WorkerThread
    fun parseGCodeToPath(text: String?, context: Context = app()): Path? {
        if (text.isNullOrEmpty()) {
            return null
        }
        val config = gcodeParseConfig(context, text)
        val gCodeHandler = GCodeHandler()
        gCodeHandler.parseGCodeBound(parseGCodeLineList(config))
        return gCodeHandler.path
    }

    fun parseGCodeLineList(config: GCodeParseConfig): List<GCodeLineData> {
        val gCodeLineDataList = mutableListOf<GCodeLineData>()
        _lastRatio = config.mmRatio // 默认使用毫米单位
        config.text.lines().forEach { line ->
            val gCodeLineData = _parseGCodeLine(line, config.mmRatio, config.inRatio)
            gCodeLineDataList.add(gCodeLineData)
        }
        return gCodeLineDataList
    }

    /**获取GCode的Bounds[RectF]*/
    fun parseGCodeBounds(gCode: String): RectF {
        val gCodeHandler = GCodeHandler()
        gCodeHandler.reset()
        val mm = MmValueUnit()
        val inch = InchValueUnit()
        val mmValue = mm.convertValueToPixel(1f)
        val inchValue = inch.convertValueToPixel(1f)
        val config = GCodeParseConfig(gCode, mmValue, inchValue)
        val gCodeLineList = parseGCodeLineList(config)
        return gCodeHandler.parseGCodeBound(gCodeLineList)
    }

    /**
     * [mmRatio] 毫米单位时, 需要放大的比例
     * [inRatio] 英寸单位时, 需要放大的比例
     * */
    @WorkerThread
    fun parseGCode(config: GCodeParseConfig, paint: Paint): GCodeDrawable? {
        return createGCodeDrawable(parseGCodeLineList(config), paint)
    }

    /**[GCodeDrawable]*/
    fun createGCodeDrawable(
        gCodeLineDataList: List<GCodeLineData>,
        paint: Paint
    ): GCodeDrawable? {
        val gCodeHandler = GCodeHandler()
        val picture = gCodeHandler.parsePicture(gCodeLineDataList, paint) ?: return null
        return GCodeDrawable(picture).apply {
            gCodeBound.set(gCodeHandler.gCodeBounds)
            gCodeData = buildString {
                gCodeLineDataList.forEachIndexed { index, gCodeLineData ->
                    if (index != 0) {
                        appendLine()
                    }
                    append(gCodeLineData.lineCode)
                }
            }
            gCodePath.set(gCodeHandler.path)
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
        val resultData = GCodeLineData(line, cmdString, cmdList, comment)
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

    /**计算绘制圆弧后的结束点坐标*/
    private fun calcArcEndPoint(
        x: Float,
        y: Float,
        i: Float,
        j: Float,
        lastX: Float,
        lastY: Float
    ): PointF {
        val circleX = lastX + i
        val circleY = lastY + j
        val r = VectorHelper.spacing(circleX, circleY, lastX, lastY)
        val newPointAngle = VectorHelper.angle2(circleX, circleY, x, y)
        return dotDegrees(r, newPointAngle, circleX, circleY)
    }

    /**处理类*/
    class GCodeHandler {

        /**像素单位*/
        val gCodeBounds = emptyRectF()
        val path: Path = Path()

        //坐标单位是否是绝对位置, 否则就是相对位置, 相对于上一次的位置
        private var _isAbsolutePosition = true
        private var _isMoveTo = false

        //上一次xy的数据
        private var _lastX = 0f
        private var _lastY = 0f

        //上一次xy的数据, 未[transformPoint]
        private var _lastOriginX = 0f
        private var _lastOriginY = 0f

        //临时存储[transformPoint]后的变量
        val _tempXYPoint = PointF()
        val _tempIJPoint = PointF()

        /**坐标点的转换*/
        var transformPoint: ((GCodeLineData, point: PointF) -> Unit)? = null

        /**重写G指令*/
        var overrideGCommand: ((firstCmd: GCodeCmd, xy: PointF, ij: PointF?) -> Unit)? = null

        /**重写其他非G指令*/
        var overrideCommand: ((line: GCodeLineData) -> Unit)? = null

        fun reset() {
            _lastX = 0f
            _lastY = 0f
            _lastOriginX = 0f
            _lastOriginY = 0f
            _isAbsolutePosition = true
            _isMoveTo = true
            path.rewind()
            transformPoint = null
            overrideGCommand = null
            overrideCommand = null
        }

        /**入口, 开始解析[GCodeLineData]*/
        @CallPoint
        fun parsePicture(
            gCodeLineDataList: List<GCodeLineData>,
            paint: Paint
        ): Picture? {
            parseGCodeBound(gCodeLineDataList)//bound
            if (gCodeBounds.width() <= 0 || gCodeBounds.height() <= 0) {
                //无大小
                return null
            }
            val picture = Picture().apply {
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
        fun parseGCodeBound(gCodeLineDataList: List<GCodeLineData>): RectF {
            /*var minX = 0f
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

            //这种方式计算出来的bound有误差, 因为G2 G3指令是圆弧
            gCodeBounds.set(minX, minY, maxX, maxY)*/

            //解析一行的数据
            var isSpindleOn = true //M05指令:主轴关闭, M03:主轴打卡
            for (lineData in gCodeLineDataList) {
                isSpindleOn = lineData.isSpindleOn(isSpindleOn)
                parseGCodeLine(lineData, isSpindleOn, path)
            }

            path.computeBounds(gCodeBounds, true)

            /*//更新gCodeBounds
            val pathBounds = emptyRectF()
            path.computeBounds(pathBounds, true)
            gCodeBounds.left = min(gCodeBounds.left, pathBounds.left)
            gCodeBounds.top = min(gCodeBounds.top, pathBounds.top)
            gCodeBounds.right = max(gCodeBounds.right, pathBounds.right)
            gCodeBounds.bottom = max(gCodeBounds.bottom, pathBounds.bottom)*/

            return gCodeBounds
        }

        /**解析所有GCode数据*/
        fun parseGCodeLineList(gCodeLineList: List<GCodeLineData>) {
            var isSpindleOn = true //M05指令:主轴关闭, M03:主轴打卡
            gCodeLineList.forEach { line ->
                isSpindleOn = line.isSpindleOn(isSpindleOn)
                if (!parseGCodeLine(line, isSpindleOn)) {
                    //其他指令, 原封不动写入
                    overrideCommand?.invoke(line)
                }
            }
        }

        /**[isSpindleOn] 主轴是否开启; 主轴关闭后, 所有的G指令操作, 都变成Move
         * 返回值表示是否处理了*/
        fun parseGCodeLine(
            line: GCodeLineData,
            isSpindleOn: Boolean,
            toPath: Path = path
        ): Boolean {
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
                        //L.v("未找到x,y->${line.lineCode}")
                        return false
                    }

                    if (!_isAbsolutePosition) {
                        x += _lastX
                        y += _lastY
                    }

                    val originLastX = _lastOriginX
                    val originLastY = _lastOriginY

                    transformPoint(line, x, y, _tempXYPoint)
                    x = _tempXYPoint.x
                    y = _tempXYPoint.y

                    when (number) {
                        0 -> { //G0
                            toPath.moveTo(x, y)
                            _onMoveTo(x, y)
                            overrideGCommand?.invoke(firstCmd, _tempXYPoint, null)
                        }
                        1 -> { //G1
                            if (isSpindleOn && _isMoveTo) {
                                toPath.lineTo(x, y)
                                setLastLocation(x, y)
                            } else {
                                toPath.moveTo(x, y)
                                _onMoveTo(x, y)
                            }
                            overrideGCommand?.invoke(firstCmd, _tempXYPoint, null)
                        }
                        2, 3 -> { //G2 G3, G2是顺时针圆弧移动，G3是逆时针圆弧移动。
                            //x1 y1, x2, y2 经过i j未中心的圆弧
                            if (isSpindleOn && _isMoveTo) {
                                var i = line.getGCodePixel("I") //圆心距离当前位置的x偏移量
                                var j = line.getGCodePixel("J") //圆心距离当前位置的y偏移量

                                if (i == null || j == null) {
                                    L.w("未找到i,j->${line.cmdString}")
                                    return false
                                }

                                //之前的圆心
                                var circleX = originLastX + i
                                var circleY = originLastY + j

                                _tempIJPoint.set(circleX, circleY)
                                transformPoint?.invoke(line, _tempIJPoint)

                                //将圆心旋转缩放之后, 计算新的i j
                                i = _tempIJPoint.x - _lastX
                                j = _tempIJPoint.y - _lastY
                                _tempIJPoint.set(i, j)

                                overrideGCommand?.invoke(firstCmd, _tempXYPoint, _tempIJPoint)

                                circleX = _lastX + i
                                circleY = _lastY + j

                                //圆弧的半径
                                val r = VectorHelper.spacing(circleX, circleY, _lastX, _lastY)

                                //圆弧的矩形范围
                                val arcRect = emptyRectF()
                                arcRect.set(circleX - r, circleY - r, circleX + r, circleY + r)

                                //圆弧的角度
                                val lastPointAngle =
                                    VectorHelper.angle2(circleX, circleY, _lastX, _lastY)
                                val newPointAngle =
                                    VectorHelper.angle2(circleX, circleY, x, y)

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

                                toPath.addArc(arcRect, startAngle, sweepAngle)
                                dotDegrees(r, newPointAngle, circleX, circleY).apply {
                                    transformPoint(line, this.x, this.y, _tempXYPoint)
                                    val endX = _tempXYPoint.x
                                    val endY = _tempXYPoint.y

                                    toPath.moveTo(endX, endY)
                                    _onMoveTo(endX, endY)
                                }
                            } else {
                                toPath.moveTo(x, y)
                                _onMoveTo(x, y)
                            }
                        }
                    }
                    return true
                } else if (number == 90 || number == 91) {
                    //90: 绝对位置, 1: 相对位置
                    _isAbsolutePosition = number == 90
                } else {
                    L.v("忽略G指令:${line.cmdString}")
                }
            } else if (firstCmdString?.startsWith("M") == true) {
                //no op
            } else {
                L.v("跳过指令:${line.cmdString}")
            }
            return false
        }

        fun _onMoveTo(x: Float, y: Float) {
            _isMoveTo = true
            setLastLocation(x, y)
        }

        fun setLastLocation(x: Float, y: Float) {
            _lastX = x
            _lastY = y
        }

        /**转换点坐标*/
        fun transformPoint(line: GCodeLineData, x: Float, y: Float, point: PointF) {
            _lastOriginX = x
            _lastOriginY = y
            point.set(x, y)
            transformPoint?.invoke(line, point)
        }
    }
}