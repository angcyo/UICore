package com.angcyo.widget

import androidx.annotation.IdRes
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.angcyo.tablayout.DslTabLayout
import com.angcyo.widget.base.Anim
import com.angcyo.widget.base.checkEmpty
import com.angcyo.widget.base.simulateClick
import com.angcyo.widget.edit.AutoCompleteEditText
import com.angcyo.widget.edit.DslEditText
import com.angcyo.widget.image.DslImageView
import com.angcyo.widget.layout.DslSoftInputLayout
import com.angcyo.widget.pager.DslViewPager
import com.angcyo.widget.recycler.DslRecyclerView
import com.angcyo.widget.text.DslTextView

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/02
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

/**模拟点击事件,和直接[performClick]不同的是有效背景效果*/
fun DslViewHolder.simulateClick(@IdRes id: Int, delay: Long = Anim.ANIM_DURATION) {
    view(id)?.simulateClick(delay)
}

fun DslViewHolder.vp(@IdRes id: Int): ViewPager? {
    return v(id)
}

fun DslViewHolder.vp2(@IdRes id: Int): ViewPager2? {
    return v(id)
}

fun DslViewHolder.tab(@IdRes id: Int): DslTabLayout? {
    return v(id)
}

fun DslViewHolder.button(@IdRes id: Int): DslButton? {
    return v(id)
}

fun DslViewHolder.drv(@IdRes id: Int): DslRecyclerView? {
    return v(id)
}

fun DslViewHolder._rv(@IdRes id: Int): DslRecyclerView? {
    return v(id)
}

fun DslViewHolder._tv(@IdRes id: Int): DslTextView? {
    return v(id)
}

fun DslViewHolder._et(@IdRes id: Int): DslEditText? {
    return v(id)
}

fun DslViewHolder._ev(@IdRes id: Int): DslEditText? {
    return v(id)
}

fun DslViewHolder.checkEmpty(@IdRes vararg ids: Int): Boolean {
    var empty = false
    for (id in ids) {
        if (ev(id)?.checkEmpty() == true) {
            empty = true
            break
        }
    }
    return empty
}

fun DslViewHolder._img(@IdRes id: Int): DslImageView? {
    return v(id)
}

fun DslViewHolder.spinner(@IdRes id: Int): RSpinner? {
    return v(id)
}

fun DslViewHolder.soft(@IdRes id: Int): DslSoftInputLayout? {
    return v(id)
}

fun DslViewHolder.auto(
    @IdRes resId: Int,
    dataList: List<CharSequence>?,
    showOnFocus: Boolean = true,
    focusDelay: Long = 0L
): AutoCompleteEditText? {
    val auto: AutoCompleteEditText? = v(resId)
    auto?.setDataList(dataList ?: emptyList(), showOnFocus, focusDelay)
    return auto
}

fun DslViewHolder._vp(@IdRes id: Int): DslViewPager? {
    return v(id)
}

fun DslViewHolder.hawkTag(@IdRes id: Int): Any? {
    return tag(id, R.id.lib_tag_hawk, "$id")
}
