package com.angcyo.item

import androidx.recyclerview.widget.RecyclerView
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.item.style.INestedRecyclerItem
import com.angcyo.item.style.NestedRecyclerItemConfig
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.R

/**
 * 内嵌[RecyclerView]的item
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/19
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class DslNestedRecyclerItem : DslAdapterItem(), INestedRecyclerItem {

    override var nestedRecyclerItemConfig: NestedRecyclerItemConfig = NestedRecyclerItemConfig()

    init {
        itemLayoutId = R.layout.dsl_nested_recycler_item
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