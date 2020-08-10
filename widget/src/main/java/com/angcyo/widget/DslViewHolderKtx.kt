package com.angcyo.widget

import androidx.annotation.IdRes
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.angcyo.tablayout.DslTabLayout
import com.angcyo.widget.base.Anim
import com.angcyo.widget.base.checkEmpty
import com.angcyo.widget.base.error
import com.angcyo.widget.base.simulateClick
import com.angcyo.widget.edit.AutoCompleteEditText
import com.angcyo.widget.edit.DslEditText
import com.angcyo.widget.image.DslImageView
import com.angcyo.widget.layout.DslFlowLayout
import com.angcyo.widget.layout.DslSoftInputLayout
import com.angcyo.widget.pager.DslViewPager
import com.angcyo.widget.progress.DslProgressBar
import com.angcyo.widget.progress.DslSeekBar
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

fun DslViewHolder.vp(@IdRes id: Int): ViewPager? = v(id)
fun DslViewHolder.vp2(@IdRes id: Int): ViewPager2? = v(id)
fun DslViewHolder.tab(@IdRes id: Int): DslTabLayout? = v(id)
fun DslViewHolder.button(@IdRes id: Int): DslButton? = v(id)
fun DslViewHolder.drv(@IdRes id: Int): DslRecyclerView? = v(id)
fun DslViewHolder._rv(@IdRes id: Int): DslRecyclerView? = v(id)
fun DslViewHolder._tv(@IdRes id: Int): DslTextView? = v(id)
fun DslViewHolder._et(@IdRes id: Int): DslEditText? = v(id)
fun DslViewHolder._ev(@IdRes id: Int): DslEditText? = v(id)
fun DslViewHolder.flow(@IdRes id: Int): DslFlowLayout? = v(id)

/**返回true, 表示有空字符串*/
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

fun DslViewHolder._img(@IdRes id: Int): DslImageView? = v(id)
fun DslViewHolder.spinner(@IdRes id: Int): RSpinner? = v(id)
fun DslViewHolder.bar(@IdRes id: Int): DslProgressBar? = v(id)
fun DslViewHolder.seek(@IdRes id: Int): DslSeekBar? = v(id)

fun DslViewHolder.seek(
    @IdRes id: Int,
    changed: (value: Int, fraction: Float, fromUser: Boolean) -> Unit
): DslSeekBar? {
    val view = v<DslSeekBar>(id)
    view?.config {
        onSeekChanged = changed
    }
    return view
}

fun DslViewHolder.soft(@IdRes id: Int): DslSoftInputLayout? = v(id)

/**快速设置[AutoCompleteEditText]下拉输入框数据*/
fun DslViewHolder.auto(
    @IdRes resId: Int,
    dataList: List<CharSequence>?,
    showOnFocus: Boolean = true,
    focusDelay: Long = 0L,
    notifyFirst: Boolean = true
): AutoCompleteEditText? {
    val auto: AutoCompleteEditText? = v(resId)
    auto?.setDataList(dataList ?: emptyList(), showOnFocus, focusDelay, notifyFirst)
    return auto
}

fun DslViewHolder._vp(@IdRes id: Int): DslViewPager? = v(id)

/**设置hawk指定的key value, 返回旧值*/
fun DslViewHolder.hawkTag(@IdRes id: Int): Any? {
    return tag(id, R.id.lib_tag_hawk, "$id")
}

/**错误提示*/
fun DslViewHolder.error(@IdRes vararg ids: Int): DslViewHolder {
    for (id in ids) {
        view(id)?.error()
    }
    return this
}