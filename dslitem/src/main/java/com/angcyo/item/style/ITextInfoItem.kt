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
interface ITextInfoItem : IDslItem {

    /**[R.id.lib_text_view]*/
    var itemInfoTextViewId: Int

    /**条目文本*/
    var itemInfoText: CharSequence?

    /**统一样式配置*/
    var itemInfoTextStyle: TextStyleConfig

    /**初始化*/
    fun initInfoTextItem(itemHolder: DslViewHolder) {
        itemHolder.gone(itemInfoTextViewId, itemInfoTextStyle.text == null)
        itemHolder.tv(itemInfoTextViewId)?.apply {
            itemInfoTextStyle.updateStyle(this)
        }
    }

    fun configInfoTextStyle(action: TextStyleConfig.() -> Unit) {
        itemInfoTextStyle.action()
    }
}