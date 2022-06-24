package com.angcyo.canvas.utils

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Path
import android.graphics.RectF
import com.angcyo.canvas.core.MmValueUnit
import com.angcyo.canvas.items.getHoldData
import com.angcyo.canvas.items.renderer.BaseItemRenderer
import com.angcyo.gcode.GCodeAdjust
import com.angcyo.library.ex.*
import com.angcyo.library.utils.fileName
import com.angcyo.library.utils.filePath
import java.io.File
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * 雕刻助手
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/05/25
 */
object CanvasDataHandleHelper {

    /**缓存文件的文件夹*/
    const val CACHE_FILE_FOLDER = "engrave"

    /**GCode数据, 字符串的文本数据*/
    const val KEY_GCODE = "key_gcode"

    /**SVG数据, List<Path>*/
    const val KEY_SVG = "key_svg"

    fun _defaultGCodeOutputFile() = filePath("GCode", fileName(suffix = ".gcode")).file()

    /**将路径描边转换为G1代码. 输出的GCode可以直接打印
     * [path] 需要转换的路径, 缩放后的path, 但是没有旋转
     * [rotateBounds] 路径需要平移的left, top
     * [rotate] 路径需要旋转的角度
     * [outputFile] GCode输出路径
     * */
    fun pathStrokeToGCode(
        path: Path,
        rotateBounds: RectF,
        rotate: Float,
        outputFile: File = _defaultGCodeOutputFile()
    ): File {
        //形状的路径, 用来计算path内的左上角偏移量
        val pathBounds = RectF()
        path.computeBounds(pathBounds, true)
        val targetPath = Path(path)

        //旋转的支持
        if (rotate != 0f) {
            val matrix = Matrix()
            matrix.postRotate(rotate, rotateBounds.centerX(), rotateBounds.centerY())
            matrix.mapRect(pathBounds, pathBounds)
            targetPath.transform(matrix)
        }

        val offsetLeft = rotateBounds.left - min(0f, pathBounds.left)
        val offsetTop = rotateBounds.top - min(0f, pathBounds.top)

        //像素单位转成mm单位
        val mmValueUnit = MmValueUnit()

        val gCodeWriteHandler = GCodeWriteHandler()

        outputFile.writer().use { writer ->
            targetPath.eachPath { index, posArray ->
                //像素单位, 要转成G21毫米单位
                val xPixel = posArray[0] + offsetLeft
                val yPixel = posArray[1] + offsetTop

                val x = mmValueUnit.convertPixelToValue(xPixel)
                val y = mmValueUnit.convertPixelToValue(yPixel)

                gCodeWriteHandler.writeLine(writer, index == 0, x, y)
            }
            gCodeWriteHandler.closeCnc(writer)
            gCodeWriteHandler.writeFinish(writer)
        }

        return outputFile
    }

    /**将路径集合转换成GCode, 只有描边的数据. 输出的GCode可以直接打印
     * [pathList] 未缩放旋转的原始路径数据
     * [bounds] 未旋转时的bounds, 用来实现缩放
     * [rotate] 旋转角度, 配合[bounds]实现平移
     * */
    fun pathStrokeToGCode(
        pathList: List<Path>,
        bounds: RectF,
        rotate: Float,
        outputFile: File = _defaultGCodeOutputFile()
    ): File {
        val newPathList = mutableListOf<Path>()

        val matrix = Matrix()
        val rotateBounds = RectF()//旋转后的Bounds
        if (rotate != 0f) {
            matrix.setRotate(rotate, bounds.centerX(), bounds.centerY())
        }
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
        if (rotate != 0f) {
            matrix.postRotate(rotate, bounds.width() / 2, bounds.height() / 2)
        }

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
        val gCodeHandler = GCodeWriteHandler()
        gCodeHandler.autoFinish = false
        outputFile.writer().use { writer ->
            gCodeHandler.pathStrokeToGCode(newPathList, MmValueUnit(), writer)
            gCodeHandler.closeCnc(writer)
            gCodeHandler.writeFinish(writer)
        }
        return outputFile
    }

    /**GCode数据坐标调整, 先缩放旋转,再偏移
     * [gCode] 原始的GCode数据
     * [bounds] 未旋转时的bounds
     * [rotate] 旋转角度, 配合[bounds]实现平移
     * */
    fun gCodeAdjust(
        gCode: String,
        bounds: RectF,
        rotate: Float,
        outputFile: File = _defaultGCodeOutputFile()
    ): File {
        val gCodeAdjust = GCodeAdjust()
        gCodeAdjust.gCodeAdjust(gCode, bounds, rotate, outputFile)
        return outputFile
    }

    /**GCode数据坐标平移
     * [gCode] 原始的GCode数据
     * [rotateBounds] 旋转后的bounds, 用来确定Left,Top坐标
     * [rotate] 旋转角度, 配合[bounds]实现平移
     * */
    fun gCodeTranslation(
        gCode: String,
        rotateBounds: RectF,
        outputFile: File = _defaultGCodeOutputFile()
    ): File {
        val gCodeAdjust = GCodeAdjust()
        outputFile.writer().use { writer ->
            gCodeAdjust.gCodeTranslation(gCode, rotateBounds.left, rotateBounds.top, writer)
        }
        return outputFile
    }

    /**简单的将[Bitmap]转成GCode数据
     * 横向扫描像素点,白色像素跳过,黑色就用G1打印
     *
     * [threshold] 当色值>=此值时, 忽略数据 255白色 [0~255]
     * [lineSpace] 每一行之间的间隙, 毫米单位. //1K:0.1 2K:0.05 4K:0.025f
     * */
    fun bitmapToGCode(
        bitmap: Bitmap,
        lineSpace: Float = 0.1f,
        threshold: Int = 255,
        outputFile: File = _defaultGCodeOutputFile()
    ): File {
        val gCodeWriteHandler = GCodeWriteHandler()

        val width = bitmap.width
        val height = bitmap.height
        val data = bitmap.engraveColorBytes()

        //像素单位转成mm单位
        val mmValueUnit = MmValueUnit()

        //转成像素
        val lineStep = mmValueUnit.convertValueToPixel(lineSpace).roundToInt()

        var isRTL = false
        var lastGCodeY: Int = -1

        //写入
        fun writeGCode(writer: Appendable, list: List<Int>, yPixel: Int) {
            if (list.size() > 0) {

                val first: Int = list.first()
                val last: Int = list.last()

                val x1 = mmValueUnit.convertPixelToValue(first.toFloat())
                val x2 = mmValueUnit.convertPixelToValue(last.toFloat())

                val y = mmValueUnit.convertPixelToValue(yPixel.toFloat())

                writer.appendLine("G0 X$x1 Y$y")
                gCodeWriteHandler.openCnc(writer)
                writer.appendLine("G1 X$x2 Y$y")

                lastGCodeY = yPixel
            }
        }

        outputFile.writer().use { writer ->

            val xList = mutableListOf<Int>() //相同像素的点的集合
            //y坐标
            var lastY: Int = 0

            gCodeWriteHandler.writeFirst(writer, mmValueUnit)

            var y = 0
            while (y < height) {//行
                lastY = y + 1
                for (x in 0 until width) {//列
                    //rtl
                    val lineX = if (isRTL) {
                        (width - 1 - x)
                    } else {
                        x
                    }
                    val index = y * width + lineX
                    val value: Int = data[index].toHexInt()
                    if (value >= threshold) {
                        //白色忽略
                        gCodeWriteHandler.closeCnc(writer)
                        writeGCode(writer, xList, lastY)
                        xList.clear()
                    } else {
                        xList.add(lineX + 1)
                    }
                }
                writeGCode(writer, xList, lastY)

                //rtl
                if (lastGCodeY == lastY) {
                    //这一行有GCode数据
                    isRTL = !isRTL
                }

                //换行了
                xList.clear()

                //next
                if (y == height - 1) {
                    break
                } else {
                    //最后一行
                    y += lineStep
                    if (y >= height) {
                        y = height - 1
                    }
                }
            }

            gCodeWriteHandler.closeCnc(writer)
            gCodeWriteHandler.writeFinish(writer)
        }
        return outputFile
    }
}

/**获取渲染器对应的GCode数据, 如果有*/
fun BaseItemRenderer<*>.getGCodeText(): String? {
    return getRendererItem()?.getHoldData(CanvasDataHandleHelper.KEY_GCODE)
}

/**获取渲染器对应的List<Path>数据, 如果有*/
fun BaseItemRenderer<*>.getPathList(): List<Path>? {
    return getRendererItem()?.getHoldData(CanvasDataHandleHelper.KEY_SVG)
}