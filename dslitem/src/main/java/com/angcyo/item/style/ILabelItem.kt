package com.angcyo.item.style

import com.angcyo.dsladapter.item.IDslItem
import com.angcyo.item.R
import com.angcyo.widget.DslViewHolder

/**
 * 带Label的item
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/06/28
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
interface ILabelItem : IDslItem {

    /**[R.id.lib_label_view]*/
    var itemLabelTextViewId: Int

    /**Label文本*/
    var itemLabelText: CharSequence?

    /**统一样式配置*/
    var itemLabelTextStyle: TextStyleConfig

    /**初始化*/
    fun initLabelItem(itemHolder: DslViewHolder) {
        itemHolder.gone(itemLabelTextViewId, itemLabelTextStyle.text == null)
        itemHolder.tv(itemLabelTextViewId)?.apply {
            itemLabelTextStyle.updateStyle(this)
        }
    }

    fun configLabelTextStyle(action: TextStyleConfig.() -> Unit) {
        itemLabelTextStyle.action()
    }
}