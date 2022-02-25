package com.angcyo.item

import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.item.style.CheckItemConfig
import com.angcyo.item.style.ICheckItem
import com.angcyo.item.style.ITextItem
import com.angcyo.item.style.TextItemConfig
import com.angcyo.widget.DslViewHolder

/**
 * 选中/未选中的item
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/02/25
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
open class DslCheckItem : DslAdapterItem(), ITextItem, ICheckItem {

    override var textItemConfig: TextItemConfig = TextItemConfig()

    override var checkItemConfig: CheckItemConfig = CheckItemConfig()

    init {
        itemLayoutId = R.layout.lib_check_item
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)
    }
}