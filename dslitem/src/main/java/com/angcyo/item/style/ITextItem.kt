package com.angcyo.item.style

import com.angcyo.dsladapter.item.IDslItem
import com.angcyo.item.R
import com.angcyo.widget.DslViewHolder

/**
 * 带Text的item
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/07/16
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
interface ITextItem : IDslItem {

    /**[R.id.lib_text_view]*/
    var itemTextViewId: Int

    /**条目文本*/
    var itemText: CharSequence?

    /**统一样式配置*/
    var itemTextStyle: TextStyleConfig

    /**初始化*/
    fun initTextItem(itemHolder: DslViewHolder) {
        //itemHolder.gone(itemTextViewId, itemTextStyle.text == null)
        itemHolder.tv(itemTextViewId)?.apply {
            itemTextStyle.updateStyle(this)
        }
    }

    fun configTextStyle(action: TextStyleConfig.() -> Unit) {
        itemTextStyle.action()
    }
}