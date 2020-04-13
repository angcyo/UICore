package com.angcyo.library.ex

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

/**第4位的最高字节  0x8000 = 32,768, 未定义的资源*/
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