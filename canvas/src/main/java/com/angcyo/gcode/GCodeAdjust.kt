package com.angcyo.gcode

import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.RectF
import com.angcyo.canvas.core.InchValueUnit
import com.angcyo.canvas.core.MmValueUnit
import com.angcyo.canvas.utils.mapPoint
import com.angcyo.canvas.utils.mapRectF
import java.io.File
import java.io.OutputStreamWriter
import kotlin.math.min

/**
 * GCode数据调整
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/06/10
 */
class GCodeAdjust {

    val gCodeHandler = GCodeHelper.GCodeHandler()

    val mm = MmValueUnit()
    val inch = InchValueUnit()

    val mmValue = mm.convertValueToPixel(1f)
    val inchValue = inch.convertValueToPixel(1f)

    /**GCode数据坐标调整, 先缩放旋转,再偏移
     * [gCode]
     * [bounds] 未旋转时的bounds
     * */
    fun gCodeAdjust(
        gCode: String,
        bounds: RectF,
        rotate: Float,
        outputFile: File
    ): File {
        gCodeHandler.reset()
        val config = GCodeParseConfig(gCode, mmValue, inchValue)
        val gCodeLineList = GCodeHelper.parseGCodeLineList(config)
        gCodeHandler.parseGCodeBound(gCodeLineList)

        //G Bounds
        val gCodeBounds = gCodeHandler.gCodeBounds
        val scaleX = bounds.width() / gCodeBounds.width()
        val scaleY = bounds.height() / gCodeBounds.height()

        //先缩放和旋转
        val matrix = Matrix()
        val rotateBounds = RectF()//
        if (rotate != 0f) {
            matrix.setRotate(rotate, bounds.centerX(), bounds.centerY())
        }
        matrix.mapRectF(bounds, rotateBounds)

        matrix.reset()
        matrix.setScale(scaleX, scaleY, gCodeBounds.left, gCodeBounds.top)
        if (rotate != 0f) {
            matrix.postRotate(
                rotate,
                gCodeBounds.left + bounds.width() / 2,
                gCodeBounds.top + bounds.height() / 2
            )//转换
        }

        outputFile.writer().use { writer ->
            gCodeHandler.transformPoint = { gCodeLineData, pointF ->
                matrix.mapPoint(pointF, pointF)
            }
            gCodeHandler.overrideGCommand = { firstCmd, xy, ij ->
                overrideGCommand(writer, firstCmd, xy, ij)
            }
            gCodeHandler.overrideCommand = { line ->
                writer.appendLine(line.lineCode)
            }
            gCodeHandler.parseGCodeLineList(gCodeLineList)
        }

        //再平移
        val gcode = outputFile.readText()
        outputFile.writer().use { writer ->
            gCodeTranslation(gcode, rotateBounds.left, rotateBounds.top, writer)
        }
        return outputFile
    }

    /**GCode平移
     * [left] 需要平移到的x坐标像素
     * [top] 需要平移到的y坐标像素
     * */
    fun gCodeTranslation(gCode: String, left: Float, top: Float, writer: OutputStreamWriter) {
        gCodeHandler.reset()
        val config = GCodeParseConfig(gCode, mmValue, inchValue)
        val gCodeLineList = GCodeHelper.parseGCodeLineList(config)
        gCodeHandler.parseGCodeBound(gCodeLineList)

        val gCodeBounds = gCodeHandler.gCodeBounds
        //偏移
        val offsetLeft = left - min(0f, gCodeBounds.left)
        val offsetTop = top - min(0f, gCodeBounds.top)

        gCodeHandler.transformPoint = { gCodeLineData, pointF ->
            pointF.x += offsetLeft
            pointF.y += offsetTop
        }
        gCodeHandler.overrideGCommand = { firstCmd, xy, ij ->
            overrideGCommand(writer, firstCmd, xy, ij)
        }
        gCodeHandler.overrideCommand = { line ->
            writer.appendLine(line.lineCode)
        }
        //触发
        gCodeHandler.parseGCodeLineList(gCodeLineList)
    }

    /**
     * [xy] 像素
     * [ij] 像素
     * */
    private fun overrideGCommand(
        writer: OutputStreamWriter,
        firstCmd: GCodeCmd,
        xy: PointF,
        ij: PointF?
    ) {
        //像素值, 转换成mm/in

        var x = xy.x
        var y = xy.y

        if (firstCmd.ratio == inchValue) {
            //inch单位
            x = inch.convertPixelToValue(x)
            y = inch.convertPixelToValue(y)
        } else {
            x = mm.convertPixelToValue(x)
            y = mm.convertPixelToValue(y)
        }

        if (ij == null) {
            writer.appendLine("${firstCmd.code} X$x Y$y")
        } else {
            var i = ij.x
            var j = ij.y

            if (firstCmd.ratio == inchValue) {
                //inch单位
                i = inch.convertPixelToValue(i)
                j = inch.convertPixelToValue(j)
            } else {
                i = mm.convertPixelToValue(i)
                j = mm.convertPixelToValue(j)
            }

            writer.appendLine("${firstCmd.code} X$x Y$y I$i J$j")
        }
    }

}