package com.angcyo.item

import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.item.style.ILabelItem
import com.angcyo.item.style.LabelItemConfig
import com.angcyo.widget.DslViewHolder

/**
 * 简单的搜索输入框提示样式item
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/09/22
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class DslSearchTipItem : DslAdapterItem(), ILabelItem {

    override var labelItemConfig: LabelItemConfig = LabelItemConfig()

    init {
        itemLayoutId = R.layout.dsl_search_tip_item

        labelItemConfig.itemLabelText = "请输入搜索关键字"
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