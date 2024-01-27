package com.angcyo

import android.graphics.Paint
import android.graphics.Path
import com.angcyo.gcode.GCodeWriteHandler
import com.angcyo.library.annotation.Pixel
import com.angcyo.library.component.hawk.LibHawkKeys
import com.angcyo.library.ex.computePathBounds
import com.angcyo.library.ex.toListOf
import com.angcyo.library.model.PointD
import com.angcyo.library.unit.IValueUnit
import com.angcyo.svg.SvgWriteHandler
import com.angcyo.vector.VectorWriteHandler
import java.io.File
import java.io.FileOutputStream
import java.io.StringWriter


/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/04/12
 */

//region ---GCode---

/**
 * 将[Path]转换成GCode字符串内容
 * [style]
 *   [Paint.Style.STROKE]:只输出描边数据
 *   [Paint.Style.FILL]:只输出填充数据
 *   [Paint.Style.FILL_AND_STROKE]:同时输出描边和填充数据
 * [output] 内容输出的文件路径*/
fun List<Path>.toGCodeContent(
    output: File,
    style: Paint.Style = Paint.Style.FILL_AND_STROKE,
    writeFirst: Boolean = true,
    writeLast: Boolean = true,
    offsetLeft: Float = 0f, //x偏移的像素
    offsetTop: Float = 0f, //y偏移的像素
    pathStep: Float = LibHawkKeys._pathAcceptableError, //Path路径的采样步进
    autoCnc: Boolean = false, //是否使用自动CNC
    fillPathStep: Float = 1f, //填充的线距
    fillAngle: Float = 0f, //填充线的角度
): File {
    when (style) {
        Paint.Style.STROKE -> {
            toGCodeStrokeContent(
                output,
                writeFirst,
                writeLast,
                offsetLeft,
                offsetTop,
                pathStep,
                autoCnc,
                false
            )
        }

        Paint.Style.FILL -> {
            toGCodeFillContent(
                output,
                writeFirst,
                writeLast,
                offsetLeft,
                offsetTop,
                pathStep,
                autoCnc,
                false,
                fillPathStep,
                fillAngle,
            )
        }

        else -> {
            toGCodeStrokeContent(
                output,
                writeFirst,
                writeLast,
                offsetLeft,
                offsetTop,
                pathStep,
                autoCnc,
                false
            )
            toGCodeFillContent(
                output,
                writeFirst,
                writeLast,
                offsetLeft,
                offsetTop,
                pathStep,
                autoCnc,
                true,
                fillPathStep,
                fillAngle,
            )
        }
    }
    return output
}

/**只获取描边的数据*/
fun List<Path>.toGCodeStrokeContent(
    output: File,
    writeFirst: Boolean = true,
    writeLast: Boolean = true,
    offsetLeft: Float = 0f, //x偏移的像素
    offsetTop: Float = 0f, //y偏移的像素
    pathStep: Float = LibHawkKeys._pathAcceptableError, //Path路径的采样步进
    autoCnc: Boolean = false, //是否使用自动CNC
    append: Boolean = false, //是否是追加写入文本
): File {
    //转换成GCode
    val gCodeHandler = GCodeWriteHandler()
    gCodeHandler.unit = IValueUnit.MM_UNIT
    gCodeHandler.isAutoCnc = autoCnc
    FileOutputStream(output, append).writer().use { writer ->
        gCodeHandler.writer = writer
        gCodeHandler.pathStrokeToVector(
            this,
            writeFirst,
            writeLast,
            offsetLeft,
            offsetTop,
            pathStep
        )
    }
    return output
}

/**简单的将[Path]转成GCode
 * [lastPoint] 最后一次的点, 如果有*/
fun Path.toGCodeStrokeSingleContent(
    lastPoint: PointD? = null,
    writeFirst: Boolean = false,
    writeLast: Boolean = false,
    action: GCodeWriteHandler.() -> Unit = {}
): String {
    val gCodeHandler = GCodeWriteHandler()
    gCodeHandler.unit = IValueUnit.MM_UNIT
    gCodeHandler.isAutoCnc = false
    gCodeHandler.action()
    //---
    StringWriter().use { writer ->
        gCodeHandler.writer = writer
        lastPoint?.let {
            gCodeHandler._pointList.add(
                VectorWriteHandler.VectorPoint(
                    it.x,
                    it.y,
                    VectorWriteHandler.POINT_TYPE_NEW
                )
            )
        }
        gCodeHandler.pathStrokeToVector(this, writeFirst, writeLast)
        return writer.toString()
    }
}

/**只获取填充的数据*/
fun List<Path>.toGCodeFillContent(
    output: File,
    writeFirst: Boolean = true,
    writeLast: Boolean = true,
    offsetLeft: Float = 0f, //x偏移的像素
    offsetTop: Float = 0f, //y偏移的像素
    pathStep: Float = LibHawkKeys._pathAcceptableError, //Path路径的采样步进
    autoCnc: Boolean = false, //是否使用自动CNC
    append: Boolean = false, //是否是追加写入文本
    fillPathStep: Float = 1f, //填充的线距
    fillAngle: Float = 0f, //填充线的角度
): File {
    //转换成GCode
    val gCodeHandler = GCodeWriteHandler()
    gCodeHandler.unit = IValueUnit.MM_UNIT
    gCodeHandler.isAutoCnc = autoCnc
    FileOutputStream(output, append).writer().use { writer ->
        gCodeHandler.writer = writer
        gCodeHandler.pathFillToVector(
            this,
            writeFirst,
            writeLast,
            offsetLeft,
            offsetTop,
            pathStep,
            fillPathStep,
            fillAngle
        )
    }
    return output
}

//endregion ---GCode---

//region ---SVG---

/**
 * 将[Path]转换成SVG字符串内容
 * [toGCodeContent]*/
fun List<Path>.toSVGContent(
    output: File,
    style: Paint.Style = Paint.Style.FILL_AND_STROKE,
    offsetLeft: Float = 0f, //x偏移的像素
    offsetTop: Float = 0f, //y偏移的像素
    pathStep: Float = LibHawkKeys._pathAcceptableError, //Path路径的采样步进
    fillPathStep: Float = 1f, //填充的线距
    fillAngle: Float = 0f, //填充线的角度
): File {
    when (style) {
        Paint.Style.STROKE -> {
            toSVGStrokeContentStr(
                output,
                offsetLeft,
                offsetTop,
                pathStep,
                false
            )
        }

        Paint.Style.FILL -> {
            toSVGFillContent(
                output,
                offsetLeft,
                offsetTop,
                pathStep,
                false,
                fillPathStep,
                fillAngle,
            )
        }

        else -> {
            toSVGStrokeContentStr(
                output,
                offsetLeft,
                offsetTop,
                pathStep,
                false
            )
            toSVGFillContent(
                output,
                offsetLeft,
                offsetTop,
                pathStep,
                true,
                fillPathStep,
                fillAngle,
            )
        }
    }
    return output
}

/**只获取描边的数据*/
fun List<Path>.toSVGStrokeContentStr(
    output: File,
    offsetLeft: Float = 0f, //x偏移的像素
    offsetTop: Float = 0f, //y偏移的像素
    pathStep: Float = LibHawkKeys.svgTolerance, //Path路径的采样步进
    append: Boolean = false, //是否是追加写入文本
    wrapSvgXml: Boolean = false, //是否使用svg xml文档格式包裹
): File {
    //转换成Svg, 使用像素单位
    val svgWriteHandler = SvgWriteHandler()
    FileOutputStream(output, append).writer().use { writer ->
        svgWriteHandler.writer = writer
        svgWriteHandler.gapValue = 1f
        svgWriteHandler.gapMaxValue = 1f

        if (wrapSvgXml) {
            @Pixel val bounds = computePathBounds()
            writer.write("""<?xml version="1.0" encoding="UTF-8"?><svg xmlns="http://www.w3.org/2000/svg" viewBox="${bounds.left} ${bounds.top} ${bounds.right} ${bounds.bottom}">""")
            writer.write("""<path stroke="black" fill="none" d="""")
        }

        svgWriteHandler.pathStrokeToVector(
            this,
            false,
            true,
            offsetLeft,
            offsetTop,
            pathStep
        )

        if (wrapSvgXml) {
            writer.write(""""/></svg>""")
        }
    }
    return output
}

fun Path.toSVGStrokeContentStr(
    offsetLeft: Float = 0f,
    offsetTop: Float = 0f,
    pathStep: Float = LibHawkKeys._pathAcceptableError,
    action: (SvgWriteHandler) -> Unit = {}
): String {
    return toListOf().toSVGStrokeContentStr(offsetLeft, offsetTop, pathStep, action)
}

fun List<Path>.toSVGStrokeContentStr(
    offsetLeft: Float = 0f, //x偏移的像素
    offsetTop: Float = 0f, //y偏移的像素
    pathStep: Float = LibHawkKeys._pathAcceptableError,
    action: (SvgWriteHandler) -> Unit = {}
): String {
    val writer = StringWriter()
    toSVGStrokeContentStr(writer, offsetLeft, offsetTop, pathStep, action)
    return writer.toString()
}

/**[toSVGStrokeContentStr]*/
fun List<Path>.toSVGStrokeContentStr(
    writer: Appendable,
    offsetLeft: Float = 0f, //x偏移的像素
    offsetTop: Float = 0f, //y偏移的像素
    pathStep: Float = LibHawkKeys._pathAcceptableError, //Path路径的采样步进
    action: (SvgWriteHandler) -> Unit = {}
) {
    //转换成Svg, 使用像素单位
    val svgWriteHandler = SvgWriteHandler()
    svgWriteHandler.writer = writer
    svgWriteHandler.gapValue = 1f
    svgWriteHandler.gapMaxValue = 1f
    action(svgWriteHandler)
    svgWriteHandler.pathStrokeToVector(
        this,
        false,
        true,
        offsetLeft,
        offsetTop,
        pathStep
    )
}

/**只获取填充的数据*/
fun List<Path>.toSVGFillContent(
    output: File,
    offsetLeft: Float = 0f, //x偏移的像素
    offsetTop: Float = 0f, //y偏移的像素
    pathStep: Float = LibHawkKeys._pathAcceptableError, //Path路径的采样步进
    append: Boolean = false, //是否是追加写入文本
    fillPathStep: Float = 1f, //填充的线距
    fillAngle: Float = 0f, //填充线的角度
): File {
    //转换成Svg, 使用像素单位
    val svgWriteHandler = SvgWriteHandler()
    //svgWriteHandler.unit = mmUnit
    FileOutputStream(output, append).writer().use { writer ->
        svgWriteHandler.writer = writer
        svgWriteHandler.gapValue = 1f
        svgWriteHandler.gapMaxValue = 1f
        svgWriteHandler.pathFillToVector(
            this,
            false,
            true,
            offsetLeft,
            offsetTop,
            pathStep,
            fillPathStep,
            fillAngle
        )
    }
    return output
}

//endregion ---SVG---

