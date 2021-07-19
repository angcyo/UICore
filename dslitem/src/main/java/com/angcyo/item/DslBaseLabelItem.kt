package com.angcyo.item

import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.item.style.ILabelItem
import com.angcyo.item.style.TextStyleConfig
import com.angcyo.widget.DslViewHolder

/**
 * 带有Label的item
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/23
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

open class DslBaseLabelItem : DslAdapterItem(), ILabelItem {

    override var itemLabelTextViewId: Int = R.id.lib_label_view

    /**左边的Label文本*/
    override var itemLabelText: CharSequence? = null
        set(value) {
            field = value
            itemLabelTextStyle.text = value
        }

    /**统一样式配置*/
    override var itemLabelTextStyle = TextStyleConfig()

    init {
        itemLayoutId = R.layout.dsl_label_item
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)
        initLabelItem(itemHolder)
    }
}


