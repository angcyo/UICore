package com.angcyo.path

import android.graphics.Path
import android.graphics.PointF
import androidx.annotation.OptIn
import androidx.core.os.BuildCompat
import androidx.graphics.path.PathIterator
import androidx.graphics.path.PathSegment
import androidx.graphics.path.iterator
import com.angcyo.library.L
import com.angcyo.library.ex.toListOf
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
                        }
                        writer.append((point ?: lastPoint).x)
                        writer.append(",")
                        writer.append((point ?: lastPoint).y)
                    }

                    PathSegment.Type.Line -> {
                        writer.append("L")
                        val point = segment.points.getOrNull(1)?.apply {
                            lastPoint.set(x, y)
                        }
                        writer.append((point ?: lastPoint).x)
                        writer.append(",")
                        writer.append((point ?: lastPoint).y)
                    }

                    PathSegment.Type.Quadratic, PathSegment.Type.Conic -> {
                        //二次曲线 转svg
                        writer.append("Q")
                        val controlPoint = segment.points.getOrNull(1) //控制点
                        val point = segment.points.getOrNull(1)?.apply {
                            lastPoint.set(x, y)
                        }
                        writer.append(controlPoint?.x ?: 0f)
                        writer.append(",")
                        writer.append(controlPoint?.y ?: 0f)
                        writer.append(",")
                        writer.append((point ?: lastPoint).x)
                        writer.append(",")
                        writer.append((point ?: lastPoint).y)
                    }

                    PathSegment.Type.Cubic -> {
                        //三次曲线 转svg
                        writer.append("C")
                        val controlPoint1 = segment.points.getOrNull(1) //控制点1
                        val controlPoint2 = segment.points.getOrNull(2) //控制点2
                        val point = segment.points.getOrNull(3)?.apply {
                            lastPoint.set(x, y)
                        } //终点
                        writer.append(controlPoint1?.x ?: 0f)
                        writer.append(",")
                        writer.append(controlPoint1?.y ?: 0f)
                        writer.append(",")
                        writer.append(controlPoint2?.x ?: 0f)
                        writer.append(",")
                        writer.append(controlPoint2?.y ?: 0f)
                        writer.append(",")
                        writer.append((point ?: lastPoint).x)
                        writer.append(",")
                        writer.append((point ?: lastPoint).y)
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
/*
*/
/**转GCode数据*//*
@OptIn(BuildCompat.PrereleaseSdkCheck::class)
fun List<Path>.toGCodePathContent(
    output: File,
    tolerance: Float = 0.1f,
    append: Boolean = false
): File {
    FileOutputStream(output, append).writer().use { writer ->
        val lastPoint = PointF(0f, 0f)
        forEachIndexed { index, path ->
            lastPoint.set(0f, 0f)

            //转成对应的SVG path数据
            for (segment in path.iterator(PathIterator.ConicEvaluation.AsQuadratics, tolerance)) {
                //M L Q A C Z
                when (segment.type) {
                    PathSegment.Type.Move -> {
                        writer.append("G0")
                        val point = segment.points.getOrNull(0)?.apply {
                            lastPoint.set(x, y)
                        }
                        writer.append("X${(point ?: lastPoint).x}")
                        writer.append("Y${(point ?: lastPoint).y}")
                        writer.appendLine()
                    }

                    PathSegment.Type.Line -> {
                        writer.append("G1")
                        val point = segment.points.getOrNull(1)?.apply {
                            lastPoint.set(x, y)
                        }
                        writer.append("X${(point ?: lastPoint).x}")
                        writer.append("Y${(point ?: lastPoint).y}")
                        writer.appendLine()
                    }

                    PathSegment.Type.Quadratic, PathSegment.Type.Conic -> {
                        //二次曲线 转svg
                        writer.append("G2")
                        val startPoint = segment.points.getOrNull(0) //起点
                        val point = segment.points.getOrNull(1)?.apply {
                            lastPoint.set(x, y)
                        }
                        writer.append("I${(startPoint!!.x + (point ?: lastPoint).x) / 2}")
                        writer.append("J${(startPoint.y + (point ?: lastPoint).y) / 2}")
                        writer.append("X${(point ?: lastPoint).x}")
                        writer.append("Y${(point ?: lastPoint).y}")
                        writer.appendLine()
                    }

                    PathSegment.Type.Close -> writer.appendLine("M2")
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

*//**将[Path]转换成gcode数据
 * [tolerance] 近似误差值*//*
fun Path.toGCodePathContent(output: File, tolerance: Float = 0.1f, append: Boolean = false): File {
    return toListOf().toGCodePathContent(output, tolerance, append)
}*/

fun Appendable.append(value: Float): Appendable = append("$value")
