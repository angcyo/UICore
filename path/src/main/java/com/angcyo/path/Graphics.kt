package com.angcyo.path

import android.graphics.Path
import android.graphics.PointF
import android.os.Build
import androidx.annotation.OptIn
import androidx.core.os.BuildCompat
import androidx.graphics.path.PathIterator
import androidx.graphics.path.PathSegment
import androidx.graphics.path.iterator
import com.angcyo.gcode.GCodeWriteHandler
import com.angcyo.library.L
import com.angcyo.library.ex.toListOf
import com.angcyo.library.unit.IValueUnit
import com.angcyo.library.unit.toMm
import com.angcyo.toGCodeStrokeSingleContent
import java.io.File
import java.io.FileOutputStream

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/05/25
 */

@OptIn(BuildCompat.PrereleaseSdkCheck::class)
fun List<Path>.toSvgPathContent(
    output: File,
    tolerance: Float = 0.1f,
    append: Boolean = false
): File {
    val lastPoint = PointF(0f, 0f)
    FileOutputStream(output, append).writer().use { writer ->
        forEachIndexed { index, path ->
            lastPoint.set(0f, 0f)
            //转成对应的SVG path数据
            for (segment in path.iterator(PathIterator.ConicEvaluation.AsQuadratics, tolerance)) {
                //M L Q A C Z
                when (segment.type) {
                    PathSegment.Type.Move -> {
                        writer.append("M")
                        val point = segment.points.getOrNull(0)?.apply {
                            lastPoint.set(x, y)
                        } ?: lastPoint
                        writer.append(point.x)
                        writer.append(",")
                        writer.append(point.y)
                    }

                    PathSegment.Type.Line -> {
                        writer.append("L")
                        val endPoint = segment.points.getOrNull(1)?.apply {
                            lastPoint.set(x, y)
                        } ?: lastPoint
                        writer.append(endPoint.x)
                        writer.append(",")
                        writer.append(endPoint.y)
                    }

                    PathSegment.Type.Quadratic, PathSegment.Type.Conic -> {
                        //二次曲线 转svg
                        writer.append("Q")
                        val controlPoint = segment.points.getOrNull(1) //控制点
                        val endPoint = segment.points.getOrNull(2)?.apply {
                            lastPoint.set(x, y)
                        } ?: lastPoint
                        writer.append(controlPoint?.x ?: 0f)
                        writer.append(",")
                        writer.append(controlPoint?.y ?: 0f)
                        writer.append(",")
                        writer.append(endPoint.x)
                        writer.append(",")
                        writer.append(endPoint.y)
                    }

                    PathSegment.Type.Cubic -> {
                        //三次曲线 转svg
                        writer.append("C")
                        val controlPoint1 = segment.points.getOrNull(1) //控制点1
                        val controlPoint2 = segment.points.getOrNull(2) //控制点2
                        val endPoint = segment.points.getOrNull(3)?.apply {
                            lastPoint.set(x, y)
                        } ?: lastPoint //终点
                        writer.append(controlPoint1?.x ?: 0f)
                        writer.append(",")
                        writer.append(controlPoint1?.y ?: 0f)
                        writer.append(",")
                        writer.append(controlPoint2?.x ?: 0f)
                        writer.append(",")
                        writer.append(controlPoint2?.y ?: 0f)
                        writer.append(",")
                        writer.append(endPoint.x)
                        writer.append(",")
                        writer.append(endPoint.y)
                    }

                    PathSegment.Type.Close -> writer.append("Z")
                    PathSegment.Type.Done -> Unit
                    else -> {
                        L.w("不支持的数据:$segment")
                    }
                }
            }
        }
    }

    return output
}

/**将[Path]转换成svg path数据
 * [tolerance] 近似误差值*/
fun Path.toSvgPathContent(output: File, tolerance: Float = 0.1f, append: Boolean = false): File {
    return toListOf().toSvgPathContent(output, tolerance, append)
}

/**转GCode数据*/
@OptIn(BuildCompat.PrereleaseSdkCheck::class)
fun List<Path>.toGCodePathContent(
    output: File,
    tolerance: Float = 0.1f,
    append: Boolean = false,
    isAutoCnc: Boolean = false,
    writeFirst: Boolean = true,
    writeLast: Boolean = true,
): File {
    val gCodeWriteHandler = GCodeWriteHandler()
    gCodeWriteHandler.unit = IValueUnit.MM_UNIT
    gCodeWriteHandler.isAutoCnc = isAutoCnc
    FileOutputStream(output, append).writer().use { writer ->
        gCodeWriteHandler.writer = writer
        if (writeFirst) {
            gCodeWriteHandler.onPathStart()
        }
        val lastPoint = PointF(0f, 0f)
        forEachIndexed { index, path ->
            lastPoint.set(0f, 0f)

            //转成对应的SVG path数据
            for (segment in path.iterator(PathIterator.ConicEvaluation.AsQuadratics, tolerance)) {
                //M L Q A C Z
                when (segment.type) {
                    PathSegment.Type.Move -> {
                        gCodeWriteHandler.closeCnc()
                        writer.append("G0")
                        val point = segment.points.getOrNull(0)?.apply {
                            lastPoint.set(x, y)
                        } ?: lastPoint
                        point.toMm()
                        writer.append("X${point.x}")
                        writer.append("Y${point.y}")
                        writer.appendLine()
                    }

                    PathSegment.Type.Line -> {
                        gCodeWriteHandler.openCnc()
                        writer.append("G1")
                        val endPoint = segment.points.getOrNull(1)?.apply {
                            lastPoint.set(x, y)
                        } ?: lastPoint
                        endPoint.toMm()
                        writer.append("X${endPoint.x}")
                        writer.append("Y${endPoint.y}")
                        writer.appendLine()
                    }

                    PathSegment.Type.Quadratic, PathSegment.Type.Conic, PathSegment.Type.Cubic -> {
                        //二次曲线 转svg
                        val temp = Path()
                        val p1 = segment.points[0]
                        val p2 = segment.points[1]
                        val p3 = segment.points[2]

                        temp.moveTo(p1.x, p1.y)
                        if (segment.type == PathSegment.Type.Quadratic) {
                            temp.quadTo(p2.x, p2.y, p3.x, p3.y)
                        } else if (segment.type == PathSegment.Type.Conic) {
                            if (Build.VERSION.SDK_INT >= 34) {
                                temp.conicTo(p2.x, p2.y, p3.x, p3.y, segment.weight)
                            }
                        } else {
                            val p4 = segment.points[3]
                            temp.cubicTo(p2.x, p2.y, p3.x, p3.y, p4.x, p4.y)
                        }
                        gCodeWriteHandler.openCnc()
                        writer.append(temp.toGCodeStrokeSingleContent())
                    }

                    PathSegment.Type.Close -> {
                        gCodeWriteHandler.closeCnc()
                    }

                    PathSegment.Type.Done -> Unit
                    else -> {
                        L.w("不支持的数据:$segment")
                    }
                }
            }
        }
        if (writeLast) {
            gCodeWriteHandler.onPathEnd(true)
        }
    }
    return output
}

/**将[Path]转换成gcode数据
 * [tolerance] 近似误差值*/
fun Path.toGCodePathContent(output: File, tolerance: Float = 0.1f, append: Boolean = false): File {
    return toListOf().toGCodePathContent(output, tolerance, append)
}

fun Appendable.append(value: Float): Appendable = append("$value")
