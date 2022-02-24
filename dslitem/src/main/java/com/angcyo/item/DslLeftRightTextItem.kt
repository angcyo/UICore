package com.angcyo.item

import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.item.style.TextItemConfig
import com.angcyo.widget.DslViewHolder

/**
 * 左右共2个TextView的item
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/02/24
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
open class DslLeftRightTextItem : DslAdapterItem() {

    var leftTextItemConfig = TextItemConfig().apply {
        itemTextStyle.goneOnTextEmpty = true
        itemTextViewId = R.id.lib_left_text_view
    }

    var rightTextItemConfig = TextItemConfig().apply {
        itemTextStyle.goneOnTextEmpty = true
        itemTextViewId = R.id.lib_right_text_view
    }

    init {
        itemLayoutId = R.layout.dsl_left_right_text_item
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)

        itemHolder.tv(leftTextItemConfig.itemTextViewId)?.apply {
            leftTextItemConfig.itemTextStyle.updateStyle(this)
        }

        itemHolder.tv(rightTextItemConfig.itemTextViewId)?.apply {
            rightTextItemConfig.itemTextStyle.updateStyle(this)
        }
    }
}

var DslLeftRightTextItem.itemLeftText: CharSequence?
    get() = leftTextItemConfig.itemText
    set(value) {
        leftTextItemConfig.itemText = value
    }

var DslLeftRightTextItem.itemRightText: CharSequence?
    get() = rightTextItemConfig.itemText
    set(value) {
        rightTextItemConfig.itemText = value
    }