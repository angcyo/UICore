package com.angcyo.item

import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.item.style.ButtonItemConfig
import com.angcyo.item.style.IButtonItem
import com.angcyo.widget.DslButton
import com.angcyo.widget.DslViewHolder

/**
 * 带有[DslButton]的item
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/26
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class DslButtonItem : DslAdapterItem(), IButtonItem {

    override var buttonItemConfig = ButtonItemConfig()

    init {
        itemLayoutId = R.layout.dsl_button_item
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)
    }

    override fun _initItemListener(itemHolder: DslViewHolder) {
        //去掉整体item的事件监听
        //super._initItemListener(itemHolder)
    }
}