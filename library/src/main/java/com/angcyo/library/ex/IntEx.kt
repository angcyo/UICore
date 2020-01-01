package com.angcyo.library.ex

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

fun Int?.orDefault(default: Int) = if (this ?: 0 > 0) this else default

fun Long?.orDefault(default: Long) = if (this ?: 0 > 0) this else default