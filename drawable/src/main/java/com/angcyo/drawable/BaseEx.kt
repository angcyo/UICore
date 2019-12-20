package com.angcyo.drawable

import android.content.res.Resources
import android.graphics.Paint

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/20
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

val density: Float = Resources.getSystem()?.displayMetrics?.density ?: 0f
val dp: Float = Resources.getSystem()?.displayMetrics?.density ?: 0f
val dpi: Int = Resources.getSystem()?.displayMetrics?.density?.toInt() ?: 0

fun Int.toDp(): Float {
    return this * dp
}

fun Int.toDpi(): Int {
    return this * dpi
}

fun Float.toDp(): Float {
    return this * dp
}

fun Float.toDpi(): Int {
    return (this * dpi).toInt()
}

/**文本的宽度*/
fun Paint.textWidth(text: String?): Float {
    return measureText(text ?: "")
}

/**文本的高度*/
fun Paint.textHeight(): Float {
    return descent() - ascent()
}