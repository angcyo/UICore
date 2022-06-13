package com.angcyo.canvas.utils

import android.graphics.Matrix
import android.graphics.Path
import android.graphics.RectF
import com.angcyo.canvas.core.MmValueUnit
import com.angcyo.canvas.items.getHoldData
import com.angcyo.canvas.items.renderer.BaseItemRenderer
import com.angcyo.gcode.GCodeAdjust
import com.angcyo.library.ex.computeBounds
import com.angcyo.library.ex.eachPath
import com.angcyo.library.ex.file
import com.angcyo.library.utils.fileName
import com.angcyo.library.utils.filePath
import java.io.File
import kotlin.math.min

/**
 * 雕刻助手
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/05/25
 */
object EngraveHelper {

    /**GCode数据, 字符串的文本数据*/
    const val KEY_GCODE = "key_gcode"

    /**SVG数据, List<Path>*/
    const val KEY_SVG = "key_svg"

    /**将路径描边转换为G1代码
     * [path] 需要转换的路径, 缩放后的path, 但是没有旋转
     * [rotateBounds] 路径需要平移的left, top
     * [rotate] 路径需要旋转的角度
     * [outputFile] GCode输出路径
     * */
    fun pathStrokeToGCode(
        path: Path, rotateBounds: RectF, rotate: Float,
        outputFile: File = filePath("GCode", fileName(suffix = ".gcode")).file()
    ): File {
        //形状的路径, 用来计算path内的左上角偏移量
        val pathBounds = RectF()
        path.computeBounds(pathBounds, true)
        val targetPath = Path(path)

        val matrix = Matrix()
        matrix.postRotate(rotate, rotateBounds.centerX(), rotateBounds.centerY())

        //旋转的支持
        matrix.mapRect(pathBounds, pathBounds)
        targetPath.transform(matrix)

        val offsetLeft = rotateBounds.left - min(0f, pathBounds.left)
        val offsetTop = rotateBounds.top - min(0f, pathBounds.top)

        //像素单位转成mm单位
        val mmValueUnit = MmValueUnit()

        val pathGCodeHandler = PathGCodeHandler()

        outputFile.writer().use { writer ->
            targetPath.eachPath { index, posArray ->
                //像素单位, 要转成G21毫米单位
                val xPixel = posArray[0] + offsetLeft
                val yPixel = posArray[1] + offsetTop

                val x = mmValueUnit.convertPixelToValue(xPixel)
                val y = mmValueUnit.convertPixelToValue(yPixel)

                pathGCodeHandler.writeLine(writer, index, x, y)
            }
            pathGCodeHandler.writeFinish(writer)
        }

        return outputFile
    }

    /**将路径集合转换成GCode, 只有描边的数据
     * [pathList] 未缩放旋转的原始路径数据
     * [bounds] 未旋转时的bounds, 用来实现缩放
     * [rotate] 旋转角度, 配合[bounds]实现平移
     * */
    fun pathStrokeToGCode(
        pathList: List<Path>, bounds: RectF, rotate: Float,
        outputFile: File = filePath("GCode", fileName(suffix = ".gcode")).file()
    ): File {
        val newPathList = mutableListOf<Path>()

        val matrix = Matrix()
        val rotateBounds = RectF()//旋转后的Bounds
        matrix.setRotate(rotate, bounds.centerX(), bounds.centerY())
        matrix.mapRectF(bounds, rotateBounds)

        //平移到左上角0,0, 然后缩放, 旋转
        var pathBounds = pathList.computeBounds()
        matrix.reset()
        matrix.setTranslate(-pathBounds.left, -pathBounds.top)

        //缩放
        val scaleX = bounds.width() / pathBounds.width()
        val scaleY = bounds.height() / pathBounds.height()
        matrix.postScale(scaleX, scaleY, 0f, 0f)

        //旋转
        matrix.postRotate(rotate, bounds.width() / 2, bounds.height() / 2)

        for (path in pathList) {
            val newPath = Path(path)
            newPath.transform(matrix)
            newPathList.add(newPath)
        }

        //再次偏移到目标位置中心点重合的位置
        pathBounds = newPathList.computeBounds()
        matrix.reset()
        matrix.setTranslate(
            rotateBounds.centerX() - pathBounds.centerX(),
            rotateBounds.centerY() - pathBounds.centerY()
        )
        for (path in newPathList) {
            path.transform(matrix)
        }

        //转换成GCode
        val gCodeHandler = PathGCodeHandler()
        gCodeHandler.autoFinish = false
        outputFile.writer().use { writer ->
            gCodeHandler.pathStrokeToGCode(newPathList, MmValueUnit(), writer)
            gCodeHandler.writeFinish(writer)
        }
        return outputFile
    }

    /**GCode数据坐标调整, 先缩放旋转,再偏移
     * [gCode]
     * [bounds] 未旋转时的bounds
     * [rotate] 旋转角度, 配合[bounds]实现平移
     * */
    fun gCodeAdjust(
        gCode: String, bounds: RectF, rotate: Float,
        outputFile: File = filePath("GCode", fileName(suffix = ".gcode")).file()
    ): File {
        val gCodeAdjust = GCodeAdjust()
        gCodeAdjust.gCodeAdjust(gCode, bounds, rotate, outputFile)
        return outputFile
    }
}

/**获取渲染器对应的GCode数据, 如果有*/
fun BaseItemRenderer<*>.getGCodeText(): String? {
    return getRendererItem()?.getHoldData(EngraveHelper.KEY_GCODE)
}

/**获取渲染器对应的List<Path>数据, 如果有*/
fun BaseItemRenderer<*>.getPathList(): List<Path>? {
    return getRendererItem()?.getHoldData(EngraveHelper.KEY_SVG)
}