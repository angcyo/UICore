package com.angcyo.vector

import android.graphics.Path
import android.os.Debug
import com.angcyo.library.L
import com.angcyo.library.annotation.Flag
import com.angcyo.library.annotation.MM
import com.angcyo.library.annotation.Private
import com.angcyo.library.component.pool.acquireTempPath
import com.angcyo.library.component.pool.acquireTempRectF
import com.angcyo.library.component.pool.release
import com.angcyo.library.ex.c
import com.angcyo.library.ex.eachPath
import com.angcyo.library.ex.size
import com.angcyo.library.ex.toBitmap
import com.angcyo.library.unit.IValueUnit
import kotlin.math.absoluteValue

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

        //---

        /**是一个重新开始的, 那么之前的数据需要G1先连接, 然后G0到新的点*/
        @Flag("点位类型")
        private const val POINT_TYPE_NEW = 1

        /**当前的点和上个点是一样的, 则抹除上一次的最后点信息*/
        @Flag("点位类型")
        private const val POINT_TYPE_SAME = 2

        /**当和上一个点不一样, 但是又在可允许的范围内时, 则把之前的数据G1连起来,
         * 并且替换最后一个点为第一个点,而不是擦除所有点位信息*/
        @Flag("点位类型")
        private const val POINT_TYPE_GAP = 3
    }

    //---

    /**输出写入器*/
    var writer: Appendable? = null

    /**单位转换器, 如果不设置则[Path]的值, 1:1输出*/
    var unit: IValueUnit? = null

    /**真实值, 可以是像素, 也可以是mm
     *
     * 间隔太长, 就会使用G0移动到过
     * 当2个点之间的距离小于此值时, 视为同一个点
     *
     * 负数表示关闭Gap判断, 全部使用G1
     * */
    var gapValue: Float = PATH_SPACE_GAP

    /**当距离上一个点距离大于这个值时,
     * 则连接之前的最后一个点,并且抹除点位信息.
     *
     * 类似于2点之间的间隙
     * */
    var gapMaxValue: Float = PATH_SPACE_GAP * 2

    /**多少度误差之内, 视为同一个点*/
    var angleGapValue: Float = 0.5f

    /**路径填充类型*/
    var pathFillType: Int = PATH_FILL_TYPE_RECT

    //---

    /**是否关闭Gap, 则所有的点都直接用G1/L*/
    val isCloseGap: Boolean
        get() = gapValue <= 0f

    /**记录*/
    val _pointList = mutableListOf<VectorPoint>()

    //region ---Core回调---

    /**[Path]的起点
     * GCode 数据的一些初始化配置
     * Svg 数据的M操作
     * */
    open fun onPathStart() {

    }

    /**[Path]的终点*/
    open fun onPathEnd() {
        clearLastPoint()
    }

    /**
     * 产生的一个新点, 则应该先把之前的数据G1, 然后新数据G0
     * GCode G0
     * SVG M
     * */
    open fun onNewPoint(x: Float, y: Float) {

    }

    /**需要连接到点
     * GCode 用G1
     * SVG 用L*/
    open fun onLineToPoint(x: Float, y: Float) {

    }

    //endregion ---Core回调---

    //region ---Core---

    /**清理上一次最后的点, 通常在遇到新的点时调用*/
    fun clearLastPoint() {
        if (_pointList.size() > 1) {
            //如果有旧数据
            val last = _pointList.last()
            onLineToPoint(last.x, last.y)
        }
        _pointList.clear()//重置集合
    }

    /**连接到最后一个点, 并且将最后一个点设置为第一个点*/
    fun lineLastPoint() {
        if (_pointList.isNotEmpty()) {
            //如果有旧数据
            val last = _pointList.last()
            onLineToPoint(last.x, last.y)
            _pointList.clear()
            _pointList.add(last)
        }
    }

    //add value, 并且只留2个值
    @Private
    fun _resetLastPoint(point: VectorPoint) {
        if (_pointList.size >= 2) {
            _pointList.removeLast()
        }
        _pointList.add(point)
    }

    /**计算角度偏移*/
    fun _angleDiff(newX: Float, newY: Float): Float {
        return if (_pointList.size() > 1) {
            val first = _pointList.first()
            val last = _pointList.last()
            val a1 = VectorHelper.angle(first.x, first.y, last.x, last.y)
            val a2 = VectorHelper.angle(last.x, last.y, newX, newY)
            return (a2 - a1).absoluteValue
        } else {
            -1f
        }
    }

    /**判断当前的点, 和之前的点是否是同一角度*/
    @Private
    fun _isSameAngle(newX: Float, newY: Float): Boolean {
        return if (_pointList.size() > 1) {
            _angleDiff(newX, newY) < angleGapValue
        } else {
            false
        }
    }

    /**计算当前的点, 和上一个点的类型*/
    fun _valueChangedType(x: Float, y: Float): Int {
        val last = _pointList.lastOrNull() ?: return POINT_TYPE_NEW //之前没有点, 那当前的点肯定是最新的
        //val first = _pointList.first()
        if (_pointList.size() == 1) {
            //之前只有1个点
            return _valueChangedType(last, x, y)
        } else {
            //之前已经有多个点
            //val firstType = _valueChangedType(first, x, y) //第一个点的类型
            val lastType = _valueChangedType(last, x, y) //最后一个点的类型

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

    fun _valueChangedType(point: VectorPoint, x: Float, y: Float): Int {
        val c = c(point.x, point.y, x, y).toFloat()
        if (((point.x - x).absoluteValue > gapMaxValue || (point.y - y).absoluteValue > gapMaxValue) && c > gapMaxValue) {
            //2点之间间隙太大, 则视为新的点
            return POINT_TYPE_NEW
        }
        if (point.x == x || point.y == y) {
            //在一根线上
            return POINT_TYPE_SAME
        }
        if (isCloseGap) {
            //关闭了gap, 则直接G1之前的点, 否则可能是一样的值, 直接忽略
            return POINT_TYPE_GAP
        }
        if (c < gapValue) {
            return POINT_TYPE_SAME
        }
        return POINT_TYPE_GAP
    }

    /**创建一个新的点*/
    fun generatePoint(x: Float, y: Float): VectorPoint = VectorPoint(x, y, _valueChangedType(x, y))

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
     * */
    fun writePoint(x: Float, y: Float) {
        val point = generatePoint(x, y)

        when (point.pointType) {
            POINT_TYPE_NEW -> {
                clearLastPoint()
                onNewPoint(x, y)
            }
            POINT_TYPE_GAP -> lineLastPoint()
            /*POINT_TYPE_SAME, else -> _resetLastPoint(point)*/
        }

        _resetLastPoint(point)
    }

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
        pathStep: Float = 1f
    ) {
        path.eachPath(pathStep) { index, ratio, contourIndex, posArray ->
            val xPixel = posArray[0] + offsetLeft
            val yPixel = posArray[1] + offsetTop

            //像素转成mm/inch
            val x = unit?.convertPixelToValue(xPixel) ?: xPixel
            val y = unit?.convertPixelToValue(yPixel) ?: yPixel

            if (index == 0 && contourIndex == 0) {
                if (writeFirst) {
                    onPathStart()
                }
            }

            writePoint(x, y)
        }
        clearLastPoint()
        if (writeLast) {
            onPathEnd()
        }
    }

    /** [pathStrokeToVector] */
    fun pathFillToVector(
        path: Path,
        writeFirst: Boolean = true,
        writeLast: Boolean = true,
        offsetLeft: Float = 0f, //偏移的像素
        offsetTop: Float = 0f,
        pathStep: Float = 1f
    ) {
        //能够完全包含path的矩形
        val pathBounds = acquireTempRectF()
        path.computeBounds(pathBounds, true)

        //矩形由上往下扫描, 取与path的交集
        val scanStep = pathStep //扫描步长

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
                    y - scanStep,
                    pathBounds.right,
                    y,
                    Path.Direction.CCW //ccw无效?
                )
            }

            if (resultPath.op(scanPath, path, Path.Op.INTERSECT)) {
                //操作成功
                if (!resultPath.isEmpty) {
                    //有交集数据, 写入GCode数据
                    pathStrokeToVector(
                        resultPath,
                        writeFirst && isFirst,
                        false,
                        offsetLeft,
                        offsetTop,
                        scanStep,
                    )
                    isFirst = false
                }
            }

            if (y == endY) {
                break
            }

            //
            y += if (pathFillType == PATH_FILL_TYPE_CIRCLE) {
                scanStep
            } else {
                pathStep + scanStep
            }

            //
            if (y > endY) {
                y = endY
            }
        }
        clearLastPoint()
        if (writeLast) {
            onPathEnd()
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
        pathStep: Float = 1f
    ) {
        var isFirst = true
        for (path in pathList) {
            if (Debug.isDebuggerConnected()) {
                val bitmap = path.toBitmap()
                L.i(bitmap.byteCount)
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
            onPathEnd()
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
        pathStep: Float = 1f
    ) {
        var isFirst = true
        for (path in pathList) {
            if (Debug.isDebuggerConnected()) {
                val bitmap = path.toBitmap()
                L.i(bitmap.byteCount)
            }
            pathFillToVector(
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
            onPathEnd()
        }
    }

    //endregion ---Path---

    /**矢量点位信息*/
    data class VectorPoint(
        /**坐标值, 输入的是啥单位就是啥单位*/
        val x: Float,
        val y: Float,
        /**当前这个点, 和上一个点的类型
         * [com.angcyo.vector.VectorWriteHandler.POINT_TYPE_NEW]
         * [com.angcyo.vector.VectorWriteHandler.POINT_TYPE_SAME]
         * [com.angcyo.vector.VectorWriteHandler.POINT_TYPE_GAP]
         * */
        val pointType: Int
    )

}