package com.angcyo.vector

import android.graphics.Path
import android.os.Debug
import com.angcyo.library.L
import com.angcyo.library.annotation.Flag
import com.angcyo.library.annotation.Private
import com.angcyo.library.component.pool.acquireTempPath
import com.angcyo.library.component.pool.acquireTempRectF
import com.angcyo.library.component.pool.release
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
        const val PATH_SPACE_GAP = 0.15f

        /**Path的采样率,采样率越高, 间隙越小, 越清晰*/
        const val PATH_SPACE_1K = 0.1f
        const val PATH_SPACE_2K = 0.05f
        const val PATH_SPACE_4K = 0.025f

        /**距离上一个点, 改变了, 则使用G0*/
        @Flag("数值改变类型")
        private const val VALUE_CHANGED = 1 //改变了

        /**和上一个点一致, 忽略*/
        @Flag("数值改变类型")
        private const val VALUE_SAME = 2 //一致

        /**和上一个点在gap范围内, 则使用G1*/
        @Flag("数值改变类型")
        private const val VALUE_SAME_GAP = 3 //在GAP范围内, 一致
    }

    //---

    /**输出写入器*/
    var writer: Appendable? = null

    /**单位转换器, 如果不设置则[Path]的值, 1:1输出*/
    var unit: IValueUnit? = null

    /**非像素值, 真实值. 间隔太长, 就会使用G0移动到过
     * 当2个点之间的距离小于此值时, 视为同一个点
     *
     * 负数表示关闭Gap判断, 全部使用G1
     * */
    var gapValue: Float = PATH_SPACE_GAP

    //---

    /**是否关闭Gap, 则所有的点都直接用G1/L*/
    val isCloseGap: Boolean
        get() = gapValue <= 0f

    //存值
    val _xList = mutableListOf<Float>()
    val _yList = mutableListOf<Float>()

    //---

    //相同方向的值, 是否改变了, 不一致. 此时可能需要G0操作
    @Private
    fun _valueChangedType(list: List<Float>, newValue: Float): Int {
        if (list.isEmpty()) {
            return VALUE_CHANGED
        }
        val lastValue = list.last()
        return _valueChangedType(lastValue, newValue)
    }

    /**相同方向的值, 是否改变了, 不一致. 此时可能需要G0操作*/
    @Private
    fun _valueChangedType(oldValue: Float, newValue: Float): Int {
        if (oldValue == newValue) {
            return VALUE_SAME
        }
        if (isCloseGap) {
            return VALUE_SAME_GAP
        }
        if ((oldValue - newValue).absoluteValue <= gapValue) {
            return VALUE_SAME_GAP
        }
        //需要G0 / M
        return VALUE_CHANGED
    }

    //add value, 并且只留2个值
    @Private
    fun _resetLast(list: MutableList<Float>, value: Float) {
        if (list.size >= 2) {
            list.removeLast()
        }
        list.add(value)
    }

    /**判断G1的线的角度是否相同, 相同角度也视为是相同的线*/
    @Private
    fun _isSameAngle(newX: Float, newY: Float): Boolean {
        if (_xList.size() > 1 && _yList.size() > 1) {
            val a1 = VectorHelper.angle(
                _xList.first(),
                _yList.first(),
                _xList.last(),
                _yList.last()
            )
            val a2 = VectorHelper.angle(
                _xList.last(),
                _yList.last(),
                newX,
                newY
            )
            return (a2 - a1).absoluteValue < 0.01
        } else {
            return false
        }
    }

    //---

    fun reset() {
        _xList.clear()
        _yList.clear()
    }

    /**[Path]的起点
     * GCode 数据的一些初始化配置
     * Svg 数据的M操作
     * */
    open fun onPathStart(x: Float, y: Float) {

    }

    /**[Path]的终点*/
    open fun onPathEnd() {
        //_xList.lastOrNull()
        //_yList.lastOrNull()
        checkLastPoint()
    }

    /**[Path]中每一段路径的起点
     * GCode
     * SVG M
     * */
    open fun onFirstPoint(x: Float, y: Float) {

    }

    /**产生的一个新点, 则应该先把之前的数据G1, 然后新数据G0*/
    open fun onNewPoint(x: Float, y: Float) {
        checkLastPoint()
    }

    /**需要连接到点
     * GCode 用G1
     * SVG 用L*/
    open fun onLineToPoint(x: Float, y: Float) {

    }

    /**检查上一次是否还有点没有处理*/
    fun checkLastPoint() {
        if (_xList.isNotEmpty() && _yList.isNotEmpty()) {
            //如果有旧数据
            val lastX = _xList.last()
            val lastY = _yList.last()
            onLineToPoint(lastX, lastY)
            reset()//重置集合
        }
    }

    //region ---Core---

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
        if (_xList.isEmpty() && _yList.isEmpty()) {
            onFirstPoint(x, y)
        } else {
            val xChangedType = _valueChangedType(_xList, x)
            val yChangedType = _valueChangedType(_yList, y)

            //G1
            var newPoint = false

            if (xChangedType == VALUE_CHANGED || yChangedType == VALUE_CHANGED) {
                //很大的跨度点
                newPoint = true
            }

            if (xChangedType == VALUE_SAME && yChangedType == VALUE_SAME_GAP) {
                //可能是竖线, 判断之前是否是横线, 如果是则G1
                if (_yList.size() >= 2 && _yList.first() == _yList.last()) {
                    newPoint = true
                }
            } else if (yChangedType == VALUE_SAME && xChangedType == VALUE_SAME_GAP) {
                //可能是横线, 判断之前是否是竖线, 如果是则G1
                if (_xList.size() >= 2 && _xList.first() == _xList.last()) {
                    newPoint = true
                }
            } else if (xChangedType == VALUE_SAME_GAP && yChangedType == VALUE_SAME_GAP) {
                //斜向
                if (_isSameAngle(x, y)) {
                    //角度相同
                } else {
                    newPoint = true
                }
            }

            if (newPoint) {
                //G1, 上一次的点需要先被G1连接
                if (_xList.size() > 1 && _yList.size() > 1) {
                    onNewPoint(x, y)
                }
            }

            if (xChangedType == VALUE_CHANGED || yChangedType == VALUE_CHANGED) {
                //此时G0
                onFirstPoint(x, y)
            }
        }

        _resetLast(_xList, x)
        _resetLast(_yList, y)
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
                    onPathStart(x, y)
                }
            }
            if (index == 0) {
                //path 可能有多段
                onNewPoint(x, y)
            }

            writePoint(x, y)
        }
        checkLastPoint()
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
        val lineHeight = pathStep
        var y = pathBounds.top + lineHeight
        val endY = pathBounds.bottom
        val linePath = acquireTempPath()
        val resultPath = acquireTempPath()

        var isFirst = true
        while (y <= endY) {
            //逐行扫描
            linePath.rewind()
            resultPath.rewind()

            //一行
            linePath.addRect(
                pathBounds.left,
                y - lineHeight,
                pathBounds.right,
                y,
                Path.Direction.CW
            )

            if (resultPath.op(linePath, path, Path.Op.INTERSECT)) {
                //操作成功
                if (!resultPath.isEmpty) {
                    //有交集数据, 写入GCode数据
                    pathStrokeToVector(
                        resultPath,
                        writeFirst && isFirst,
                        false,
                        offsetLeft,
                        offsetTop,
                        lineHeight,
                    )
                    isFirst = false
                }
            }

            if (y == endY) {
                break
            }
            y += pathStep + lineHeight
            if (y > endY) {
                y = endY
            }
        }
        checkLastPoint()
        if (writeLast) {
            onPathEnd()
        }
        linePath.release()
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
        checkLastPoint()
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
        checkLastPoint()
        if (writeLast) {
            onPathEnd()
        }
    }

    //endregion ---Path---

}