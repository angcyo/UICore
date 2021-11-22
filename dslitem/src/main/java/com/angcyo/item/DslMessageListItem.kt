package com.angcyo.item

import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.item.style.*
import com.angcyo.widget.DslViewHolder

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/09/23
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
open class DslMessageListItem : DslAdapterItem(), IImageItem, ITextItem, IDesItem, IBadgeItem {

    override var imageItemConfig = ImageItemConfig()

    override var textItemConfig = TextItemConfig()

    override var desItemConfig = DesItemConfig()

    override var badgeItemConfig: BadgeItemConfig = BadgeItemConfig()

    /**时间*/
    var itemTime: CharSequence? = null

    init {
        itemLayoutId = R.layout.dsl_message_list_item
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)

        itemHolder.tv(R.id.lib_time_view)?.text = itemTime
    }
}