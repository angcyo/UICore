package com.angcyo.gcode

import android.graphics.Path
import android.os.Debug
import com.angcyo.canvas.core.IValueUnit
import com.angcyo.canvas.core.InchValueUnit
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

        /**如果2点之间的间隙大于此值, 则使用G0指令
         * 0.5mm
         * 1K:0.1 2K:0.05 4K:0.025f
         * */
        const val GCODE_SPACE_GAP = 0.15f

        /**分辨率越高, 间隙越小, 越清晰*/
        const val GCODE_SPACE_1K = 0.1f
        const val GCODE_SPACE_2K = 0.05f
        const val GCODE_SPACE_4K = 0.025f
    }

    /**非像素值, 真实值*/
    var gapValue: Float = GCODE_SPACE_GAP

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

    /**[Path]路径描边数据, 转成GCode数据, 不包含GCode头尾数据*/
    fun pathStrokeToGCode(
        path: Path,
        unit: IValueUnit,
        writer: Appendable,
        offsetLeft: Float = 0f, //偏移的像素
        offsetTop: Float = 0f
    ) {
        writeFirst(writer, unit)
        path.eachPath { index, posArray ->
            val xPixel = posArray[0] + offsetLeft
            val yPixel = posArray[1] + offsetTop

            //像素转成mm/inch
            val x = unit.convertPixelToValue(xPixel)
            val y = unit.convertPixelToValue(yPixel)

            writeLine(writer, x, y)
        }
        if (autoFinish) {
            writeFinish(writer)
        }
    }

    /**[pathList] 实际的路径数据
     * [pathStrokeToGCode]*/
    fun pathStrokeToGCode(
        pathList: List<Path>,
        unit: IValueUnit,
        writer: Appendable,
        offsetLeft: Float = 0f, //偏移的像素
        offsetTop: Float = 0f
    ) {
        writeFirst(writer, unit)
        for (path in pathList) {
            if (Debug.isDebuggerConnected()) {
                val bitmap = path.toBitmap()
                L.i()
            }
            path.eachPath { index, posArray ->
                val xPixel = posArray[0] + offsetLeft
                val yPixel = posArray[1] + offsetTop

                //像素转成mm/inch
                val x = unit.convertPixelToValue(xPixel)
                val y = unit.convertPixelToValue(yPixel)

                writeLine(writer, x, y)
            }
            _writeLastG1(writer)
        }
        if (autoFinish) {
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
            writer.appendLine("M03 S0")
            isClosedCnc = false
        }
    }

    /**结束后的指令*/
    fun writeFinish(writer: Appendable) {
        _writeLastG1(writer)
        closeCnc(writer)
        writer.appendLine("G0 X0 Y0")
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
        val CHANGED = 1 //改变了
        val SAME = 2 //一致
        val SAME_GAP = 3 //在GAP范围内, 一致

        //相同方向的值, 是否改变了, 不一致. 此时可能需要G0操作
        fun valueChangedType(list: List<Float>, value: Float): Int {
            if (list.isEmpty()) {
                return CHANGED
            }
            val lastValue = list.last()
            if (lastValue == value) {
                return SAME
            }
            if ((lastValue - value).absoluteValue <= gapValue) {
                return SAME_GAP
            }
            //需要G0
            return CHANGED
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

            if (xChangedType == CHANGED || yChangedType == CHANGED ||
                (xChangedType == SAME_GAP && yChangedType == SAME_GAP)//斜向
            ) {
                //G1
                if (_xList.size() > 1 && _yList.size() > 1) {
                    _writeLastG1(writer)
                }

                //此时G0
                closeCnc(writer)
                writer.appendLine("G0 X${x} Y${y}")
            }
        }

        resetLast(_xList, x)
        resetLast(_yList, y)
    }

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

    //endregion

}