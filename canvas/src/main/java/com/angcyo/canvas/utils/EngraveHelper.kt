package com.angcyo.canvas.utils

import android.graphics.Matrix
import android.graphics.Path
import android.graphics.RectF
import com.angcyo.canvas.core.MmValueUnit
import com.angcyo.library.ex.eachPath
import java.io.File
import kotlin.math.absoluteValue
import kotlin.math.min

/**
 * 雕刻助手
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/05/25
 */
object EngraveHelper {

    /**GCode数据*/
    const val KEY_GCODE = "key_gcode"

    /**将路径描边转换为G1代码
     * [path] 需要转换的路径
     * [bounds] 路径需要平移的left, top
     * [rotate] 路径需要旋转的角度
     * [outputFile] GCode输出路径
     * */
    fun pathStrokeToGCode(path: Path, bounds: RectF, rotate: Float, outputFile: File): File {
        val gap = 10 //如果2点之间的间隙大于此值, 则使用G0指令
        var lastX = Float.MIN_VALUE
        var lastY = Float.MIN_VALUE

        //形状的路径, 用来计算path内的左上角偏移量
        val pathBounds = RectF()
        path.computeBounds(pathBounds, true)
        val targetPath = Path(path)

        val matrix = Matrix()
        matrix.postRotate(rotate, bounds.centerX(), bounds.centerY())

        //旋转的支持
        matrix.mapRect(pathBounds, pathBounds)
        targetPath.transform(matrix)

        val offsetLeft = bounds.left - min(0f, pathBounds.left)
        val offsetTop = bounds.top - min(0f, pathBounds.top)

        //像素单位转成mm单位
        val mmValueUnit = MmValueUnit()

        var isCloseCnc = false
        outputFile.writer().use { writer ->
            targetPath.eachPath { index, pos ->
                //像素单位, 要转成G21毫米单位
                val xPixel = pos[0] + offsetLeft
                val yPixel = pos[1] + offsetTop

                val x = mmValueUnit.convertPixelToValue(xPixel)
                val y = mmValueUnit.convertPixelToValue(yPixel)

                if (index == 0) {
                    writer.appendLine("G21")
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

            if (!isCloseCnc) {
                writer.appendLine("M05 S0")
                isCloseCnc = true
            }
            writer.appendLine("G0 X0 Y0")
        }

        return outputFile
    }
}