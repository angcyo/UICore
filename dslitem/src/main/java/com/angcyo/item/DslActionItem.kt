package com.angcyo.item

import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.item.style.IImageItem
import com.angcyo.item.style.ITextItem
import com.angcyo.item.style.ImageItemConfig
import com.angcyo.item.style.TextItemConfig
import com.angcyo.widget.DslViewHolder

/**
 * 图片+文本+图片的item
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/09/27
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class DslActionItem : DslAdapterItem(), IImageItem, ITextItem {

    override var imageItemConfig: ImageItemConfig = ImageItemConfig()
    override var textItemConfig: TextItemConfig = TextItemConfig()

    init {
        itemLayoutId = R.layout.dsl_action_item
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