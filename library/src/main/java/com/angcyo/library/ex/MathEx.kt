package com.angcyo.library.ex

import android.graphics.PointF
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

/**根据半径[radius],原点[pivotX,pivotY]坐标, 计算出角度[degrees]对应的圆上坐标点*/
fun dotDegrees(radius: Float, degrees: Int, pivotX: Int, pivotY: Int): PointF {
    val x = pivotX + radius * cos(degrees * Math.PI / 180)
    val y = pivotY + radius * sin(degrees * Math.PI / 180)
    return PointF(x.toFloat(), y.toFloat())
}

/**弧度[radians]*/
fun dotRadians(radius: Float, radians: Float, pivotX: Int, pivotY: Int): PointF {
    val x = pivotX + radius * cos(radians)
    val y = pivotY + radius * sin(radians)
    return PointF(x, y)
}

/**角的度数。[0-360]*/
fun degrees(x: Float, y: Float, pivotX: Float, pivotY: Float): Int {
    return Math.toDegrees(radians(x, y, pivotX, pivotY)).toInt()
}

/**
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
fun Double.decimal(digit: Int = 2, fadedUp: Boolean = false): Float {
    val f = 10f.pow(digit)
    return if (fadedUp) {
        (this * f).roundToInt()
    } else {
        (this * f).toInt()
    } / f
}

/**保留小数点后几位*/
fun Float.decimal(digit: Int = 2, fadedUp: Boolean = false): Float {
    return this.toDouble().decimal(digit, fadedUp)
}

/**向上取整
 * 1.01 -> 2*/
fun Double.ceil() = ceil(this)
fun Float.ceil() = ceil(this.toDouble()).toFloat()
fun Float.ceilReverse() = if (this > 0f) {
    ceil(this.toDouble()).toFloat()
} else {
    floor()
}

/**向下取整
 * 1.01 -> 1*/
fun Double.floor() = floor(this)
fun Float.floor() = floor(this.toDouble()).toFloat()
fun Float.floorReverse() = if (this > 0f) {
    ceil()
} else {
    floor(this.toDouble()).toFloat()
}
