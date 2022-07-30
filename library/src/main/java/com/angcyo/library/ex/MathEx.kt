package com.angcyo.library.ex

import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.RectF
import kotlin.math.*

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/27
 */

/**计算两点之间的距离*/
fun distance(point1: PointF, point2: PointF): Double {
    return c(point1.x, point1.y, point2.x, point2.y)
}

/**勾股定理 C边的长度*/
fun c(x1: Float, y1: Float, x2: Float, y2: Float): Double {
    val a = (x2 - x1).absoluteValue
    val b = (y2 - y1).absoluteValue
    return c(a, b)
}

/**勾股定律*/
fun c(a: Float, b: Float): Double {
    return hypot(a.toDouble(), b.toDouble())
}

fun c(a: Double, b: Double): Double {
    return hypot(a, b)
}

/**根据半径[radius],原点[pivotX,pivotY]坐标, 计算出角度[degrees]对应的圆上坐标点
 * [dotRadians]*/
fun dotDegrees(
    radius: Float,
    degrees: Float,
    pivotX: Float,
    pivotY: Float,
    result: PointF = _tempPoint
): PointF {
    val x = pivotX + radius * cos(degrees * Math.PI / 180)
    val y = pivotY + radius * sin(degrees * Math.PI / 180)
    result.set(x.toFloat(), y.toFloat())
    return result
}

/**弧度[radians]
 * [dotDegrees]*/
fun dotRadians(
    radius: Float,
    radians: Float,
    pivotX: Float,
    pivotY: Float,
    result: PointF = _tempPoint
): PointF {
    val x = pivotX + radius * cos(radians)
    val y = pivotY + radius * sin(radians)
    result.set(x, y)
    return result
}

/**计算2点的角度
 * 角的度数。[0-360]*/
fun degrees(x: Float, y: Float, pivotX: Float, pivotY: Float): Double {
    return Math.toDegrees(radians(x, y, pivotX, pivotY))
}

/**计算2点的弧度
 * 以弧度表示的角度的测量。[0-2PI]
 *
 * 计算点 (x,y) 相对于原点 (pivotX,pivotY) 的弧度
 * */
fun radians(x: Float, y: Float, pivotX: Float, pivotY: Float): Double {
    val deltaX = x - pivotX
    val deltaY = y - pivotY
    val radians = atan2(deltaY.toDouble(), deltaX.toDouble())
    return if (radians < 0) {
        2 * Math.PI + radians
    } else {
        radians
    }
}

/**
 * 保留浮点数, 小数点后几位 .x0 -> .x
 * @param digit 需要保留的小数位数
 * @param fadedUp 是否四舍五入
 * */
fun Double.decimal(digit: Int = 2, fadedUp: Boolean = false): String {
    val f = 10f.pow(digit)
    val value = if (isNaN()) {
        0
    } else if (fadedUp) {
        (this * f).roundToInt()
    } else {
        (this * f).toInt()
    } / f
    return String.format("%.${digit}f", value.toFloat())
}

/**保留小数点后几位*/
fun Float.decimal(digit: Int = 2, fadedUp: Boolean = false): String {
    return this.toDouble().decimal(digit, fadedUp)
}

/**向上取整
 * 1.01 -> 2*/
fun Double.ceil() = ceil(this)
fun Float.ceil() = ceil(this.toDouble()).toFloat()

/**向上取整, 如果小于零时, 向下取整*/
fun Float.ceilReverse() = if (this > 0f) {
    ceil(this.toDouble()).toFloat()
} else {
    floor()
}

/**向下取整
 * 1.01 -> 1*/
fun Double.floor() = floor(this)
fun Float.floor() = floor(this.toDouble()).toFloat()

/**向下取整, 如果大于零时, 向上取整*/
fun Float.floorReverse() = if (this > 0f) {
    ceil()
} else {
    floor(this.toDouble()).toFloat()
}

/**角度转弧度*/
fun Float.toRadians(): Float = Math.toRadians(this.toDouble()).toFloat()

/**弧度转角度*/
fun Float.toDegrees(): Float = Math.toDegrees(this.toDouble()).toFloat()

//<editor-fold desc="matrix">

/**临时对象, 用来存储[Matrix]矩阵值*/
val _tempValues = floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)

/**临时对象, 用来存储坐标点位值*/
val _tempPoints = floatArrayOf(0f, 0f)

val _tempPoint = PointF()

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
    return _tempPoint
}

/**[PointF]
 * [point] 入参
 * @return 返回值*/
fun Matrix.mapPoint(point: PointF): PointF {
    return mapPoint(point.x, point.y)
}

/**
 * [point] 入参
 * [result] 返回值*/
fun Matrix.mapPoint(point: PointF, result: PointF): PointF {
    _tempPoints[0] = point.x
    _tempPoints[1] = point.y
    mapPoints(_tempPoints, _tempPoints)
    result.x = _tempPoints[0]
    result.y = _tempPoints[1]
    return result
}

/**[RectF]*/
fun Matrix.mapRectF(rect: RectF, result: RectF = _tempRectF): RectF {
    mapRect(result, rect)
    return result
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


//</editor-fold desc="matrix">
