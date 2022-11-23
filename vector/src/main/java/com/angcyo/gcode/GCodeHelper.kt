package com.angcyo.gcode

import android.content.Context
import android.graphics.*
import android.util.DisplayMetrics
import android.util.TypedValue
import androidx.annotation.WorkerThread
import com.angcyo.library.L
import com.angcyo.library.annotation.CallPoint
import com.angcyo.library.annotation.Pixel
import com.angcyo.library.app
import com.angcyo.library.ex.*
import com.angcyo.library.model.PointD
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
    private var _lastRatio = 1.0

    /**主轴打开*/
    const val SPINDLE_ON = 3

    /**主轴关闭*/
    const val SPINDLE_OFF = 5

    /**主轴自动打开/关闭*/
    const val SPINDLE_AUTO = 4

    /**[GCodeParseConfig]*/
    fun gcodeParseConfig(context: Context, text: String): GCodeParseConfig {
        //1毫米等于多少像素
        val dm: DisplayMetrics = context.resources.displayMetrics
        val mmPixel = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, 1f, dm) //21.176456

        //1英寸等于多少像素, 1英寸=2.54厘米=25.4毫米
        val inPixel = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_IN, 1f, dm) //537.882

        val config = GCodeParseConfig(text, mmPixel.toDouble(), inPixel.toDouble())
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

    /**将GCode数据解析成一行一行的数据*/
    fun parseGCodeLineList(config: GCodeParseConfig): List<GCodeLineData> {
        val gCodeLineDataList = mutableListOf<GCodeLineData>()
        _lastRatio = config.mmRatio // 默认使用毫米单位

        var spindleType: Int? = null // M03/M05/M04 激光类型
        config.text.lines().forEach { line ->
            if (line.isNotBlank()) {
                //不为空
                val gCodeLineData = _parseGCodeLine(line, config.mmRatio, config.inRatio)
                gCodeLineDataList.add(gCodeLineData)

                if (spindleType == null) {
                    //还未找到激光类型指令
                    spindleType = gCodeLineData.spindleType()
                }
            }
        }

        if (spindleType == null) {
            //全文中未设置M指令, 则使用M04自动激光...
            gCodeLineDataList.firstOrNull()?.notFoundMCmd = true
        }
        return gCodeLineDataList
    }

    /**获取GCode的Bounds[RectF]*/
    fun parseGCodeBounds(gCode: String): RectF {
        val gCodeHandler = GCodeHandler()
        gCodeHandler.reset()
        val mm = MmValueUnit()
        val inch = InchValueUnit()
        val mmValue = mm.convertValueToPixel(1.0)
        val inchValue = inch.convertValueToPixel(1.0)
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
    fun _parseGCodeLine(line: String, mmRatio: Double, inRatio: Double): GCodeLineData {
        val cmdList = mutableListOf<GCodeCmd>()

        var ratio = _lastRatio

        //注释
        var comment: String? = null

        var cmdString: String = line//G00 G17 G40 G21 G54
        val commentIndex = line.indexOf(";")
        if (commentIndex == -1) {
            //无注释
        } else {
            //有注释
            cmdString = line.substring(0, commentIndex).uppercase()
            comment = line.substring(commentIndex + 1, line.length)
        }

        var cmdStringList = cmdString.split(' ')//G1  X81.3282 Y52.9104;有空格隔开的指令
        if (cmdStringList.size() <= 1) {
            if (cmdString.contains("X") ||
                cmdString.contains("Y") ||
                cmdString.contains("I") ||
                cmdString.contains("J")
            ) {
                val regex = "[A-z][-]?[\\d.]*\\d+"
                cmdStringList = cmdString.patternList(regex)//G1X83.4949Y-8.0145;无空格隔开的指令
            }
        }
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
                val number = numberBuilder.toString().toDoubleOrNull() ?: 0.0
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
        x: Double,
        y: Double,
        i: Double,
        j: Double,
        lastX: Double,
        lastY: Double
    ): PointD {
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
        private var _isMoveTo = true

        //上一次的xy的数据
        private var _lastX = 0.0
        private var _lastY = 0.0

        //上一次移动到的xy的数据
        private var _lastMoveX = 0.0
        private var _lastMoveY = 0.0

        //上一次xy的数据, 未[transformPoint]
        private var _lastOriginX = 0.0
        private var _lastOriginY = 0.0

        //临时存储[transformPoint]后的变量
        val _tempXYPoint = PointD()
        val _tempIJPoint = PointD()

        /**坐标点的转换*/
        var transformPoint: ((GCodeLineData, point: PointD) -> Unit)? = null

        /**重写G指令*/
        var overrideGCommand: ((line: GCodeLineData, firstCmd: GCodeCmd, xy: PointD, ij: PointD?) -> Unit)? =
            null

        /**重写其他非G指令*/
        var overrideCommand: ((line: GCodeLineData) -> Unit)? = null

        /**
         * M05指令:主轴关闭, M03:主轴打卡 M04自动
         * 自动从文件中提取提取, 如果文件中不包含M03/M05, 则自动使用M04
         * */
        private var spindleType: Int? = null

        fun reset() {
            _lastMoveX = 0.0
            _lastMoveY = 0.0
            _lastOriginX = 0.0
            _lastOriginY = 0.0
            _isAbsolutePosition = true
            _isMoveTo = true
            path.rewind()
            transformPoint = null
            overrideGCommand = null
            overrideCommand = null
            spindleType = null
        }

        /**初始化激光类型*/
        fun _initSpindleType(gCodeLineList: List<GCodeLineData>) {
            spindleType = if (gCodeLineList.firstOrNull()?.notFoundMCmd == true) {
                SPINDLE_AUTO
            } else {
                SPINDLE_OFF
            }
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
        fun parseGCodeBound(gCodeLineList: List<GCodeLineData>): RectF {
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
            _initSpindleType(gCodeLineList)

            for (lineData in gCodeLineList) {
                spindleType = lineData.spindleType(spindleType)
                parseGCodeLine(lineData, path)
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
            _initSpindleType(gCodeLineList)
            gCodeLineList.forEach { line ->
                spindleType = line.spindleType(spindleType)
                if (!parseGCodeLine(line)) {
                    //其他指令, 原封不动写入
                    overrideCommand?.invoke(line)
                }
            }
        }

        /**主轴关闭后, 所有的G指令操作, 都变成Move
         *  返回值表示是否处理了*/
        fun parseGCodeLine(line: GCodeLineData, toPath: Path = path): Boolean {
            val firstCmd = line.cmdList.firstOrNull()
            val firstCmdString = firstCmd?.cmd

            //主轴状态 主轴关闭后, 所有的G指令操作, 都变成Move
            var isSpindleOn = spindleType == SPINDLE_ON

            if (firstCmdString?.startsWith("G") == true) {
                //G指令
                val number = firstCmd.number.toInt()
                if (number == 0 || number == 1 || number == 2 || number == 3) {
                    //G0 G1 G2 G3
                    @Pixel
                    val _x = line.getGCodeX()

                    @Pixel
                    val _y = line.getGCodeY()

                    var x = _x
                    var y = _y

                    if (x == null && y == null) {
                        //同时为空时
                        return false
                    }

                    //如果某个为空, 则使用上一次的值
                    x = x ?: _lastX
                    y = y ?: _lastY

                    if (!_isAbsolutePosition) {
                        x += _lastMoveX
                        y += _lastMoveY
                    }

                    val originLastX = _lastOriginX
                    val originLastY = _lastOriginY

                    transformPoint(line, x, y, _tempXYPoint)
                    x = _tempXYPoint.x
                    y = _tempXYPoint.y

                    when (number) {
                        0 -> { //G0
                            isSpindleOn = if (spindleType == SPINDLE_AUTO) {
                                false
                            } else {
                                isSpindleOn
                            }

                            if (isSpindleOn) {
                                toPath.moveTo(x.toFloat(), y.toFloat())
                                _onMoveTo(x, y)
                            } else {
                                //主轴关闭的情况下, G0操作留到下一次G1进行
                                _isMoveTo = false
                                setLastLocation(x, y)
                            }
                            overrideGCommand?.invoke(line, firstCmd, _tempXYPoint, null)
                        }
                        1 -> { //G1
                            isSpindleOn = if (spindleType == SPINDLE_AUTO) {
                                true
                            } else {
                                isSpindleOn
                            }

                            if (isSpindleOn) {
                                if (!_isMoveTo) {
                                    toPath.moveTo(_lastMoveX.toFloat(), _lastMoveY.toFloat())
                                    _onMoveTo(_lastMoveX, _lastMoveY)
                                }
                                toPath.lineTo(x.toFloat(), y.toFloat())
                                setLastLocation(x, y)
                            } else {
                                _isMoveTo = false
                                setLastLocation(x, y)
                            }
                            overrideGCommand?.invoke(line, firstCmd, _tempXYPoint, null)
                        }
                        2, 3 -> { //G2 G3, G2是顺时针圆弧移动，G3是逆时针圆弧移动。
                            //x1 y1, x2, y2 经过i j未中心的圆弧

                            isSpindleOn = if (spindleType == SPINDLE_AUTO) {
                                true
                            } else {
                                isSpindleOn
                            }

                            if (isSpindleOn) {
                                @Pixel
                                var i = line.getGCodePixel("I") //圆心距离当前位置的x偏移量

                                @Pixel
                                var j = line.getGCodePixel("J") //圆心距离当前位置的y偏移量

                                if (i == null || j == null) {
                                    L.w("未找到i,j->${line.cmdString}")
                                    return false
                                }

                                if (!_isMoveTo) {
                                    toPath.moveTo(_lastMoveX.toFloat(), _lastMoveY.toFloat())
                                    _onMoveTo(_lastMoveX, _lastMoveY)
                                }

                                //之前的圆心
                                var circleX = originLastX + i
                                var circleY = originLastY + j

                                _tempIJPoint.set(circleX, circleY)
                                transformPoint?.invoke(line, _tempIJPoint)

                                //将圆心旋转缩放之后, 计算新的i j
                                i = _tempIJPoint.x - _lastMoveX
                                j = _tempIJPoint.y - _lastMoveY
                                _tempIJPoint.set(i, j)

                                overrideGCommand?.invoke(line, firstCmd, _tempXYPoint, _tempIJPoint)

                                circleX = _lastMoveX + i
                                circleY = _lastMoveY + j

                                //圆弧的半径
                                val r =
                                    VectorHelper.spacing(circleX, circleY, _lastMoveX, _lastMoveY)

                                //圆弧的矩形范围
                                val arcRect = emptyRectF()
                                arcRect.set(
                                    (circleX - r).toFloat(),
                                    (circleY - r).toFloat(),
                                    (circleX + r).toFloat(),
                                    (circleY + r).toFloat()
                                )

                                //圆弧的角度
                                val lastPointAngle =
                                    VectorHelper.angle2(circleX, circleY, _lastMoveX, _lastMoveY)
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

                                toPath.addArc(arcRect, startAngle.toFloat(), sweepAngle.toFloat())
                                dotDegrees(r, newPointAngle, circleX, circleY).apply {
                                    transformPoint(line, this.x, this.y, _tempXYPoint)
                                    val endX = _tempXYPoint.x
                                    val endY = _tempXYPoint.y

                                    toPath.moveTo(endX.toFloat(), endY.toFloat())
                                    _onMoveTo(endX, endY)
                                }
                            } else {
                                _isMoveTo = false
                                setLastLocation(x, y)
                            }
                        }
                    }

                    //last, 这里的xy需要是数据中的, 不能是转换(transform)后的
                    _lastX = _x ?: _lastX
                    _lastY = _y ?: _lastY
                    return true
                } else if (number == 90 || number == 91) {
                    //90: 绝对位置, 1: 相对位置
                    _isAbsolutePosition = number == 90
                } else if (number == 20 || number == 21) {
                    //20: 英寸单位, 21: 毫米单位
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

        fun _onMoveTo(x: Double, y: Double) {
            _isMoveTo = true
            setLastLocation(x, y)
        }

        fun setLastLocation(x: Double, y: Double) {
            _lastMoveX = x
            _lastMoveY = y
        }

        /**转换点坐标*/
        fun transformPoint(line: GCodeLineData, x: Double, y: Double, point: PointD) {
            _lastOriginX = x
            _lastOriginY = y
            point.set(x, y)
            transformPoint?.invoke(line, point)
        }
    }
}