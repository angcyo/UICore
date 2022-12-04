package com.angcyo.library.ex

import android.content.res.Resources

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/20
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

val density: Float get() = Resources.getSystem()?.displayMetrics?.density ?: 0f
val dp: Float get() = Resources.getSystem()?.displayMetrics?.density ?: 0f
val dpi: Int get() = Resources.getSystem()?.displayMetrics?.density?.toInt() ?: 0

/**dp转px值*/
fun Int.toDp(): Float {
    return this * dp
}

/**dp转px值*/
fun Int.toDpi(): Int {
    return this * dpi
}

/**dp转px值*/
fun Float.toDp(): Float {
    return this * dp
}

/**dp转px值*/
fun Float.toDpi(): Int {
    return (this * dpi).toInt()
}

//---

/**dp转px值*/
fun Int.toDpFromPixel(): Float {
    return this / dp
}

/**dp转px值*/
fun Float.toDpFromPixel(): Float {
    return this / dp
}