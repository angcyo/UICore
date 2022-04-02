package com.angcyo.canvas.utils

import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import kotlin.math.max
import kotlin.math.min

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/01
 */

/**临时对象, 用来存储[Matrix]矩阵值*/
val _tempValues = floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)

/**临时对象, 用来存储坐标点位值*/
val _tempPoint = floatArrayOf(0f, 0f)

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

/**将[value]限制在[min] [max]之间*/
fun clamp(value: Float, min: Float, max: Float): Float = min(max(value, min), max)
