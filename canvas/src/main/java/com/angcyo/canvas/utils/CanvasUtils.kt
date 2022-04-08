package com.angcyo.canvas.utils

import android.graphics.*
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.angcyo.library.ex.dp
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToLong

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/01
 */

//<editor-fold desc="临时变量">

/**临时对象, 用来存储[Matrix]矩阵值*/
val _tempValues = floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)

/**临时对象, 用来存储坐标点位值*/
val _tempPoints = floatArrayOf(0f, 0f)

val _tempPoint = PointF()

/**临时对象, 用来存储矩形坐标*/
val _tempRectF = RectF()

/**临时对象, 用来存储[Matrix]*/
val _tempMatrix = Matrix()

//</editor-fold desc="临时变量">

//<editor-fold desc="canvas">

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

/**[StaticLayout]*/
fun createStaticLayout(
    source: CharSequence,
    paint: TextPaint,
    width: Int,
    align: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL // Layout.Alignment.ALIGN_OPPOSITE
): StaticLayout {
    val layout: StaticLayout
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        layout = StaticLayout.Builder.obtain(
            source,
            0,
            source.length,
            paint,
            width
        ).setAlignment(align).build()
    } else {
        layout = StaticLayout(
            source,
            0,
            source.length,
            paint,
            width,
            align,
            1f,
            0f,
            false
        )
    }
    return layout
}

//</editor-fold desc="canvas">

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

/**获取旋转的角度, 非弧度
 * https://stackoverflow.com/questions/12256854/get-the-rotate-value-from-matrix-in-android
 * [0~180°]
 * [-180°~0]
 *
 * */
fun Matrix.getRotateDegrees(): Float {
    getValues(_tempValues)
/*//    // translation is simple
 * [0~-180°]
 * [180°~0]
//    val tx = _tempValues[Matrix.MTRANS_X]
//    val ty = _tempValues[Matrix.MTRANS_Y]
//
//    // calculate real scale
//    val scalex: Float = _tempValues[Matrix.MSCALE_X]
//    val skewy: Float = _tempValues[Matrix.MSKEW_Y]
//    val rScale = sqrt((scalex * scalex + skewy * skewy).toDouble()).toFloat()

    // calculate the degree of rotation
    val rAngle = Math.round(
        atan2(
            _tempValues[Matrix.MSKEW_X].toDouble(),
            _tempValues[Matrix.MSCALE_X].toDouble()
        ) * (180 / Math.PI)
    ).toFloat()
    return rAngle*/

    val degrees = atan2(
        _tempValues[Matrix.MSKEW_X],
        _tempValues[Matrix.MSCALE_X]
    ) * (180 / Math.PI)

    return -degrees.roundToLong().toFloat()
}

/**[PointF]*/
fun Matrix.mapPoint(x: Float, y: Float): PointF {
    _tempPoints[0] = x
    _tempPoints[1] = y
    mapPoints(_tempPoints, _tempPoints)
    _tempPoint.x = _tempPoints[0]
    _tempPoint.y = _tempPoints[1]
    return PointF().apply { set(_tempPoint) }
}

/**[PointF]*/
fun Matrix.mapPoint(point: PointF): PointF {
    return mapPoint(point.x, point.y)
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

//<editor-fold desc="Other">

/**将[value]限制在[min] [max]之间*/
fun clamp(value: Float, min: Float, max: Float): Float = min(max(value, min), max)

fun Layout.getMaxLineWidth(): Float {
    var width = 0f
    for (line in 0 until lineCount) {
        width = max(width, getLineWidth(line))
    }
    return width
}

/**角度转弧度*/
fun Float.toRadians(): Float = Math.toRadians(this.toDouble()).toFloat()

/**弧度转角度*/
fun Float.toDegrees(): Float = Math.toDegrees(this.toDouble()).toFloat()

//</editor-fold desc="Other">