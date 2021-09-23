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

/**match_parent*/
fun Int.isExactly(): Boolean {
    return getMode() == MeasureSpec.EXACTLY
}

/**wrap_content*/
fun Int.isAtMost(): Boolean {
    return getMode() == MeasureSpec.AT_MOST
}

fun Int.isUnspecified(): Boolean {
    return getMode() == MeasureSpec.UNSPECIFIED
}

/**未指定大小*/
fun Int.isNotSpecified(): Boolean {
    return isAtMost() || isUnspecified()
}

fun atMost(size: Int) = MeasureSpec.makeMeasureSpec(size, MeasureSpec.AT_MOST)
fun exactly(size: Int) = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY)
