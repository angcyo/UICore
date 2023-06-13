package com.angcyo.library.ex

import android.animation.ArgbEvaluator
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.RectF
import android.os.SystemClock
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import androidx.core.math.MathUtils
import java.util.Locale
import java.util.Random
import kotlin.random.Random.Default.nextInt

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/20
 */

/**随机颜色*/
val randomColorList = listOf(
    "#4CC38F",
    "#57A7F1",
    "#8076d6",
    "#62bec7",
    "#f0a72e",
    "#f6899e",
    "#b4846d",
    "#67719d",
    "#b8838c",
    "#63bca4"
)

/**随机产生一个颜色*/
fun randomColor(minValue: Int = 120, maxValue: Int = 250): Int {
    val r = nextInt(minValue, maxValue)
    val g = nextInt(minValue, maxValue)
    val b = nextInt(minValue, maxValue)
    return Color.rgb(r, g, b)
}

/**随机产生一个颜色(带透明)*/
fun randomColorAlpha(minValue: Int = 120, maxValue: Int = 250): Int {
    val a = nextInt(minValue, maxValue)
    val r = nextInt(minValue, maxValue)
    val g = nextInt(minValue, maxValue)
    val b = nextInt(minValue, maxValue)
    return Color.argb(a, r, g, b)
}

fun randomColorIn(random: Random = Random(SystemClock.elapsedRealtime())): Int {
    return randomColorIn(random, 120, 250)
}

/**
 * 随机颜色, 设置一个最小值, 设置一个最大值, 第三个值在这2者之间随机改变
 */
fun randomColorIn(random: Random, minValue: Int, maxValue: Int): Int {
    val a = minValue + random.nextInt(maxValue - minValue)
    val list1: MutableList<Int> = ArrayList()
    val list2: MutableList<Int> = ArrayList()
    list1.add(a)
    list1.add(minValue)
    list1.add(maxValue)
    while (list2.size != 3) {
        val i = random.nextInt(list1.size)
        list2.add(list1.removeAt(i))
    }
    return Color.rgb(list2[0], list2[1], list2[2])
}

private var argbEvaluator: ArgbEvaluator = ArgbEvaluator()

/**根据比例, 获取评估后的颜色值.
 * [fraction] [0~1]比例进度
 * [startValue] 颜色的开始值
 * [endValue] 颜色的结束值
 * */
fun evaluateColor(fraction: Float, startValue: Int, endValue: Int): Int {
    return argbEvaluator.evaluate(fraction, startValue, endValue) as Int
}

/**[com.angcyo.library.component.RectEvaluator]*/
fun evaluateRect(fraction: Float, startValue: Rect, endValue: Rect, result: Rect): Rect {
    val left = startValue.left + ((endValue.left - startValue.left) * fraction).toInt()
    val top = startValue.top + ((endValue.top - startValue.top) * fraction).toInt()
    val right = startValue.right + ((endValue.right - startValue.right) * fraction).toInt()
    val bottom = startValue.bottom + ((endValue.bottom - startValue.bottom) * fraction).toInt()
    result.set(left, top, right, bottom)
    return result
}

fun evaluateRectF(fraction: Float, startValue: RectF, endValue: RectF, result: RectF): RectF {
    val left = startValue.left + (endValue.left - startValue.left) * fraction
    val top = startValue.top + (endValue.top - startValue.top) * fraction
    val right = startValue.right + (endValue.right - startValue.right) * fraction
    val bottom = startValue.bottom + (endValue.bottom - startValue.bottom) * fraction
    result.set(left, top, right, bottom)
    return result
}

/**[this~toValue] [progress]进度[0~1]比例进度
 * [evaluateColor]*/
fun Int.progressColor(progress: Float, toValue: Int) = evaluateColor(progress, this, toValue)

/**从一个渐变颜色中获取颜色*/
fun evaluateColor(fraction: Float, colors: IntArray, positions: FloatArray? = null): Int {
    if (fraction <= 0) {
        return colors.first()
    } else if (fraction >= 1f) {
        return colors.last()
    }

    val array = if (positions == null) {
        val list = mutableListOf<Float>()
        val avg = 1f / (colors.size - 1)
        colors.forEachIndexed { index, i ->
            list.add(index * avg)
        }
        list.toFloatArray()
    } else {
        positions
    }

    var startColor: Int = Color.TRANSPARENT
    var endColor: Int = Color.TRANSPARENT
    var f = 0f

    for ((index, fl) in array.withIndex()) {
        val nextFl = array.getOrNull(index + 1) ?: return colors[index]

        if (nextFl > fraction && fraction > fl) {
            startColor = colors[index]
            endColor = colors[index + 1]
            f = (fraction - fl) / (nextFl - fl)
            break
        }
    }

    return ArgbEvaluator().evaluate(f, startColor, endColor) as Int
}

/**[alpha]*/
fun Int.alpha(alpha: Float): Int {
    return alpha(alpha.toInt())
}

/**
 * 设置一个颜色的透明值, 并返回这个颜色值.
 *
 * @param alpha [0..255] 值越小,越透明.
 */
fun Int.alpha(alpha: Int): Int {
    return ColorUtils.setAlphaComponent(this, MathUtils.clamp(alpha, 0, 255))
}

/**[alpha]不透明度比例, 值越小越透明, 值越大越不透明.
 * [alpha] [0~1f]*/
fun Int.alphaRatio(alpha: Float): Int {
    return alpha(alpha * 255)
}

/**0xFFFF8000*/
fun Int.toHexColorString(prefix: String = "#"): String {
    val a = Color.alpha(this)
    val r = Color.red(this)
    val g = Color.green(this)
    val b = Color.blue(this)
    return String.format(Locale.US, "${prefix}%02X%02X%02X%02X", a, r, g, b)
}

@ColorInt
fun Int.toHsv(h: Float = 1f, s: Float = 1f, v: Float = 1f): Int {
    var result = this
    val hsv = FloatArray(3)
    Color.colorToHSV(result, hsv)
    hsv[0] *= h
    hsv[1] *= s
    hsv[2] *= v
    result = Color.HSVToColor(hsv)
    return result
}

/**改变颜色的色调*/
@ColorInt
fun Int.toHue(factor: Float = 0.8f): Int {
    var result = this
    val hsv = FloatArray(3)
    Color.colorToHSV(result, hsv)
    hsv[0] *= factor // value component
    result = Color.HSVToColor(hsv)
    return result
}

/**改变颜色的饱和度 */
@ColorInt
fun Int.toSaturation(factor: Float = 0.8f): Int {
    var result = this
    val hsv = FloatArray(3)
    Color.colorToHSV(result, hsv)
    hsv[1] *= factor // value component
    result = Color.HSVToColor(hsv)
    return result
}

/**改变颜色的亮度
 * [factor] 值越小, 越暗*/
@ColorInt
fun Int.toBrightness(factor: Float = 0.8f): Int {
    var result = this
    val hsv = FloatArray(3)
    Color.colorToHSV(result, hsv)
    hsv[2] *= factor // value component
    result = Color.HSVToColor(hsv)
    return result
}

/**将颜色绘制出来*/
fun Int.toBitmap(size: Int = 10 * dpi): Bitmap {
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(this)
    return bitmap
}