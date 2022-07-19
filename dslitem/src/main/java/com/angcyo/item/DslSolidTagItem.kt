package com.angcyo.item

import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.item.style.DesItemConfig
import com.angcyo.item.style.IDesItem
import com.angcyo.item.style.ILabelItem
import com.angcyo.item.style.LabelItemConfig
import com.angcyo.widget.DslViewHolder

/**
 * 左边solid label, 右边solid des
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/07/19
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
open class DslSolidTagItem : DslAdapterItem(), ILabelItem, IDesItem {

    override var desItemConfig: DesItemConfig = DesItemConfig()

    override var labelItemConfig: LabelItemConfig = LabelItemConfig()

    init {
        itemLayoutId = R.layout.dsl_solid_tag_item
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