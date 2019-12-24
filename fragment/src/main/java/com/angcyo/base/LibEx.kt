package com.angcyo.base

import android.content.Context
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.Px
import androidx.core.content.ContextCompat
import com.angcyo.fragment.AbsFragment
import com.angcyo.library.app

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

@ColorInt
fun Context.getColor(@ColorRes id: Int): Int {
    return ContextCompat.getColor(this, id)
}

@Px
fun Context.getDimen(@DimenRes id: Int): Int {
    return resources.getDimensionPixelOffset(id)
}