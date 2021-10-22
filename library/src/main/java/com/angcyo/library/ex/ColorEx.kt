package com.angcyo.library.ex

import android.animation.ArgbEvaluator
import android.graphics.Color
import android.os.SystemClock
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import androidx.core.math.MathUtils
import java.util.*
import kotlin.random.Random.Default.nextInt

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/20
 */

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

/**根据比例, 获取评估后的颜色值[fraction][0-1]*/
fun evaluateColor(fraction: Float, startValue: Int, endValue: Int): Int {
    return argbEvaluator.evaluate(fraction, startValue, endValue) as Int
}

/**
 * 设置一个颜色的透明值, 并返回这个颜色值.
 *
 * @param alpha [0..255] 值越小,越透明.
 */
fun Int.alpha(alpha: Int): Int {
    return ColorUtils.setAlphaComponent(this, MathUtils.clamp(alpha, 0, 255))
}

/**[alpha]不透明度比例, 值越大越不透明, 值越小越透明*/
fun Int.alphaRatio(alpha: Float): Int {
    return alpha(alpha * 255)
}

fun Int.alpha(alpha: Float): Int {
    return alpha(alpha.toInt())
}

/**0xFFFF8000*/
fun Int.toHexColorString(): String {
    val a = Color.alpha(this)
    val r = Color.red(this)
    val g = Color.green(this)
    val b = Color.blue(this)
    return String.format(Locale.getDefault(), "0x%02X%02X%02X%02X", a, r, g, b)
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