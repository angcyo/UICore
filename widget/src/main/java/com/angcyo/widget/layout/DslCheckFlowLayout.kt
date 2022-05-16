package com.angcyo.widget.layout

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.angcyo.tablayout.DslSelector

/**
 * [android.widget.RadioGroup]
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/01/10
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
open class DslCheckFlowLayout(context: Context, attrs: AttributeSet? = null) :
    DslFlowLayout(context, attrs) {

    /**单选/多选支持*/
    val selectorHelper = DslSelector()

    init {
        selectorHelper.install(this) {
            dslMultiMode = false
        }
    }

    override fun onViewAdded(child: View?) {
        super.onViewAdded(child)
        updateLayout()
    }

    override fun onViewRemoved(child: View?) {
        super.onViewRemoved(child)
        updateLayout()
    }

    open fun updateLayout() {
        selectorHelper.updateVisibleList()
        selectorHelper.updateStyle()
        selectorHelper.updateClickListener()
    }

    /**选中目标*/
    open fun selectIndex(index: Int) {
        selectorHelper.selector(index)
    }

    /**监听选中改变*/
    open fun onSelectChanged(listener: (fromIndex: Int, selectIndexList: List<Int>, reselect: Boolean, fromUser: Boolean) -> Unit) {
        selectorHelper.dslSelectorConfig.onSelectIndexChange = listener
    }

}