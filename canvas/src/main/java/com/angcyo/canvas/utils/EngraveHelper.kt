package com.angcyo.canvas.utils

import android.graphics.Matrix
import android.graphics.Path
import android.graphics.RectF
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

    /**将路径描边转换为G1代码
     * [path] 需要转换的路径
     * [bounds] 路径需要平移的left, top
     * [rotate] 路径需要旋转的角度
     * [outputFile] GCode输出路径
     * */
    fun pathStrokeToGCode(path: Path, bounds: RectF, rotate: Float, outputFile: File) {
        val gap = 10 //如果2点之间的间隙大于此值, 则使用G0指令
        var lastX = Float.MIN_VALUE
        var lastY = Float.MIN_VALUE

        val pathBounds = RectF()
        path.computeBounds(pathBounds, true)
        val targetPath = Path(path)

        val matrix = Matrix()
        matrix.postRotate(rotate, bounds.centerX(), bounds.centerY())

        matrix.mapRect(pathBounds, pathBounds)
        targetPath.transform(matrix)

        val offsetLeft = bounds.left - min(0f, pathBounds.left)
        val offsetTop = bounds.top - min(0f, pathBounds.top)

        var isCloseCnc = false
        outputFile.writer().use { writer ->
            targetPath.eachPath { index, pos ->
                val x = pos[0] + offsetLeft
                val y = pos[1] + offsetTop

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
    }

}