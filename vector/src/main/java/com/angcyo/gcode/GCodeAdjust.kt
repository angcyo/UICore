package com.angcyo.gcode

import android.graphics.Matrix
import android.graphics.RectF
import com.angcyo.library.annotation.Pixel
import com.angcyo.library.ex.ensure
import com.angcyo.library.ex.mapPoint
import com.angcyo.library.ex.mapRectF
import com.angcyo.library.ex.toLossyFloat
import com.angcyo.library.model.PointD
import com.angcyo.library.unit.InchValueUnit
import com.angcyo.library.unit.MmValueUnit
import java.io.File
import java.io.OutputStreamWriter

/**
 * GCode数据调整
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/06/10
 */
class GCodeAdjust {

    val gCodeHandler = GCodeHelper.GCodeHandler()

    val mm = MmValueUnit()
    val inch = InchValueUnit()

    val mmValue = mm.convertValueToPixel(1.0)
    val inchValue = inch.convertValueToPixel(1.0)

    /**GCode数据坐标调整, 先缩放旋转,再偏移
     * [gCode]
     * [bounds] 未旋转时的bounds, 像素坐标
     * */
    fun gCodeAdjust(
        gCode: String,
        @Pixel
        bounds: RectF,
        rotate: Float,
        isAutoCnc: Boolean,
        isLast: Boolean,
        outputFile: File,
    ): File {
        gCodeHandler.reset()
        val config = GCodeParseConfig(gCode, mmValue, inchValue)
        val gCodeLineList = GCodeHelper.parseGCodeLineList(config)
        gCodeHandler.parseGCodeBound(gCodeLineList)

        //G Bounds
        val gCodeBounds = gCodeHandler.gCodeBounds
        val scaleX = (bounds.width() / gCodeBounds.width()).ensure(1f)
        val scaleY = (bounds.height() / gCodeBounds.height()).ensure(1f)

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
            if (gCodeLineList.firstOrNull()?.notFoundMCmd == true) {
                writer.appendLine("M04 S255")
            }
            gCodeHandler.transformPoint = { gCodeLineData, pointF ->
                matrix.mapPoint(pointF, pointF)
            }
            gCodeHandler.overrideGCommand = { line, firstCmd, xy, ij ->
                overrideGCommand(writer, line, firstCmd, xy, ij)
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
            if (isLast) {
                //平移完之后, 写入G0 0 0
                GCodeWriteHandler().apply {
                    this.writer = writer
                    this.isAutoCnc = isAutoCnc
                    onPathEnd(true)
                }
            }
        }
        return outputFile
    }

    /**GCode平移
     * [left] 需要平移到的x坐标像素
     * [top] 需要平移到的y坐标像素
     * */
    fun gCodeTranslation(
        gCode: String,
        @Pixel left: Float,
        @Pixel top: Float,
        writer: OutputStreamWriter
    ) {
        gCodeHandler.reset()
        val config = GCodeParseConfig(gCode, mmValue, inchValue)
        val gCodeLineList = GCodeHelper.parseGCodeLineList(config)
        gCodeHandler.parseGCodeBound(gCodeLineList)

        val gCodeBounds = gCodeHandler.gCodeBounds
        //偏移
        @Pixel
        val offsetLeft = left - gCodeBounds.left

        @Pixel
        val offsetTop = top - gCodeBounds.top

        if (gCodeLineList.firstOrNull()?.notFoundMCmd == true) {
            writer.appendLine("M04 S255")
        }

        gCodeHandler.transformPoint = { gCodeLineData, pointF ->
            //这里还是像素单位, 但是override的时候, 统一转成对应单位
            pointF.x += offsetLeft
            pointF.y += offsetTop
        }
        gCodeHandler.overrideGCommand = { line, firstCmd, xy, ij ->
            overrideGCommand(writer, line, firstCmd, xy, ij)
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
        line: GCodeLineData,
        firstCmd: GCodeCmd,
        xy: PointD,
        ij: PointD?
    ) {
        //像素值, 转换成mm/in

        var x = xy.x + 0.0
        var y = xy.y + 0.0

        if (firstCmd.ratio == inchValue) {
            //inch单位
            x = inch.convertPixelToValue(x)
            y = inch.convertPixelToValue(y)
        } else {
            x = mm.convertPixelToValue(x)
            y = mm.convertPixelToValue(y)
        }

        if (ij == null) {
            writer.append("${firstCmd.code} X${x.toLossyFloat()} Y${y.toLossyFloat()}")
        } else {
            var i = ij.x + 0.0
            var j = ij.y + 0.0

            if (firstCmd.ratio == inchValue) {
                //inch单位
                i = inch.convertPixelToValue(i)
                j = inch.convertPixelToValue(j)
            } else {
                i = mm.convertPixelToValue(i)
                j = mm.convertPixelToValue(j)
            }

            //保留4位小数点
            writer.append("${firstCmd.code} ")
            //writer.append("X${x.decimal(4)} Y${y.decimal(4)} ")
            //writer.appendLine("I${i.decimal(4)} J${j.decimal(4)}")
            writer.append("X${x.toLossyFloat()} Y${y.toLossyFloat()} ")
            writer.append("I${i.toLossyFloat()} J${j.toLossyFloat()}")
        }

        //其他指令, 原封不动追加上去
        line.cmdList.forEach { cmdData ->
            val cmd = cmdData.cmd
            if (cmd == "G" ||
                cmd == "X" ||
                cmd == "Y" ||
                cmd == "I" ||
                cmd == "J"
            ) {
                //no op
            } else {
                writer.append(" ")//空格隔开, 很关键
                writer.append(cmdData.code)
            }
        }
        writer.appendLine()
    }

}