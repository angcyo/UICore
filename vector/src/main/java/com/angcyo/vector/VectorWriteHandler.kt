package com.angcyo.vector

import android.graphics.Path
import android.graphics.PointF
import android.os.Build
import com.angcyo.gcode.GCodeWriteHandler
import com.angcyo.library.L
import com.angcyo.library.annotation.CallPoint
import com.angcyo.library.annotation.Flag
import com.angcyo.library.annotation.MM
import com.angcyo.library.annotation.Pixel
import com.angcyo.library.annotation.Private
import com.angcyo.library.component.hawk.LibHawkKeys
import com.angcyo.library.component.hawk.LibLpHawkKeys
import com.angcyo.library.component.pool.acquireTempPath
import com.angcyo.library.component.pool.acquireTempRectF
import com.angcyo.library.component.pool.release
import com.angcyo.library.ex.*
import com.angcyo.library.model.PointD
import com.angcyo.library.unit.IValueUnit
import com.angcyo.library.unit.MmValueUnit
import com.angcyo.library.unit.PixelValueUnit
import kotlin.math.absoluteValue
import kotlin.math.tan

/**
 * 矢量输出基类,
 *
 * svg 移动用 M
 * gcode 移动用 G0
 *
 * svg 连接用 L
 * gcode 连接用 G1
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/10/13
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
abstract class VectorWriteHandler {

    companion object {

        /**如果2点之间的间隙大于此值, 则使用G0指令 厘米单位
         * 0.5mm.
         * 1K:0.1 2K:0.05 4K:0.025f
         * */
        @MM
        const val PATH_SPACE_GAP = 0.15f

        /**不推荐直接关闭GAP, 因为浮点运算会有一定的误差*/
        @MM
        const val PATH_SPACE_GAP_MIN = 0.01f

        /**Path的采样率,采样率越高, 间隙越小, 越清晰*/
        const val PATH_SPACE_1K = 0.1f
        const val PATH_SPACE_2K = 0.05f
        const val PATH_SPACE_4K = 0.025f

        /**路径填充类型, 矩形扫描*/
        const val PATH_FILL_TYPE_RECT = 1

        /**路径填充类型, 圆形扫描*/
        const val PATH_FILL_TYPE_CIRCLE = 2

        /**添加的路径, 默认的方向
         * [Path.Direction.CW] 顺时针, X从右开始 Y↓扫描
         * [Path.Direction.CCW] 逆时针, X从右开始 Y↑扫描
         * */
        val DEFAULT_PATH_DIRECTION = Path.Direction.CW

        //---

        /**是一个重新开始的, 那么之前的数据需要G1先连接, 然后G0到新的点*/
        @Flag("点位类型")
        internal const val POINT_TYPE_NEW = 1

        /**当前的点和上个点是一样的, 则抹除上一次的最后点信息*/
        @Flag("点位类型")
        internal const val POINT_TYPE_SAME = 2

        /**当和上一个点不一样, 但是又在可允许的范围内时, 则把之前的数据G1连起来,
         * 并且替换最后一个点为第一个点,而不是擦除所有点位信息*/
        @Flag("点位类型")
        internal const val POINT_TYPE_GAP = 3

        /**多个点在圆上*/
        @Flag("点位类型")
        internal const val POINT_TYPE_CIRCLE = 4

        /**下一个点在新的圆上*/
        @Flag("点位类型")
        internal const val POINT_TYPE_NEW_CIRCLE = 5
    }

    //---

    /**输出写入器*/
    var writer: Appendable? = null

    /**单位转换器, 如果不设置则[Path]的值, 1:1输出
     * [com.angcyo.gcode.GCodeWriteHandler.isPixelValue]*/
    var unit: IValueUnit? = null

    /**路径枚举时的最小间隙
     * [unit]*/
    @MM
    var pathStep: Float = 0.01f

    /**真实值, 可以是像素, 也可以是mm. 根据[writePoint]的值, 自行决定
     *
     * 间隔太长, 就会使用G0移动到过
     * 当2个点之间的距离小于此值时, 视为同一个点
     *
     * 负数表示关闭Gap判断, 全部使用G1
     * */
    @MM
    var gapValue: Float = PATH_SPACE_GAP

    /**当距离上一个点距离大于这个值时,
     * 则连接之前的最后一个点,并且抹除点位信息.
     *
     * 类似于2点之间的间隙
     * */
    @MM
    var gapMaxValue: Float = PATH_SPACE_GAP * 2

    /**多少度误差之内, 视为同一个点*/
    var angleGapValue: Float = 0.5f

    /**路径填充类型*/
    var pathFillType: Int = PATH_FILL_TYPE_RECT

    /**保留小数点后几位*/
    var decimal = LibHawkKeys.vectorDecimal

    /**是否是简单的路径, 如果是则全程使用G1连接, 否则智能通过Gap判断
     * 此属性会关闭弧度采样判断
     * */
    var isSinglePath: Boolean = false

    //---弧度采样---

    /**[LibLpHawkKeys.enableVectorRadiansSample]
     * [LibHawkKeys.pathSampleStepRadians]
     * [LibHawkKeys.pathAcceptableDegrees]
     * */
    var enableVectorRadiansSample: Boolean = false

    /**在弧度模式下, 采样的间隙, 只影响采样性能, 不影响数据精度*/
    @Pixel
    var pathSampleStepRadians: Float = LibHawkKeys.pathSampleStepRadians

    /**公差 跟着[unit]单位*/
    var pathTolerance: Float = LibHawkKeys.defPathTolerance

    //---

    /**是否关闭Gap, 则所有的点都直接用G1/L*/
    val isCloseGap: Boolean
        get() = gapValue <= 0f

    /**记录*/
    val _pointList = mutableListOf<VectorPoint>()

    val _refGapValue: Float
        get() = pathStep + gapValue

    val _refGapMaxValue: Float
        get() = pathStep + gapMaxValue

    /**最后一次写入的点*/
    protected var lastWriteX: Double = 0.0
    protected var lastWriteY: Double = 0.0

    //---

    fun updatePathStepByPixel(pixel: Float) {
        pathStep = unit?.convertPixelToValue(pixel) ?: pixel
    }

    fun updateGapValueByPixel(pixel: Float) {
        gapValue = unit?.convertPixelToValue(pixel) ?: pixel
        updateGapMaxValueByPixel(2 * pixel)
    }

    fun updateGapMaxValueByPixel(pixel: Float) {
        gapMaxValue = unit?.convertPixelToValue(pixel) ?: pixel
    }

    fun updatePathToleranceByPixel(pixel: Float) {
        pathTolerance = unit?.convertPixelToValue(pixel) ?: pixel
    }

    //region ---Core回调---

    /**更新单位, 并且*/
    open fun updateUnit(unit: IValueUnit?) {
        this.unit = unit
        if (unit is MmValueUnit) {
            gapValue = PATH_SPACE_GAP
            gapMaxValue = PATH_SPACE_GAP * 2
        } else if (unit == null || unit is PixelValueUnit) {
            gapValue = 1f
            gapMaxValue = 1f
        }
    }

    /**[Path]的起点
     * GCode 数据的一些初始化配置
     * Svg 数据的M操作
     * */
    @CallPoint
    open fun onPathStart() {

    }

    /**[Path]的终点
     * [isPathFinish] [Path]路径是否全部结束, 有可能只是其中一段结束了*/
    @CallPoint
    open fun onPathEnd(isPathFinish: Boolean) {
        clearLastPoint()
    }

    /**
     * 产生的一个新点, 则应该先把之前的数据G1, 然后新数据G0
     * GCode G0
     * SVG M
     * */
    open fun onNewPoint(x: Double, y: Double) {

    }

    /**需要连接到点
     * GCode 用G1, 支持G2
     * SVG 用L*/
    open fun onLineToPoint(point: VectorPoint) {
        onLineToPoint(point.x, point.y)
    }

    @Deprecated(
        "请使用[onLineToPoint]",
        replaceWith = ReplaceWith("this.onLineToPoint(VectorPoint)")
    )
    open fun onLineToPoint(x: Double, y: Double) {

    }

    //endregion ---Core回调---

    //region ---Core---

    fun Double.toDoubleValueString() = toFloat().toFloatValueString()

    fun Float.toFloatValueString() = decimal(decimal, true, false, true)

    /**清理上一次最后的点, 通常在遇到新的点时调用*/
    fun clearLastPoint() {
        if (_pointList.size() > 1) {
            //如果有旧数据
            val last = _pointList.last()
            onLineToPoint(last)
        }
        _pointList.clear()//重置集合
    }

    /**连接到最后一个点, 并且将最后一个点设置为第一个点*/
    open fun lineLastPoint(fromPoint: VectorPoint) {
        if (_pointList.isNotEmpty()) {
            //如果有旧数据
            val last = _pointList.last()
            onLineToPoint(last)
            _pointList.clear()
            _pointList.add(last)
        }
    }

    //add value, 并且只留2个值
    @Private
    fun _resetLastPoint(point: VectorPoint) {
        if (_pointList.size >= 2) {
            _pointList.removeLastOrNull()
        }
        _pointList.add(point)
    }

    /**计算角度偏移*/
    fun _angleDiff(newX: Double, newY: Double): Double {
        return if (_pointList.size() > 1) {
            val first = _pointList.first()
            val last = _pointList.last()
            val a1 = VectorHelper.angle(first.x, first.y, last.x, last.y)
            val a2 = VectorHelper.angle(last.x, last.y, newX, newY)
            return (a2 - a1).absoluteValue
        } else {
            -1.0
        }
    }

    /**判断当前的点, 和之前的点是否是同一角度*/
    @Private
    fun _isSameAngle(newX: Double, newY: Double): Boolean {
        return if (_pointList.size() > 1) {
            _angleDiff(newX, newY) < angleGapValue
        } else {
            false
        }
    }

    /**计算当前的点, 和上一个点的类型*/
    open fun _valueChangedType(x: Double, y: Double, radians: Float?): Int {
        if (enableVectorRadiansSample) {
            val first = _pointList.firstOrNull() ?: return POINT_TYPE_NEW //之前没有点, 那当前的点肯定是最新的
            //弧度采样情况下不支持G2/G3输出
            return _valueChangedType(first, x, y, radians)
        } else {
            val last = _pointList.lastOrNull() ?: return POINT_TYPE_NEW //之前没有点, 那当前的点肯定是最新的
            val pointType = _valueChangedType(last, x, y, radians) //当前点的类型
            if (_pointList.size() == 1) {
                //之前只有1个点
                return pointType
            } else {
                //之前已经有多个点
                if (LibLpHawkKeys.enableVectorArc && _pointList.size() == 2) {
                    //之前有2个点, 现在是第3个点, 则判断3个点是否是在圆上
                    val first = _pointList.first()
                    val x1 = first.x
                    val y1 = first.y
                    val x2 = _pointList[1].x
                    val y2 = _pointList[1].y
                    val cPoint = VectorHelper.centerOfCircle(x1, y1, x2, y2, x, y)
                    if (cPoint == null) {
                        //不在圆上
                    } else {
                        //在圆上, 则将圆心坐标写入第一个点的对象中
                        val circle = first.circle
                        if (circle == null) {
                            first.circle = cPoint

                            val a1 = VectorHelper.angle2(x2, y2, cPoint.x, cPoint.y)
                            val a2 = VectorHelper.angle2(x, y, cPoint.x, cPoint.y)

                            first.circleDir = if (a2 >= a1) {
                                //角度在变大, 顺时针枚举点位
                                Path.Direction.CW
                            } else {
                                //角度在变小, 逆时针枚举点位
                                Path.Direction.CCW
                            }
                        } else {
                            //判断和之前是否在同一个圆上
                            val circleType = _valueChangedType(
                                VectorPoint(circle.x, circle.y, POINT_TYPE_CIRCLE),
                                cPoint.x,
                                cPoint.y,
                                radians
                            )
                            return if (circleType == POINT_TYPE_SAME) {
                                //圆心一致, 则
                                POINT_TYPE_CIRCLE
                            } else {
                                //圆心不一致, 则使用新的点
                                if (pointType == POINT_TYPE_GAP || pointType == POINT_TYPE_SAME) {
                                    //圆心不一致, 但是采样点是在临界类, 则需要先LineTo, 并清理状态
                                    POINT_TYPE_NEW_CIRCLE
                                } else {
                                    POINT_TYPE_NEW
                                }
                            }
                        }
                        return POINT_TYPE_CIRCLE
                    }
                }

                //val firstType = _valueChangedType(first, x, y) //第一个点的类型
                val lastType = pointType //最后一个点的类型

                if (lastType == POINT_TYPE_GAP) {
                    if (_isSameAngle(x, y)) {
                        //如果角度一致, 那视为相同的点
                        return POINT_TYPE_SAME
                    }
                } else if (lastType == POINT_TYPE_SAME) {
                    if (!_isSameAngle(x, y)) {
                        //如果视为相同的点,但是角度不一致, 则之前的点需要G1过去
                        return POINT_TYPE_GAP
                    }
                }
                return lastType
            }
        }
    }

    fun _valueChangedType(first: VectorPoint, x: Double, y: Double, radians: Float?): Int {
        if (enableVectorRadiansSample && radians != null && first.radians != null) {
            //公差采样
            val c = c(first.x, first.y, x, y).toFloat()
            val h = tan((radians - first.radians!!).absoluteValue / 4) * c / 2
            return if (h.absoluteValue >= pathTolerance) {
                POINT_TYPE_GAP
            } else {
                POINT_TYPE_SAME
            }
        }
        val c = c(first.x, first.y, x, y).toFloat()
        if (((first.x - x).absoluteValue > _refGapMaxValue ||
                    (first.y - y).absoluteValue > _refGapMaxValue)
            && c > _refGapMaxValue
        ) {
            //2点之间间隙太大, 则视为新的点
            return POINT_TYPE_NEW
        }
        if (first.x == x || first.y == y) {
            //在一根线上
            return POINT_TYPE_SAME
        }
        if (isCloseGap) {
            //关闭了gap, 则直接G1之前的点, 否则可能是一样的值, 直接忽略
            return POINT_TYPE_GAP
        }
        if (c < _refGapValue) {
            return POINT_TYPE_SAME
        }
        return POINT_TYPE_GAP
    }

    /**创建一个新的点*/
    open fun generatePoint(x: Double, y: Double, radians: Float?): VectorPoint {
        return if (isSinglePath) {
            VectorPoint(
                x, y,
                if (_pointList.lastOrNull() == null) POINT_TYPE_NEW else POINT_TYPE_GAP,
                radians = radians
            )
        } else {
            VectorPoint(x, y, _valueChangedType(x, y, radians), radians = radians)
        }
    }

    /**
     * 追加一个点, 如果这个点和上一点属于相同类型的点, 则不追加,
     * 继续等待下一个点, 直到不同类型的点出现
     *
     * 写入G0 或者 G1 指令. 会自动处理[CNC]
     * 只支持横向/纵向的点坐标转成G1, 不支持斜向.
     *
     * 请主动调用[writeFirst] [writeFinish]
     *
     * [x] [y] 非像素值, 真实值
     * [radians] 当前点的弧度
     *
     * 写入的点会根据[unit]进行转换
     * */
    @CallPoint
    open fun writePoint(x: Double, y: Double, radians: Float?) {
        val point = generatePoint(x, y, radians)

        //last
        val first = _pointList.firstOrNull()
        lastWriteX = first?.x ?: 0.0
        lastWriteY = first?.y ?: 0.0

        when (point.pointType) {
            POINT_TYPE_NEW_CIRCLE -> {
                _pointList.lastOrNull()?.let { last ->
                    onLineToPoint(last)
                }
                onLineToPoint(point)
                _pointList.clear()
            }

            POINT_TYPE_NEW -> {
                clearLastPoint()
                onNewPoint(x, y)
            }

            POINT_TYPE_GAP -> lineLastPoint(point)
            POINT_TYPE_SAME -> Unit
        }

        _resetLastPoint(point)
    }

    /**[writePoint]*/
    fun appendPoint(x: Double, y: Double, radians: Float?) = writePoint(x, y, radians)

    //endregion ---Core---

    //region ---Path---

    /**[Path]路径描边数据(已经旋转后), 转成矢量数据(GCode/SVG数据等)
     * [offsetLeft] [offsetTop] 偏移量
     * [pathStep] 路径枚举步长
     * [writeFirst] 是否写入头数据
     * [writeLast] 是否写入尾数据
     * */
    fun pathStrokeToVector(
        path: Path,
        writeFirst: Boolean = true,
        writeLast: Boolean = true,
        offsetLeft: Float = 0f, //偏移的像素
        offsetTop: Float = 0f,
        pathStep: Float = LibHawkKeys._pathAcceptableError
    ) {
        val step = if (enableVectorRadiansSample) pathSampleStepRadians else pathStep
        path.eachPath(step) { index, ratio, contourIndex, posArray, tanArray ->
            val xPixel = posArray[0] + offsetLeft + 0.0
            val yPixel = posArray[1] + offsetTop + 0.0

            //像素转成mm/inch
            val x = unit?.convertPixelToValue(xPixel) ?: xPixel
            val y = unit?.convertPixelToValue(yPixel) ?: yPixel

            if (index == 0) {
                if (contourIndex == 0) {
                    if (writeFirst) {
                        onPathStart()
                    }
                } else {
                    //新的轮廓
                    onPathEnd(false)
                }
            }

            //激活了弧度采样
            val radians = if (enableVectorRadiansSample) tanArray[2] else null
            writePoint(x, y, radians)
        }
        clearLastPoint()
        if (writeLast) {
            onPathEnd(true)
        }
    }

    /** [pathStrokeToVector] */
    fun pathFillToVector(
        path: Path,
        writeFirst: Boolean = true,
        writeLast: Boolean = true,
        offsetLeft: Float = 0f, //偏移的像素
        offsetTop: Float = 0f,
        pathStep: Float = 1f,
        fillPathStep: Float = 1f, //填充间距
        fillAngle: Float = 0f, //填充线的旋转角度
    ) {
        if (fillPathStep <= 0f) {
            L.w("fillPathStep = $fillPathStep!")
            return
        }
        //能够完全包含path的矩形
        val pathBounds = acquireTempRectF()
        val targetPath = Path(path)

        var rotatePoint: PointF? = null
        if (fillAngle.isRotated()) {
            targetPath.computePathBounds(pathBounds)
            rotatePoint = PointF(pathBounds.centerX(), pathBounds.centerY())
            targetPath.rotate(-fillAngle, rotatePoint) //先反向旋转, 再正向旋转
        }
        targetPath.computePathBounds(pathBounds)

        //矩形由上往下扫描, 取与path的交集
        val scanStep = fillPathStep //扫描步长

        var y = pathBounds.top + scanStep
        var endY = pathBounds.bottom

        val scanPath = acquireTempPath() //扫描形状
        val resultPath = acquireTempPath() //碰撞结果

        val centerX = pathBounds.centerX()
        val centerY = pathBounds.centerY()

        if (pathFillType == PATH_FILL_TYPE_CIRCLE) {
            //使用圆的方式填充, 则y是当前扫描的半径, endY是最大扫描半径
            y = scanStep
            endY = c(pathBounds.width() / 2, pathBounds.height() / 2).toFloat()
        }

        var isFirst = true
        while (y <= endY) {
            //逐行扫描
            scanPath.rewind()
            resultPath.rewind()

            //一行
            //这里用CCW出来的就是顺时针
            if (pathFillType == PATH_FILL_TYPE_CIRCLE) {
                //圆形碰撞扫描
                scanPath.addCircle(centerX, centerY, y, Path.Direction.CCW)
            } else {
                scanPath.addRect(
                    pathBounds.left,
                    y,
                    pathBounds.right,
                    y + scanStep,
                    Path.Direction.CCW //ccw无效?
                )
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT &&
                resultPath.op(scanPath, targetPath, Path.Op.INTERSECT)
            ) {
                //操作成功
                if (!resultPath.isEmpty) {
                    //有交集数据, 写入GCode数据

                    if (fillAngle.isRotated()) {
                        resultPath.rotate(fillAngle, rotatePoint) //正向旋转
                    }

                    pathStrokeToVector(
                        resultPath,
                        writeFirst && isFirst,
                        false,
                        offsetLeft,
                        offsetTop,
                        pathStep,
                    )
                    isFirst = false
                }
            }

            if (y == endY) {
                break
            }

            //
            y += if (pathFillType == PATH_FILL_TYPE_CIRCLE) {
                scanStep * 2
            } else {
                scanStep * 2
            }

            //
            if (y > endY) {
                //y = endY //填充下, 丢弃. 防止重复雕刻
                break
            }
        }
        clearLastPoint()
        if (writeLast) {
            onPathEnd(true)
        }
        scanPath.release()
        resultPath.release()
        pathBounds.release()
    }

    /**[pathList] 实际的路径数据
     * [pathStrokeToVector]
     * [pathFillToVector]
     * */
    fun pathStrokeToVector(
        pathList: List<Path>,
        writeFirst: Boolean = true,
        writeLast: Boolean = true,
        offsetLeft: Float = 0f, //偏移的像素
        offsetTop: Float = 0f,
        pathStep: Float = LibHawkKeys._pathAcceptableError
    ) {
        var isFirst = true
        for (path in pathList) {
            if (this is GCodeWriteHandler) {
                isSetPower = false
            }

            if (isDebuggerConnected()) {
                val bitmap = path.toBitmap()
                L.i(bitmap?.byteCount)
            }
            pathStrokeToVector(
                path,
                writeFirst && isFirst,
                false,
                offsetLeft,
                offsetTop,
                pathStep
            )

            isFirst = false
        }
        clearLastPoint()
        if (writeLast) {
            onPathEnd(true)
        }
    }

    /**[pathList] 实际的路径数据
     * [pathStrokeToVector]
     * [pathFillToVector]
     * */
    fun pathFillToVector(
        pathList: List<Path>,
        writeFirst: Boolean = true,
        writeLast: Boolean = true,
        offsetLeft: Float = 0f, //偏移的像素
        offsetTop: Float = 0f,
        pathStep: Float = 1f,
        fillPathStep: Float = 1f, //填充间距
        fillAngle: Float = 0f, //填充线的旋转角度
    ) {
        var isFirst = true
        for (path in pathList) {
            if (isDebuggerConnected()) {
                val bitmap = path.toBitmap()
                L.i(bitmap?.byteCount)
            }
            pathFillToVector(
                path,
                writeFirst && isFirst,
                false,
                offsetLeft,
                offsetTop,
                pathStep,
                fillPathStep,
                fillAngle
            )

            isFirst = false
        }
        clearLastPoint()
        if (writeLast) {
            onPathEnd(true)
        }
    }

    //endregion ---Path---

    /**矢量点位信息*/
    data class VectorPoint(
        /**坐标值, 输入的是啥单位就是啥单位*/
        val x: Double,
        val y: Double,
        /**当前这个点, 和上一个点的类型
         * [com.angcyo.vector.VectorWriteHandler.POINT_TYPE_NEW]
         * [com.angcyo.vector.VectorWriteHandler.POINT_TYPE_SAME]
         * [com.angcyo.vector.VectorWriteHandler.POINT_TYPE_GAP]
         * [com.angcyo.vector.VectorWriteHandler.POINT_TYPE_CIRCLE]
         * */
        val pointType: Int,
        /**圆心坐标, 如果有*/
        var circle: PointD? = null,

        /**圆的方向
         * `G2` 顺时针画弧 -> Path.Direction.CCW
         * `G3` 逆时针画弧 -> Path.Direction.CW
         * */
        var circleDir: Path.Direction? = null,

        /**当前点的弧度值*/
        var radians: Float? = null,

        /**当前点和上一个点的弧度差值, 用来识别G2/G3*/
        //val dxRadians: Float? = null, //关闭G2/G3输出
    )

}