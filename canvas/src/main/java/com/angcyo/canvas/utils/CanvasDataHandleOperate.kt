package com.angcyo.canvas.utils

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Path
import android.graphics.RectF
import android.view.Gravity
import com.angcyo.canvas.core.MmValueUnit
import com.angcyo.canvas.items.getHoldData
import com.angcyo.canvas.items.renderer.BaseItemRenderer
import com.angcyo.gcode.GCodeAdjust
import com.angcyo.gcode.GCodeWriteHandler
import com.angcyo.library.ex.*
import com.angcyo.library.utils.fileName
import com.angcyo.library.utils.filePath
import java.io.File
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * 雕刻助手
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/05/25
 */
object CanvasDataHandleOperate {

    /**缓存文件的文件夹*/
    const val CACHE_FILE_FOLDER = "engrave"

    /**GCode数据, 字符串的文本数据*/
    const val KEY_GCODE = "key_gcode"

    /**SVG数据, List<Path>*/
    const val KEY_SVG = "key_svg"

    /**数据处理的算法模式
     * [com.angcyo.engrave.canvas.CanvasBitmapHandler.BITMAP_MODE_PRINT]
     * [com.angcyo.engrave.canvas.CanvasBitmapHandler.BITMAP_MODE_GCODE]
     * [com.angcyo.engrave.canvas.CanvasBitmapHandler.BITMAP_MODE_BLACK_WHITE]
     * [com.angcyo.engrave.canvas.CanvasBitmapHandler.BITMAP_MODE_DITHERING]
     * [com.angcyo.engrave.canvas.CanvasBitmapHandler.BITMAP_MODE_GREY]
     * [com.angcyo.engrave.canvas.CanvasBitmapHandler.BITMAP_MODE_SEAL]
     * */
    const val KEY_DATA_MODE = "key_data_mode"

    fun _defaultGCodeOutputFile() = filePath("GCode", fileName(suffix = ".gcode")).file()

    /**将路径描边转换为G1代码. 输出的GCode可以直接打印
     * [path] 需要转换的路径, 缩放后的path, 但是没有旋转
     * [rotateBounds] 路径需要平移的left, top
     * [rotate] 路径需要旋转的角度
     * [outputFile] GCode输出路径
     * [pathStep] 路径枚举步长
     * */
    fun pathStrokeToGCode(
        path: Path,
        rotateBounds: RectF,
        rotate: Float,
        pathStep: Float = 1f,
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
        val gCodeHandler = GCodeWriteHandler()
        val mmValueUnit = MmValueUnit()
        outputFile.writer().use { writer ->
            gCodeHandler.gapValue = 0f
            gCodeHandler.pathStrokeToGCode(
                path,
                mmValueUnit,
                writer,
                offsetLeft,
                offsetTop,
                pathStep
            )
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
        outputFile: File = _defaultGCodeOutputFile(),
        offsetLeft: Float = 0f, //偏移的像素
        offsetTop: Float = 0f,
        pathStep: Float = 1f
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
        val mmValueUnit = MmValueUnit()
        outputFile.writer().use { writer ->
            gCodeHandler.gapValue = 0f
            gCodeHandler.pathStrokeToGCode(
                newPathList,
                mmValueUnit,
                writer,
                offsetLeft,
                offsetTop,
                pathStep
            )
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
     * [gravity] 线的扫描方向, 为null, 自动选择. 宽图使用[Gravity.LEFT], 长图使用[Gravity.TOP]
     * [Gravity.LEFT]:垂直从左开始上下下上扫描 [Gravity.RIGHT]:
     * [Gravity.TOP]:水平从上开始左右右左扫描 [Gravity.BOTTOM]:
     *
     * [threshold] 当色值>=此值时, 忽略数据 255白色 [0~255]
     * [lineSpace] 每一行之间的间隙, 毫米单位. (像素采样分辨率, 间隔多少个像素扫描) //1K:0.1 2K:0.05 4K:0.025f
     *             如果是文本信息, 建议使用 4K: [GCodeWriteHandler.GCODE_SPACE_4K]
     * */
    fun bitmapToGCode(
        bitmap: Bitmap,
        gravity: Int? = null,
        lineSpace: Float = GCodeWriteHandler.GCODE_SPACE_1K,
        gapValue: Float = GCodeWriteHandler.GCODE_SPACE_GAP,
        threshold: Int = 255,
        outputFile: File = _defaultGCodeOutputFile()
    ): File {
        val gCodeWriteHandler = GCodeWriteHandler()
        gCodeWriteHandler.gapValue = gapValue

        val width = bitmap.width
        val height = bitmap.height
        val data = bitmap.engraveColorBytes()

        val scanGravity = gravity ?: if (width > height) {
            //宽图
            Gravity.LEFT
        } else {
            Gravity.TOP
        }

        //像素单位转成mm单位
        val mmValueUnit = MmValueUnit()

        //转成像素值
        var lineStep = mmValueUnit.convertValueToPixel(lineSpace).roundToInt()
        lineStep = max(1, lineStep) //最小为1个像素差距

        //反向读取数据, Z形方式
        var isReverseDirection = false
        //最后的有效数据坐标
        var lastGCodeLineRef: Int = -1

        outputFile.writer().use { writer ->
            //最后的坐标
            var lastLineRef = 0

            gCodeWriteHandler.writeFirst(writer, mmValueUnit)

            if (scanGravity == Gravity.LEFT || scanGravity == Gravity.RIGHT) {
                //垂直扫描
                val xFrom: Int
                val xTo: Int
                val xStep: Int //跳跃的像素

                if (scanGravity == Gravity.LEFT) {
                    xFrom = 0
                    xTo = width - 1
                    xStep = lineStep
                } else {
                    xFrom = width - 1
                    xTo = 0
                    xStep = -lineStep
                }

                var currentX = xFrom
                while (true) {//列
                    lastLineRef = currentX + 1
                    for (y in 0 until height) {//行
                        //rtl
                        val lineY = if (isReverseDirection) {
                            (height - 1 - y)
                        } else {
                            y
                        }
                        val index = max(0, (lineY - 1)) * width + currentX
                        val value: Int = data[index].toHexInt()
                        if (value < threshold) {
                            //有效的像素
                            val yValue = mmValueUnit.convertPixelToValue(lineY.toFloat())
                            val xValue = mmValueUnit.convertPixelToValue(lastLineRef.toFloat())
                            gCodeWriteHandler.writeLine(writer, xValue, yValue)
                            lastGCodeLineRef = lastLineRef //有数据的列
                        }
                    }
                    gCodeWriteHandler._writeLastG1(writer)

                    //rtl
                    if (lastGCodeLineRef == lastLineRef) {
                        //这一行有GCode数据
                        isReverseDirection = !isReverseDirection
                    }

                    //到底了
                    if (currentX == xTo) {
                        break
                    } else {
                        //最后一行校验, 忽略step的值
                        currentX += xStep
                        if (currentX + 1 >= width) {
                            currentX = width - 1
                        } else if (currentX + 1 <= 0) {
                            currentX = 0
                        }
                    }
                }
            } else {
                //水平扫描, 第几行. 从0开始
                val yFrom: Int
                val yTo: Int
                val yStep: Int //跳跃的像素

                if (scanGravity == Gravity.TOP) {
                    yFrom = 0
                    yTo = height - 1
                    yStep = lineStep
                } else {
                    yFrom = height - 1
                    yTo = 0
                    yStep = -lineStep
                }

                var currentY = yFrom
                while (true) {//行
                    lastLineRef = currentY + 1
                    for (x in 0 until width) {//列
                        //rtl
                        val lineX = if (isReverseDirection) {
                            (width - 1 - x)
                        } else {
                            x
                        }
                        val index = currentY * width + lineX
                        val value: Int = data[index].toHexInt()
                        if (value < threshold) {
                            //有效的像素
                            val xValue = mmValueUnit.convertPixelToValue(lineX.toFloat())
                            val yValue = mmValueUnit.convertPixelToValue(lastLineRef.toFloat())
                            gCodeWriteHandler.writeLine(writer, xValue, yValue)
                            lastGCodeLineRef = lastLineRef //有数据的行
                        }
                    }
                    gCodeWriteHandler._writeLastG1(writer)

                    //rtl
                    if (lastGCodeLineRef == lastLineRef) {
                        //这一行有GCode数据
                        isReverseDirection = !isReverseDirection
                    }

                    //到底了
                    if (currentY == yTo) {
                        break
                    } else {
                        //最后一行校验, 忽略step的值
                        currentY += yStep
                        if (currentY + 1 >= height) {
                            currentY = height - 1
                        } else if (currentY + 1 <= 0) {
                            currentY = 0
                        }
                    }
                }
            }
            gCodeWriteHandler.writeFinish(writer)
        }
        return outputFile
    }
}

/**获取渲染器对应的GCode数据, 如果有*/
fun BaseItemRenderer<*>.getGCodeText(): String? {
    return getRendererItem()?.getHoldData(CanvasDataHandleOperate.KEY_GCODE)
}

/**获取渲染器对应的List<Path>数据, 如果有*/
fun BaseItemRenderer<*>.getPathList(): List<Path>? {
    return getRendererItem()?.getHoldData(CanvasDataHandleOperate.KEY_SVG)
}