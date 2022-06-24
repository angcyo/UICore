package com.angcyo.canvas.utils

import android.graphics.Path
import android.os.Debug
import com.angcyo.canvas.core.IValueUnit
import com.angcyo.canvas.core.InchValueUnit
import com.angcyo.library.L
import com.angcyo.library.ex.eachPath
import com.angcyo.library.ex.toBitmap
import kotlin.math.absoluteValue

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/06/13
 */
class GCodeWriteHandler {

    companion object {
        /**如果2点之间的间隙大于此值, 则使用G0指令*/
        const val PATH_GAP = 10_000
    }

    val gap = PATH_GAP

    var lastX = Float.MIN_VALUE
    var lastY = Float.MIN_VALUE

    /**是否关闭了CNC, 如果关闭了CNC所有G操作都变成G0操作*/
    var isCloseCnc = false

    /**自动关闭CNC*/
    var autoClose = true

    /**是否自动归位, G0 X0 Y0 */
    var autoFinish: Boolean = true

    fun reset() {
        lastX = Float.MIN_VALUE
        lastY = Float.MIN_VALUE
    }

    /**[Path]路径描边数据, 转成GCode数据, 不包含GCode头尾数据*/
    fun pathStrokeToGCode(path: Path, unit: IValueUnit, writer: Appendable) {
        path.eachPath { index, posArray ->
            val xPixel = posArray[0]
            val yPixel = posArray[1]

            //像素转成mm/inch
            val x = unit.convertPixelToValue(xPixel)
            val y = unit.convertPixelToValue(yPixel)

            writeLine(writer, index == 0, x, y)
        }
        if (autoClose) {
            closeCnc(writer)
        }
        if (autoFinish) {
            writeFinish(writer)
        }
    }

    /**[pathList] 实际的路径数据
     * [pathStrokeToGCode]*/
    fun pathStrokeToGCode(pathList: List<Path>, unit: IValueUnit, writer: Appendable) {
        for (path in pathList) {
            if (Debug.isDebuggerConnected()) {
                val bitmap = path.toBitmap()
                L.i()
            }
            path.eachPath { index, posArray ->
                val xPixel = posArray[0]
                val yPixel = posArray[1]

                //像素转成mm/inch
                val x = unit.convertPixelToValue(xPixel)
                val y = unit.convertPixelToValue(yPixel)

                writeLine(writer, index == 0, x, y)
            }
            reset()
        }
        if (autoClose) {
            closeCnc(writer)
        }
        if (autoFinish) {
            writeFinish(writer)
        }
    }

    /**关闭CNC
     * M05指令:主轴关闭, M03:主轴打卡*/
    fun closeCnc(writer: Appendable) {
        if (!isCloseCnc) {
            writer.appendLine("M05 S0")
            isCloseCnc = true
        }
    }

    /**打开CNC
     * M05指令:主轴关闭, M03:主轴打卡*/
    fun openCnc(writer: Appendable) {
        if (isCloseCnc) {
            writer.appendLine("M03 S0")
            isCloseCnc = false
        }
    }

    /**结束后的指令*/
    fun writeFinish(writer: Appendable) {
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
        closeCnc(writer)
    }

    /**写入G0 或者 G1 指令*/
    fun writeLine(
        writer: Appendable,
        isFirst: Boolean,
        x: Float,
        y: Float,
        unit: IValueUnit? = null
    ) {
        if (isFirst) {
            writeFirst(writer, unit)
            writer.appendLine("G0 X${x} Y${y}")
        } else {
            if ((x - lastX).absoluteValue > gap || (y - lastY).absoluteValue > gap) {
                //跨度比较大时
                closeCnc(writer)
                writer.appendLine("G0 X${x} Y${y}")
            } else {
                openCnc(writer)
                writer.appendLine("G1 X${x} Y${y}")
            }
        }
        //end
        lastX = x
        lastY = y
    }
}