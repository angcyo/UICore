package com.angcyo.base

import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.Px
import androidx.core.content.ContextCompat
import com.angcyo.fragment.AbsFragment

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/22
 */

@ColorInt
fun AbsFragment.getColor(@ColorRes id: Int): Int {
    return ContextCompat.getColor(context, id)
}

@Px
fun AbsFragment.getDimen(@DimenRes id: Int): Int {
    return context.resources.getDimensionPixelOffset(id)
}