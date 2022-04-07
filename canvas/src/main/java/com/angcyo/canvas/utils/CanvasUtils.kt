package com.angcyo.canvas.utils

import android.graphics.*
import android.text.Layout
import android.text.TextPaint
import com.angcyo.library.ex.dp
import kotlin.math.max
import kotlin.math.min

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/01
 */

/**临时对象, 用来存储[Matrix]矩阵值*/
val _tempValues = floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)

/**临时对象, 用来存储坐标点位值*/
val _tempPoints = floatArrayOf(0f, 0f)

val _tempPoint = PointF()

/**临时对象, 用来存储矩形坐标*/
val _tempRectF = RectF()

/**临时对象, 用来存储[Matrix]*/
val _tempMatrix = Matrix()

/**创建一个画笔*/
fun createPaint(color: Int = Color.GRAY, style: Paint.Style = Paint.Style.STROKE) =
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        this.style = style
        strokeWidth = 1f
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

fun createTextPaint(color: Int = Color.BLACK, textSize: Float = 12 * dp) =
    TextPaint(createPaint(color, Paint.Style.FILL)).apply {
        this.textSize = textSize
        this.textAlign = Paint.Align.LEFT
    }

//<editor-fold desc="Matrix">

/**当前矩阵, 偏移的x*/
fun Matrix.getTranslateX(): Float {
    getValues(_tempValues)
    return _tempValues[Matrix.MTRANS_X]
}

fun Matrix.getTranslateY(): Float {
    getValues(_tempValues)
    return _tempValues[Matrix.MTRANS_Y]
}

fun Matrix.setTranslateValue(x: Float, y: Float) {
    getValues(_tempValues)
    _tempValues[Matrix.MTRANS_X] = x
    _tempValues[Matrix.MTRANS_Y] = y
    setValues(_tempValues)
}

fun Matrix.setScaleValue(x: Float, y: Float) {
    getValues(_tempValues)
    _tempValues[Matrix.MSCALE_X] = x
    _tempValues[Matrix.MSCALE_Y] = y
    setValues(_tempValues)
}

/**当前矩阵, 缩放的比例. 默认是1f
 * 比如1.2f 2.0f*/
fun Matrix.getScaleX(): Float {
    getValues(_tempValues)
    return _tempValues[Matrix.MSCALE_X]
}

/**默认是1f*/
fun Matrix.getScaleY(): Float {
    getValues(_tempValues)
    return _tempValues[Matrix.MSCALE_Y]
}

/**[PointF]*/
fun Matrix.mapPoint(point: PointF): PointF {
    _tempPoints[0] = point.x
    _tempPoints[1] = point.y
    mapPoints(_tempPoints, _tempPoints)
    _tempPoint.x = _tempPoints[0]
    _tempPoint.y = _tempPoints[1]
    return _tempPoint
}

/**[RectF]*/
fun Matrix.mapRectF(rect: RectF): RectF {
    mapRect(_tempRectF, rect)
    return _tempRectF
}

fun Matrix.mapXValueList(xList: List<Float>): List<Float> {
    val src = FloatArray(xList.size * 2)
    val dst = FloatArray(src.size)

    xList.forEachIndexed { index, x ->
        src[index * 2] = x
        src[index * 2 + 1] = 0f
    }

    mapPoints(dst, src)
    val result = mutableListOf<Float>()
    for (i in xList.indices) {
        result.add(dst[i * 2])
    }
    return result
}

fun Matrix.mapYValueList(yList: List<Float>): List<Float> {
    val src = FloatArray(yList.size * 2)
    val dst = FloatArray(src.size)

    yList.forEachIndexed { index, y ->
        src[index * 2] = 0f
        src[index * 2 + 1] = y
    }

    mapPoints(dst, src)
    val result = mutableListOf<Float>()
    for (i in yList.indices) {
        result.add(dst[i * 2])
    }
    return result
}

//</editor-fold desc="Matrix">

/**将[value]限制在[min] [max]之间*/
fun clamp(value: Float, min: Float, max: Float): Float = min(max(value, min), max)

fun Layout.getMaxLineWidth(): Float {
    var width = 0f
    for (line in 0 until lineCount) {
        width = max(width, getLineWidth(line))
    }
    return width
}
