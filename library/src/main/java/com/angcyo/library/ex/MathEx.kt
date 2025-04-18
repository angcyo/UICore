package com.angcyo.library.ex

import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.RectF
import android.os.Build
import com.angcyo.library.component.pool.acquireTempMatrix
import com.angcyo.library.component.pool.acquireTempPointF
import com.angcyo.library.component.pool.acquireTempRectF
import com.angcyo.library.component.pool.release
import com.angcyo.library.model.PointD
import java.util.Locale
import kotlin.math.absoluteValue
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.math.sin
import kotlin.math.sqrt

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/27
 */

/**计算两点之间的距离*/
fun distance(point1: PointF, point2: PointF, abs: Boolean = true): Double {
    return c(
        point1.x.toDouble(), point1.y.toDouble(), point2.x.toDouble(), point2.y.toDouble(), abs
    )
}

/**[distance]*/
fun PointF.distance(other: PointF): Float = distance(this, other).toFloat()

/**勾股定理 C边的长度*/
fun c(x1: Double, y1: Double, x2: Double, y2: Double, abs: Boolean = true): Double {
    val a = x2 - x1
    val b = y2 - y1
    var c = c(a, b)
    if (a < 0 && b < 0) {
        //反向
        c = -c.absoluteValue
    }
    if (abs) {
        c = c.absoluteValue
    }
    return c
}

fun c(x1: Float, y1: Float, x2: Float, y2: Float, abs: Boolean = true): Float {
    val a = x2 - x1
    val b = y2 - y1
    var c = c(a, b).toFloat()
    if (a < 0 && b < 0) {
        //反向
        c = -c.absoluteValue
    }
    if (abs) {
        c = c.absoluteValue
    }
    return c
}

/**勾股定律*/
fun c(a: Float, b: Float): Double {
    return hypot(a.toDouble(), b.toDouble())
}

fun c(a: Double, b: Double): Double {
    return hypot(a, b)
}

/**获取2个点之间的距离, 勾股定律*/
fun spacing(x1: Double, y1: Double, x2: Double, y2: Double): Double {
    val x = x2 - x1
    val y = y2 - y1
    return sqrt(x * x + y * y)
}

fun spacing(x1: Float, y1: Float, x2: Float, y2: Float): Float {
    val x = x2 - x1
    val y = y2 - y1
    return sqrt(x * x + y * y)
}

/**计算2个点之间的角度, 度数单位
 * 以第一个点为圆心, 计算第二个点的角度
 * 3点钟方向为0度
 *
 * [0~180]
 * [0~-180]
 * */
fun angle(x1: Float, y1: Float, x2: Float, y2: Float) =
    Math.toDegrees(atan2((y2 - y1).toDouble(), (x2 - x1).toDouble())).toFloat()

fun angle(x1: Double, y1: Double, x2: Double, y2: Double) =
    Math.toDegrees(atan2((y2 - y1), (x2 - x1)))

/**计算2个点之间的角度, 度数单位*/
fun PointF.angle(p2: PointF) = angle(x, y, p2.x, p2.y)

/**获取2个点的中点坐标*/
fun midPoint(x1: Float, y1: Float, x2: Float, y2: Float, result: PointF) {
    result.x = (x1 + x2) / 2f
    result.y = (y1 + y2) / 2f
}

fun midPoint(p1: PointF, p2: PointF, result: PointF) {
    midPoint(p1.x, p1.y, p2.x, p2.y, result)
}

/**根据半径[radius],原点[pivotX,pivotY]坐标, 计算出角度[degrees]对应的圆上坐标点
 * 圆上任意一点的坐标
 * [degrees] 角度
 * [pivotX] [pivotY] 圆心坐标
 * [dotRadians]*/
fun dotDegrees(
    radius: Double, degrees: Double, pivotX: Double, pivotY: Double, result: PointD = PointD()
): PointD {
    val x = pivotX + radius * cos(degrees * Math.PI / 180)
    val y = pivotY + radius * sin(degrees * Math.PI / 180)
    result.set(x, y)
    return result
}

fun dotDegrees(
    radius: Float, degrees: Float, pivotX: Float, pivotY: Float, result: PointF = PointF()
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
    result: PointF = acquireTempPointF()
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

/**保留小数点后几位
 * [ensureInt] 如果是整数, 优先使用整数*/
fun Double.decimal(
    digit: Int,
    ensureInt: Boolean,
    fadedUp: Boolean,
): String {
    if (!isFinite()) {
        return "NaN"
    }
    if (ensureInt) {
        val int = if (fadedUp) roundToInt() else toInt()
        val intD = int.toDouble()
        if (this == intD || formatShow(digit).toDoubleOrNull() == intD) {
            return "$int"
        }
    }
    return decimal(digit, fadedUp)
}

/**
 * 保留浮点数, 小数点后几位 .x0 -> .x
 * @param digit 需要保留的小数位数
 * @param fadedUp 是否四舍五入
 * */
fun Double.decimal(digit: Int = 2, fadedUp: Boolean = false): String {
    val f = 10f.pow(digit)
    val value = if (isNaN()) {
        0.0
    } else if (fadedUp) {
        (this * f).roundToLong() //四舍五入
    } else {
        (this * f).toLong() //取整
    } / f
    return String.format(Locale.US, "%.${digit}f", value)
}

/**保留小数点后几位
 * [fadedUp] 是否四舍五入*/
fun Float.decimal(
    digit: Int = 2,
    fadedUp: Boolean = false,
): String {
    val f = 10f.pow(digit)
    val value = if (isNaN()) {
        0f
    } else if (fadedUp) {
        (this * f).roundToLong()
    } else {
        (this * f).toLong()
    } / f
    return String.format(Locale.US, "%.${digit}f", value)
}

/**保留小数点后几位*/
fun Float.decimal(
    digit: Int,
    ensureInt: Boolean,
    fadedUp: Boolean,
    removeZero: Boolean = true,
): String {
    if (!isFinite()) {
        return "NaN"
    }
    if (ensureInt) {
        val int = if (fadedUp) roundToInt() else toInt()
        val intF = int.toFloat()
        if (this == intF || formatShow(digit).toFloatOrNull() == intF) {
            return "$int"
        }
    }
    var result = decimal(digit, fadedUp)
    if (removeZero) {
        if (this == 0f) {
            return "0"
        }
        if (result.contains('.')) {
            while (result.endsWith('0')) {
                result = result.substring(0, result.length - 1);
            }
            if (result.endsWith('.')) {
                result = result.substring(0, result.length - 1);
            }
        }
    }
    return result
}

/**向上取整
 * 1.01 -> 2*/
fun Double.ceil() = ceil(this)
fun Double.ceilInt() = ceil(this).toInt()
fun Float.ceil() = ceil(this.toDouble()).toFloat()
fun Float.ceilInt() = ceil(this.toDouble()).toInt()

/**向上取整, 如果小于零时, 向下取整*/
fun Float.ceilReverse() = if (this > 0f) {
    ceil(this.toDouble()).toFloat()
} else {
    floor()
}

/**向下取整
 * 1.01 -> 1*/
fun Double.floor() = floor(this)
fun Double.floorInt() = floor(this).toInt()
fun Float.floor() = floor(this.toDouble()).toFloat()
fun Float.floorInt() = floor(this.toDouble()).toInt()
fun Float.bitmapInt() = roundToInt()

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

/**调整角度的取值范围*/
fun Float.adjustDegrees(): Float = (this + 360) % 360

/**弧度转角度*/
fun Double.toDegrees(): Double = Math.toDegrees(this)

/**获取圆上指定角度的点坐标
 * [cx] [cy] 圆心
 * [radius] 半径
 * [angleInDegrees] 角度, 非弧度*/
fun getPointOnCircle(
    cx: Float, cy: Float, radius: Float, angleInDegrees: Float, result: PointF = PointF()
): PointF {
    // 将角度值转换为弧度值
    val angleInRadians = Math.toRadians(angleInDegrees.toDouble()).toFloat()
    // 计算坐标
    val x = cx + radius * cos(angleInRadians.toDouble()).toFloat()
    val y = cy + radius * sin(angleInRadians.toDouble()).toFloat()
    result.set(x, y)
    return result
}

/**[getRectOnCircle]*/
fun getRectOnCircle(
    height: Float, cx: Float, cy: Float, radius: Float, angleInDegrees: Float
): RectF = getRectOnCircle(1f, height, cx, cy, radius, angleInDegrees)

/**获取圆上指定角度的矩形坐标
 * [cx] [cy] 圆心
 * [radius] 半径
 * [angleInDegrees] 角度, 非弧度*/
fun getRectOnCircle(
    width: Float, height: Float, cx: Float, cy: Float, radius: Float, angleInDegrees: Float
): RectF {
    val result = RectF()
    result.set(cx + radius, cy - height / 2, cx + radius + width, cy + height / 2)
    val matrix = acquireTempMatrix()
    matrix.setRotate(angleInDegrees, cx, cy)
    matrix.mapRectSelf(result)
    matrix.release()
    return result
}

//<editor-fold desc="matrix">

/**临时对象, 用来存储[Matrix]矩阵值*/
val _tempValues = floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)

/**临时对象, 用来存储坐标点位值*/
val _tempPoints = floatArrayOf(0f, 0f)

/**临时点坐标*/
val _tempPos = floatArrayOf(0f, 0f)

/**临时斜率存放*/
val _tempTan = floatArrayOf(0f, 0f, 0f)

/**临时对象*/
val _tempPoint = PointF(0f, 0f)

/**当前矩阵, 偏移的x*/
fun Matrix.getTranslateX(): Float {
    getValues(_tempValues)
    return _tempValues[Matrix.MTRANS_X]
}

fun Matrix.getTranslateY(): Float {
    getValues(_tempValues)
    return _tempValues[Matrix.MTRANS_Y]
}

/**直接更新矩阵中的偏移量*/
fun Matrix.updateTranslate(tx: Float = 0f, ty: Float = 0f) {
    getValues(_tempValues)
    _tempValues[Matrix.MTRANS_X] = tx
    _tempValues[Matrix.MTRANS_Y] = ty
    setValues(_tempValues)
}

fun Matrix.getSkewX(): Float {
    getValues(_tempValues)
    return _tempValues[Matrix.MSKEW_X]
}

fun Matrix.getSkewY(): Float {
    getValues(_tempValues)
    return _tempValues[Matrix.MSKEW_Y]
}

/**直接更新矩阵中的倾斜量*/
fun Matrix.updateSkew(kx: Float = 0f, ky: Float = 0f) {
    getValues(_tempValues)
    _tempValues[Matrix.MSKEW_X] = kx
    _tempValues[Matrix.MSKEW_Y] = ky
    setValues(_tempValues)
}

/**当前矩阵, 缩放的比例. 默认是1f
 * 比如1.2f 2.0f*/
fun Matrix.getScaleX(): Float {
    getValues(_tempValues)
    return _tempValues[Matrix.MSCALE_X].ensure(1f)
}

/**默认是1f*/
fun Matrix.getScaleY(): Float {
    getValues(_tempValues)
    return _tempValues[Matrix.MSCALE_Y].ensure(1f)
}

/**缩放值*/
fun Matrix.getScale(): Float {
    getValues(_tempValues)
    return sqrt(
        _tempValues[Matrix.MSCALE_X].pow(2) + _tempValues[Matrix.MSKEW_Y].pow(2)
    )
}

/**直接更新矩阵中的缩放量*/
fun Matrix.updateScale(sx: Float = 1f, sy: Float = 1f) {
    getValues(_tempValues)
    _tempValues[Matrix.MSCALE_X] = sx
    _tempValues[Matrix.MSCALE_Y] = sy
    setValues(_tempValues)
}

/**获取旋转角度
 * [0~360°]*/
fun Matrix.getRotate(): Float = (360 + getRotateDegrees()) % 360

/**获取旋转的角度, 非弧度
 * https://stackoverflow.com/questions/12256854/get-the-rotate-value-from-matrix-in-android
 * [0~180°]
 * [-180°~0]
 * */
fun Matrix.getRotateDegrees(): Float {
    getValues(_tempValues)/*//    // translation is simple
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
        _tempValues[Matrix.MSKEW_X], _tempValues[Matrix.MSCALE_X]
    ) * (180 / Math.PI)

    return (-degrees).toFloat()
}

/**[getRotateDegrees]*/
fun Matrix.getRotateDegreesY(): Float {
    getValues(_tempValues)
    val degrees = atan2(
        _tempValues[Matrix.MSKEW_Y], _tempValues[Matrix.MSCALE_Y]
    )
    return (-degrees).toDegrees()
}

/**[PointF]*/
fun Matrix.mapPoint(x: Float, y: Float, result: PointF = acquireTempPointF()): PointF {
    _tempPoints[0] = x
    _tempPoints[1] = y
    mapPoints(_tempPoints, _tempPoints)
    result.x = _tempPoints[0]
    result.y = _tempPoints[1]
    return result
}

/**[PointF]
 * [point] 入参
 * @return 返回值*/
fun Matrix.mapPoint(point: PointF): PointF {
    return mapPoint(point.x, point.y, point)
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

fun Matrix.mapPoint(point: PointD, result: PointD): PointD {
    _tempPoints[0] = point.x.toFloat()
    _tempPoints[1] = point.y.toFloat()
    mapPoints(_tempPoints, _tempPoints)
    result.x = _tempPoints[0].toDouble()
    result.y = _tempPoints[1].toDouble()
    return result
}

/**映射矩形
 * [mapRectF]*/
fun Matrix.mapRectSelf(rect: RectF, result: RectF = rect) = mapRectF(rect, result)

/**[RectF]*/
fun Matrix.mapRectF(rect: RectF, result: RectF = acquireTempRectF()): RectF {
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

/**旋转一个点*/
fun PointF.rotate(rotate: Float, pivotX: Float, pivotY: Float, result: PointF = this): PointF {
    val matrix = acquireTempMatrix()
    matrix.reset()
    matrix.setRotate(rotate, pivotX, pivotY)
    matrix.mapPoint(this, result)
    matrix.release()
    return result
}

/**缩放一个点*/
fun PointF.scale(
    scaleX: Float, scaleY: Float, pivotX: Float, pivotY: Float, result: PointF = this
): PointF {
    val matrix = acquireTempMatrix()
    matrix.reset()
    matrix.setScale(scaleX, scaleY, pivotX, pivotY)
    matrix.mapPoint(this, result)
    matrix.release()
    return result
}

/**反向旋转一个点坐标*/
fun PointF.invertRotate(
    rotate: Float, pivotX: Float, pivotY: Float, result: PointF = this
): PointF {
    val matrix = acquireTempMatrix()
    matrix.reset()
    matrix.setRotate(rotate, pivotX, pivotY)
    matrix.invert(matrix)
    matrix.mapPoint(this, result)
    matrix.release()
    return result
}

/**to string*/
fun Matrix.toLogString(): String = buildString {
    appendLine()
    val rotate = getRotate()
    appendLine("rotate:${rotate}° ${rotate.toRadians()}")
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        append("isAffine:${isAffine} ")
    }
    appendLine("isIdentity:${isIdentity}")
    getValues(_tempValues)
    for (i in 0 until 9) {
        if (i % 3 == 0) {
            append("[")
        }
        append(_tempValues[i])
        if (i % 3 != 2) {
            append(", ")
        }
        if (i % 3 == 2) {
            append("]")
            appendLine()
        }
    }
}

/**从当前矩阵中移除[matrix]矩阵
 *
 * 矩阵除法运算 A/B等价于A乘以B的逆矩阵
 * https://yuncode.net/code/c_50ab2790a735119*/
fun Matrix.removeMatrix(matrix: Matrix, result: Matrix = Matrix()): Matrix {
    val tempMatrix = acquireTempMatrix()
    matrix.invert(tempMatrix)

    result.set(this)
    result.preConcat(tempMatrix) //注意preConcat, 不能用postConcat

    tempMatrix.release()

    //返回的矩阵满足以下关系
    //matrix.postConcat(result) == matrix

    return result
}

//</editor-fold desc="matrix">