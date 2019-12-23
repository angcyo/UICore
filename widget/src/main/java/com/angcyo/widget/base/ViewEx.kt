package com.angcyo.widget.base

import android.graphics.drawable.Drawable
import android.view.View
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/20
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

fun View.getColor(@ColorRes id: Int): Int {
    return ContextCompat.getColor(context, id)
}

fun View.getDrawable(id: Int): Drawable? {
    if (id <= 0) {
        return null
    }
    return ContextCompat.getDrawable(context, id)
}

fun View.getStatusBarHeight(): Int {
    return context.getStatusBarHeight()
}