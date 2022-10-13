package com.angcyo.canvas.utils

import android.graphics.*
import android.view.Gravity
import com.angcyo.canvas.data.ItemDataBean.Companion.mmUnit
import com.angcyo.gcode.GCodeAdjust
import com.angcyo.gcode.GCodeWriteHandler
import com.angcyo.library.ex.*
import com.angcyo.library.utils.fileNameTime
import com.angcyo.library.utils.filePath
import com.angcyo.svg.SvgWriteHandler
import com.angcyo.vector.VectorWriteHandler
import java.io.File
import kotlin.math.max

/**
 * 雕刻助手
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/05/25
 */
object CanvasDataHandleOperate {

    /**雕刻缓存文件的文件夹*/
    const val ENGRAVE_CACHE_FILE_FOLDER = "engrave"

    /**GCode/Svg矢量缓存目录*/
    const val VECTOR_CACHE_FILE_FOLDER = "vector"

    //---

    fun _defaultGCodeOutputFile() =
        filePath(VECTOR_CACHE_FILE_FOLDER, fileNameTime(suffix = ".gcode")).file()

    fun _defaultSvgOutputFile() =
        filePath(VECTOR_CACHE_FILE_FOLDER, fileNameTime(suffix = ".svg")).file()

    //---

    /**将路径集合转换成GCode. 输出的GCode可以直接打印
     * [pathList] 未缩放旋转的原始路径数据
     * [bounds] 未旋转时的bounds, 用来实现缩放
     * [rotate] 旋转角度, 配合[bounds]实现平移
     *
     * 支持[Path]的旋转和平移
     * [com.angcyo.canvas.utils.CanvasDataHandleOperate.pathStrokeToGCode]
     * */
    fun pathStrokeToGCode(
        pathList: List<Path>,
        bounds: RectF,
        rotate: Float,
        outputFile: File = _defaultGCodeOutputFile(),
        writeFirst: Boolean = true,
        writeLast: Boolean = true,
        offsetLeft: Float = 0f, //偏移的像素
        offsetTop: Float = 0f,
        pathStep: Float = 1f,
    ): File {
        val newPathList = pathList.transform(bounds, rotate)
        //转换成GCode
        val gCodeHandler = GCodeWriteHandler()
        gCodeHandler.unit = mmUnit
        outputFile.writer().use { writer ->
            gCodeHandler.writer = writer
            gCodeHandler.gapValue = 0f
            gCodeHandler.pathStrokeToVector(
                newPathList,
                writeFirst,
                writeLast,
                offsetLeft,
                offsetTop,
                pathStep
            )
        }
        return outputFile
    }

    fun pathFillToGCode(
        pathList: List<Path>,
        bounds: RectF,
        rotate: Float,
        outputFile: File = _defaultGCodeOutputFile(),
        writeFirst: Boolean = true,
        writeLast: Boolean = true,
        offsetLeft: Float = 0f, //偏移的像素
        offsetTop: Float = 0f,
        pathStep: Float = 1f,
    ): File {
        val newPathList = pathList.transform(bounds, rotate)
        //转换成GCode
        val gCodeHandler = GCodeWriteHandler()
        gCodeHandler.unit = mmUnit
        outputFile.writer().use { writer ->
            gCodeHandler.writer = writer
            gCodeHandler.gapValue = 0f
            gCodeHandler.pathFillToVector(
                newPathList,
                writeFirst,
                writeLast,
                offsetLeft,
                offsetTop,
                pathStep
            )
        }
        return outputFile
    }

    //---

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
        gapValue: Float = VectorWriteHandler.PATH_SPACE_GAP,
        threshold: Int = 255, //255白色不输出GCode
        outputFile: File = _defaultGCodeOutputFile(),
        isFirst: Boolean = true,
        isFinish: Boolean = true,
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
        val mmValueUnit = mmUnit
        gCodeWriteHandler.unit = mmValueUnit

        val lineStep = 1

        //反向读取数据, Z形方式
        var isReverseDirection = false
        //最后的有效数据坐标
        var lastGCodeLineRef: Int = -1

        outputFile.writer().use { writer ->
            gCodeWriteHandler.writer = writer

            //最后的坐标
            var lastLineRef = 0

            if (isFirst) {
                gCodeWriteHandler.onPathStart(0f, 0f)
            }

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
                            gCodeWriteHandler.writePoint(xValue, yValue)
                            lastGCodeLineRef = lastLineRef //有数据的列
                        }
                    }
                    gCodeWriteHandler.checkLastPoint()

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
                            gCodeWriteHandler.writePoint(xValue, yValue)
                            lastGCodeLineRef = lastLineRef //有数据的行
                        }
                    }
                    gCodeWriteHandler.checkLastPoint()

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

            if (isFinish) {
                gCodeWriteHandler.onPathEnd()
            }
        }
        return outputFile
    }

    //---

    /**
     * [pathStrokeToGCode]
     * [pathFillToGCode]
     * */
    fun pathStrokeToSvg(
        pathList: List<Path>,
        bounds: RectF,
        rotate: Float,
        outputFile: File = _defaultSvgOutputFile(),
        writeFirst: Boolean = true,
        writeLast: Boolean = true,
        offsetLeft: Float = 0f, //偏移的像素
        offsetTop: Float = 0f,
        pathStep: Float = 1f,
    ): File {
        val newPathList = pathList.transform(bounds, rotate)
        //转换成Svg, 使用像素单位
        val svgWriteHandler = SvgWriteHandler()
        //svgWriteHandler.unit = mmUnit
        outputFile.writer().use { writer ->
            svgWriteHandler.writer = writer
            svgWriteHandler.gapValue = 0f
            svgWriteHandler.pathStrokeToVector(
                newPathList,
                writeFirst,
                writeLast,
                offsetLeft,
                offsetTop,
                pathStep
            )
        }
        return outputFile
    }

    fun pathFillToSvg(
        pathList: List<Path>,
        bounds: RectF,
        rotate: Float,
        outputFile: File = _defaultSvgOutputFile(),
        writeFirst: Boolean = true,
        writeLast: Boolean = true,
        offsetLeft: Float = 0f, //偏移的像素
        offsetTop: Float = 0f,
        pathStep: Float = 1f,
    ): File {
        val newPathList = pathList.transform(bounds, rotate)
        //转换成Svg, 使用像素单位
        val svgWriteHandler = SvgWriteHandler()
        //svgWriteHandler.unit = mmUnit
        outputFile.writer().use { writer ->
            svgWriteHandler.writer = writer
            svgWriteHandler.gapValue = 0f
            svgWriteHandler.pathFillToVector(
                newPathList,
                writeFirst,
                writeLast,
                offsetLeft,
                offsetTop,
                pathStep
            )
        }
        return outputFile
    }

}