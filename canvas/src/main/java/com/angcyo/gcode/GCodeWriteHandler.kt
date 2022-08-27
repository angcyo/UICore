package com.angcyo.gcode

import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.os.Debug
import com.angcyo.canvas.core.IValueUnit
import com.angcyo.canvas.core.InchValueUnit
import com.angcyo.canvas.core.component.CanvasTouchHandler
import com.angcyo.canvas.utils.pathStyle
import com.angcyo.library.L
import com.angcyo.library.ex.eachPath
import com.angcyo.library.ex.size
import com.angcyo.library.ex.toBitmap
import kotlin.math.absoluteValue

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/06/13
 */
class GCodeWriteHandler {

    companion object {

        /**如果2点之间的间隙大于此值, 则使用G0指令 厘米单位
         * 0.5mm.
         * 1K:0.1 2K:0.05 4K:0.025f
         * */
        const val GCODE_SPACE_GAP = 0.15f

        /**分辨率越高, 间隙越小, 越清晰*/
        const val GCODE_SPACE_1K = 0.1f
        const val GCODE_SPACE_2K = 0.05f
        const val GCODE_SPACE_4K = 0.025f

        /**距离上一个点, 改变了, 则使用G0*/
        private const val VALUE_CHANGED = 1 //改变了

        /**和上一个点一致, 忽略*/
        private const val VALUE_SAME = 2 //一致

        /**和上一个点在gap范围内, 则使用G1*/
        private const val VALUE_SAME_GAP = 3 //在GAP范围内, 一致
    }

    /**非像素值, 真实值. 间隔太长, 就会使用G0移动到过
     * 当2个点之间的距离小于此值时, 视为同一个点
     *
     * 负数表示关闭Gap判断, 全部使用G1
     * */
    var gapValue: Float = GCODE_SPACE_GAP

    val isCloseGap: Boolean
        get() = gapValue <= 0f

    val _xList = mutableListOf<Float>()
    val _yList = mutableListOf<Float>()

    /**是否关闭了CNC, 如果关闭了CNC所有G操作都变成G0操作*/
    var isClosedCnc = false

    /**是否自动归位, G0 X0 Y0 */
    var autoFinish: Boolean = true

    fun reset() {
        _xList.clear()
        _yList.clear()
    }

    //region ---Path---

    /**[Path]路径描边数据(已经旋转后), 转成GCode数据, 不包含GCode头尾数据
     * [offsetLeft] [offsetTop] 偏移量
     * [pathStep] 路径枚举步长
     * [isFirst] 是否是头数据
     * [isFinish] 是否是尾数据
     * */
    fun pathStrokeToGCode(
        path: Path,
        unit: IValueUnit,
        writer: Appendable,
        isFirst: Boolean,
        isFinish: Boolean,
        offsetLeft: Float = 0f, //偏移的像素
        offsetTop: Float = 0f,
        pathStep: Float = 1f
    ) {
        if (isFirst) {
            writeFirst(writer, unit)
        }
        path.eachPath(pathStep) { index, ratio, posArray ->
            if (index == 0) {
                //path 可能有多段
                _writeLastG1(writer)
            }
            val xPixel = posArray[0] + offsetLeft
            val yPixel = posArray[1] + offsetTop

            //像素转成mm/inch
            val x = unit.convertPixelToValue(xPixel)
            val y = unit.convertPixelToValue(yPixel)

            writeLine(writer, x, y)
        }
        if (isFinish && autoFinish) {
            writeFinish(writer)
        }
    }

    /**
     * [Path] 填充路径转GCode
     * [pathToGCode]
     * [isFirst] 是否是头数据, 决定[writeFirst]
     * [isFinish] 是否是尾数据, 决定[writeFinish]
     * */
    fun pathFillToGCode(
        path: Path,
        unit: IValueUnit,
        writer: Appendable,
        isFirst: Boolean = false,
        isFinish: Boolean = false,
        offsetLeft: Float = 0f, //偏移的像素
        offsetTop: Float = 0f,
        pathStep: Float = 1f
    ) {
        //能够完全包含path的矩形
        val pathBounds = RectF()
        path.computeBounds(pathBounds, true)

        if (isFirst) {
            writeFirst(writer, unit)
        }

        //矩形由上往下扫描, 取与path的交集
        val lineHeight = pathStep
        var y = pathBounds.top + lineHeight
        val endY = pathBounds.bottom
        val linePath = Path()
        val resultPath = Path()
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
                    pathStrokeToGCode(
                        resultPath,
                        unit,
                        writer,
                        false,
                        false,
                        offsetLeft,
                        offsetTop,
                        lineHeight,
                    )
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
        if (isFinish && autoFinish) {
            writeFinish(writer)
        }
    }

    /**[pathList] 实际的路径数据
     * [pathStrokeToGCode] [pathFillToGCode]
     *
     * [offsetLeft] [offsetTop] 偏移量
     * [pathStep] 路径枚举步长
     *
     * [isFirst] 是否是头数据
     * [isFinish] 是否是尾数据
     * */
    fun pathToGCode(
        pathList: List<Path>,
        unit: IValueUnit,
        writer: Appendable,
        isFirst: Boolean,
        isFinish: Boolean,
        offsetLeft: Float = 0f, //偏移的像素
        offsetTop: Float = 0f,
        pathStep: Float = 1f
    ) {
        if (isFirst) {
            writeFirst(writer, unit)
        }
        for (path in pathList) {
            if (Debug.isDebuggerConnected()) {
                val bitmap = path.toBitmap()
                L.i(bitmap.byteCount)
            }

            val style = path.pathStyle()

            //先填充
            if (style == Paint.Style.FILL || style == Paint.Style.FILL_AND_STROKE) {
                pathFillToGCode(
                    path,
                    unit,
                    writer,
                    false,
                    false,
                    offsetLeft,
                    offsetTop,
                    pathStep
                )
            }

            //后描边
            if (style == Paint.Style.STROKE || style == Paint.Style.FILL_AND_STROKE) {
                pathStrokeToGCode(
                    path,
                    unit,
                    writer,
                    false,
                    false,
                    offsetLeft,
                    offsetTop,
                    pathStep
                )
            }

            _writeLastG1(writer)
        }
        if (isFinish && autoFinish) {
            writeFinish(writer)
        }
    }
    //endregion

    //region ---core---

    /**关闭CNC
     * M05指令:主轴关闭, M03:主轴打开*/
    fun closeCnc(writer: Appendable) {
        if (!isClosedCnc) {
            writer.appendLine("M05 S0")
            isClosedCnc = true
        }
    }

    /**打开CNC
     * M03:主轴打开*/
    fun openCnc(writer: Appendable) {
        if (isClosedCnc) {
            writer.appendLine("M03 S255")
            isClosedCnc = false
        }
    }

    /**结束后的指令*/
    fun writeFinish(writer: Appendable) {
        _writeLastG1(writer)
        closeCnc(writer)
        writer.append("G0 X0 Y0")
    }

    /**开始的指令*/
    fun writeFirst(writer: Appendable, unit: IValueUnit? = null) {
        //[G20]英寸单位 [G21]毫米单位
        if (unit is InchValueUnit) {
            writer.appendLine("G20")
        } else {
            writer.appendLine("G21")
        }
        writer.appendLine("G90")
        writer.appendLine("G1 F2000")
    }

    /**写入G0 或者 G1 指令. 会自动处理[CNC]
     * 只支持横向/纵向的点坐标转成G1, 不支持斜向.
     *
     * 请主动调用[writeFirst] [writeFinish]
     *
     * [x] [y] 非像素值, 真实值
     * */
    fun writeLine(writer: Appendable, x: Float, y: Float) {

        //相同方向的值, 是否改变了, 不一致. 此时可能需要G0操作
        fun valueChangedType(list: List<Float>, newValue: Float): Int {
            if (list.isEmpty()) {
                return VALUE_CHANGED
            }
            val lastValue = list.last()
            return _valueChangedType(lastValue, newValue)
        }

        //add value, 并且只留2个值
        fun resetLast(list: MutableList<Float>, value: Float) {
            if (list.size >= 2) {
                list.removeLast()
            }
            list.add(value)
        }

        if (_xList.isEmpty() && _yList.isEmpty()) {
            closeCnc(writer)
            writer.appendLine("G0 X${x} Y${y}")
        } else {
            val xChangedType = valueChangedType(_xList, x)
            val yChangedType = valueChangedType(_yList, y)

            //G1
            var writeLastG1 = false

            if (xChangedType == VALUE_CHANGED || yChangedType == VALUE_CHANGED) {
                //很大的跨度点
                writeLastG1 = true
            }

            if (xChangedType == VALUE_SAME && yChangedType == VALUE_SAME_GAP) {
                //可能是竖线, 判断之前是否是横线, 如果是则G1
                if (_yList.size() >= 2 && _yList.first() == _yList.last()) {
                    writeLastG1 = true
                }
            } else if (yChangedType == VALUE_SAME && xChangedType == VALUE_SAME_GAP) {
                //可能是横线, 判断之前是否是竖线, 如果是则G1
                if (_xList.size() >= 2 && _xList.first() == _xList.last()) {
                    writeLastG1 = true
                }
            } else if (xChangedType == VALUE_SAME_GAP && yChangedType == VALUE_SAME_GAP) {
                //斜向
                if (_isSameAngle(x, y)) {
                    //角度相同
                } else {
                    writeLastG1 = true
                }
            }

            if (writeLastG1) {
                //G1, 上一次的点需要先被G1连接
                if (_xList.size() > 1 && _yList.size() > 1) {
                    _writeLastG1(writer)
                }
            }

            if (xChangedType == VALUE_CHANGED || yChangedType == VALUE_CHANGED) {
                //此时G0
                closeCnc(writer)
                writer.appendLine("G0 X${x} Y${y}")
            }
        }

        resetLast(_xList, x)
        resetLast(_yList, y)
    }

    /**检查最后的数据*/
    fun _writeLastG1(writer: Appendable) {
        if (_xList.isNotEmpty() && _yList.isNotEmpty()) {
            //如果有旧数据
            openCnc(writer)

            val lastX = _xList.last()
            val lastY = _yList.last()

            writer.appendLine("G1 X${lastX} Y${lastY}")
            reset()
        }
    }

    /**判断G1的线的角度是否相同, 相同角度也视为是相同的线*/
    fun _isSameAngle(newX: Float, newY: Float): Boolean {
        if (_xList.size() > 1 && _yList.size() > 1) {
            val a1 = CanvasTouchHandler.angle(
                _xList.first(),
                _yList.first(),
                _xList.last(),
                _yList.last()
            )
            val a2 = CanvasTouchHandler.angle(
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

    /**相同方向的值, 是否改变了, 不一致. 此时可能需要G0操作*/
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
        //需要G0
        return VALUE_CHANGED
    }

    //endregion

}