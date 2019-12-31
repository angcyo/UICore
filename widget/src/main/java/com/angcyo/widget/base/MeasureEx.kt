package com.angcyo.widget.base

import android.view.View.MeasureSpec

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/31
 */

fun Int.getSize(): Int {
    return MeasureSpec.getSize(this)
}

fun Int.getMode(): Int {
    return MeasureSpec.getMode(this)
}

fun Int.isExactly(): Boolean {
    return getMode() == MeasureSpec.EXACTLY
}

fun Int.isAtMost(): Boolean {
    return getMode() == MeasureSpec.AT_MOST
}

fun Int.isUnspecified(): Boolean {
    return getMode() == MeasureSpec.UNSPECIFIED
}

