package com.angcyo.library.ex

import android.animation.ArgbEvaluator
import android.graphics.Color
import java.util.*
import kotlin.random.Random.Default.nextInt

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/20
 */

fun randomColor(random: Random): Int {
    return randomColor(random, 120, 250)
}

/**
 * 随机颜色, 设置一个最小值, 设置一个最大值, 第三个值在这2者之间随机改变
 */
fun randomColor(random: Random, minValue: Int, maxValue: Int): Int {
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

fun randomColor(minValue: Int = 120, maxValue: Int = 250): Int {
    val r = nextInt(minValue, maxValue)
    val g = nextInt(minValue, maxValue)
    val b = nextInt(minValue, maxValue)
    return Color.rgb(r, g, b)
}

fun randomColorAlpha(minValue: Int = 120, maxValue: Int = 250): Int {
    val a = nextInt(minValue, maxValue)
    val r = nextInt(minValue, maxValue)
    val g = nextInt(minValue, maxValue)
    val b = nextInt(minValue, maxValue)
    return Color.argb(a, r, g, b)
}

private var argbEvaluator: ArgbEvaluator = ArgbEvaluator()
/**根据比例, 获取评估后的颜色值[fraction][0-1]*/
fun evaluateColor(fraction: Float, startValue: Int, endValue: Int): Int {
    return argbEvaluator.evaluate(fraction, startValue, endValue) as Int
}