package com.angcyo.canvas.utils

import android.graphics.Path
import com.angcyo.canvas.core.IValueUnit
import com.angcyo.canvas.core.InchValueUnit
import com.angcyo.library.ex.eachPath
import kotlin.math.absoluteValue

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/06/13
 */
class PathGCodeHandler {

    companion object {
        /**如果2点之间的间隙大于此值, 则使用G0指令*/
        const val PATH_GAP = 10
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

    /**[Path]路径描边数据, 转成GCode数据, 不包含GCode头尾数据*/
    fun pathStrokeToGCode(path: Path, unit: IValueUnit, writer: Appendable) {
        path.eachPath { index, posArray ->
            val xPixel = posArray[0]
            val yPixel = posArray[1]

            //像素转成mm/inch
            val x = unit.convertPixelToValue(xPixel)
            val y = unit.convertPixelToValue(yPixel)

            writeLine(writer, index, x, y)
        }
        if (autoClose) {
            closeCnc(writer)
        }
        if (autoFinish) {
            writeFinish(writer)
        }
    }

    /**[pathList]
     * [pathStrokeToGCode]*/
    fun pathStrokeToGCode(pathList: List<Path>, unit: IValueUnit, writer: Appendable) {
        for (path in pathList) {
            path.eachPath { index, posArray ->
                val xPixel = posArray[0]
                val yPixel = posArray[1]

                //像素转成mm/inch
                val x = unit.convertPixelToValue(xPixel)
                val y = unit.convertPixelToValue(yPixel)

                writeLine(writer, index, x, y)
            }
        }
        if (autoClose) {
            closeCnc(writer)
        }
        if (autoFinish) {
            writeFinish(writer)
        }
    }

    /**关闭CNC*/
    fun closeCnc(writer: Appendable) {
        if (!isCloseCnc) {
            writer.appendLine("M05 S0")
            isCloseCnc = true
        }
    }

    /**结束后的指令*/
    fun writeFinish(writer: Appendable) {
        writer.appendLine("G0 X0 Y0")
    }

    /**写入G0 或者 G1 指令*/
    fun writeLine(writer: Appendable, index: Int, x: Float, y: Float, unit: IValueUnit? = null) {
        if (index == 0) {
            //[G20]英寸单位 [G21]毫米单位
            if (unit is InchValueUnit) {
                writer.appendLine("G20")
            } else {
                writer.appendLine("G21")
            }
            writer.appendLine("G90")
            writer.appendLine("G1 F2000")
            if (!isCloseCnc) {
                writer.appendLine("M05 S0")
                isCloseCnc = true
            }
            writer.appendLine("G0 X${x} Y${y}")
        } else {
            if ((x - lastX).absoluteValue > gap || (y - lastY).absoluteValue > gap) {
                //跨度比较大
                if (!isCloseCnc) {
                    writer.appendLine("M05 S0")
                    isCloseCnc = true
                }
                writer.appendLine("G0 X${x} Y${y}")
            } else {
                if (isCloseCnc) {
                    writer.appendLine("M03 S255")
                    isCloseCnc = false
                }
                writer.appendLine("G1 X${x} Y${y}")
            }
        }
        //end
        lastX = x
        lastY = y
    }
}