package com.angcyo.library.ex

import android.os.SystemClock
import java.math.BigDecimal
import java.text.DecimalFormat
import kotlin.math.max
import kotlin.math.min

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/20
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

/**整型数中, 是否包含另一个整数*/
fun Int.have(value: Int): Boolean = if (this == 0 || value == 0) {
    false
} else if (this == 0 && value == 0) {
    true
} else {
    ((this > 0 && value > 0) || (this < 0 && value < 0)) && this and value == value
}

fun Int.remove(value: Int): Int = this and value.inv()
fun Int.add(value: Int): Int = this or value

/**第4位的最高字节  0x8000 = 32,768, 未定义的资源. 0默认资源*/
val undefined_res = -32_768

/**未定义的整数*/
val undefined_int = -1

/**未定义的大小*/
val undefined_size = Int.MIN_VALUE

/**未定义的浮点数*/
val undefined_float = -1f

/**未定义的颜色*/
val undefined_color = -32_768

fun Int?.or(default: Int) = if (this ?: 0 > 0) this else default

fun Long?.or(default: Long) = if (this ?: 0 > 0) this else default

/**目标值的最小值是自己,  目标值必须超过自己*/
fun Float.withMinValue(value: Float /*允许的最小值*/) = max(value, this)

fun Float.withMinValue(value: Int) = max(value.toFloat(), this)

fun Int.withMinValue(value: Int) = max(value, this)

/**目标值的最大值是自己,  目标值不能超过自己*/
fun Float.withMaxValue(value: Float /*允许的最大值*/) = min(value, this)

fun Int.withMaxValue(value: Int) = min(value, this)

/**左右对齐指定个数的0*/
fun Int.toZero(leftCount: Int = 2, rightCount: Int = 0): String {
    val b = StringBuffer()
    for (i in 0 until leftCount) {
        b.append("0")
    }
    if (rightCount > 0) {
        b.append(".")
        for (i in 0 until rightCount) {
            b.append("0")
        }
    }
    val df = DecimalFormat("$b")
    return df.format(this)
}

/**
 * 很大的数, 折叠展示成 xx.xxw xx.xxk xx
 * 转换成短数字
 * */
fun Int.toShortString() = this.toLong().toShortString()

fun Long.toShortString(
    bases: Array<Long> = arrayOf(10000, 1000), //设定的基数
    units: Array<String> = arrayOf("w", "kw"), //基数对应的单位
    digits: Array<Int> = arrayOf(2, 2) //每个单位, 需要保留的小数点位数
): String {
    val builder = StringBuilder()

    //基数, 对应的单位
    val levelNumList = mutableListOf<Long>()
    var num: Long = this
    var level = 0

    while (true) {
        if (num != 0L) {

            //基数
            val base: Long = bases.getOrNull(level) ?: Long.MAX_VALUE //最后一级结束控制

            //基数计算后的余数
            val levelNum = num % base

            levelNumList.add(levelNum)

            level++
            //下一个参与计算的数
            num /= base
        } else if (num == 0L && levelNumList.isEmpty()) {
            levelNumList.add(num)
            break
        } else {
            break
        }
    }

    levelNumList.lastOrNull()?.apply {
        builder.append(this)

        val lastIndex = levelNumList.lastIndex - 1
        val dec = digits.getOrNull(lastIndex) ?: -1
        if (dec > 0) {
            //需要小数

            levelNumList.getOrNull(lastIndex)?.apply {
                //有小数
                val numString = this.toString()

                builder.append(('.'))
                builder.append(numString.subSequence(0, min(dec, numString.length)))
            }
        }

        //单位
        units.getOrNull(lastIndex)?.apply {
            builder.append(this)
        }
    }

    return builder.toString()
}

/**构建一个16位的整数*/
fun generateInt(): Int = SystemClock.uptimeMillis().toInt() and 0xFFFF

/**转换成 100% 80% 的形式*/
fun Number.toRatioStr(sum: Number): String = "${(this.toFloat() / sum.toFloat() * 100).toInt()}%"

/**科学计数转成普通数字
 * 8.64e+07
 * 5344.34234e3
 * */
fun String?.toBigLongOrNull(): Long? {
    return this?.toLongOrNull() ?: try {
        BigDecimal(this).toLong()
    } catch (e: Exception) {
        null
    }
}