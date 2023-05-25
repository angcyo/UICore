package com.angcyo.path

import android.graphics.Path
import android.graphics.PointF
import androidx.annotation.OptIn
import androidx.core.os.BuildCompat
import androidx.graphics.path.PathIterator
import androidx.graphics.path.PathSegment
import androidx.graphics.path.iterator
import com.angcyo.library.utils.appendValue
import java.io.File
import java.io.FileOutputStream

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/05/25
 */

/**将[Path]转换成svg path数据
 * [tolerance] 近似误差值*/
@OptIn(BuildCompat.PrereleaseSdkCheck::class)
fun Path.toSvgPath(output: File, tolerance: Float = 0.1f): File {
    FileOutputStream(output, false).writer().use { writer ->
        val lastPoint = PointF(0f, 0f)
        //转成对应的SVG path数据
        for (segment in this.iterator(PathIterator.ConicEvaluation.AsQuadratics, tolerance)) {
            //M L Q A C Z
            when (segment.type) {
                PathSegment.Type.Move -> {
                    writer.appendValue("M")
                    val point = segment.points.getOrNull(0)?.apply {
                        lastPoint.set(x, y)
                    }
                    writer.appendValue((point ?: lastPoint).x)
                    writer.appendValue(",")
                    writer.appendValue((point ?: lastPoint).y)
                }

                PathSegment.Type.Line -> {
                    writer.appendValue("L")
                    val point = segment.points.getOrNull(1)?.apply {
                        lastPoint.set(x, y)
                    }
                    writer.appendValue((point ?: lastPoint).x)
                    writer.appendValue(",")
                    writer.appendValue((point ?: lastPoint).y)
                }

                PathSegment.Type.Quadratic, PathSegment.Type.Conic -> {
                    //二次曲线 转svg
                    writer.appendValue("Q")
                    val controlPoint = segment.points.getOrNull(1) //控制点
                    val point = segment.points.getOrNull(1)?.apply {
                        lastPoint.set(x, y)
                    }
                    writer.appendValue(controlPoint?.x ?: 0)
                    writer.appendValue(",")
                    writer.appendValue(controlPoint?.y ?: 0)
                    writer.appendValue(",")
                    writer.appendValue((point ?: lastPoint).x)
                    writer.appendValue(",")
                    writer.appendValue((point ?: lastPoint).y)
                }

                PathSegment.Type.Cubic -> {
                    //三次曲线 转svg
                    writer.appendValue("C")
                    val controlPoint1 = segment.points.getOrNull(1) //控制点1
                    val controlPoint2 = segment.points.getOrNull(2) //控制点2
                    val point = segment.points.getOrNull(3)?.apply {
                        lastPoint.set(x, y)
                    } //终点
                    writer.appendValue(controlPoint1?.x ?: 0)
                    writer.appendValue(",")
                    writer.appendValue(controlPoint1?.y ?: 0)
                    writer.appendValue(",")
                    writer.appendValue(controlPoint2?.x ?: 0)
                    writer.appendValue(",")
                    writer.appendValue(controlPoint2?.y ?: 0)
                    writer.appendValue(",")
                    writer.appendValue((point ?: lastPoint).x)
                    writer.appendValue(",")
                    writer.appendValue((point ?: lastPoint).y)
                }

                PathSegment.Type.Close -> writer.appendValue("Z")
                PathSegment.Type.Done -> Unit
                else -> writer.appendValue(segment)
            }
        }
    }
    return output
}