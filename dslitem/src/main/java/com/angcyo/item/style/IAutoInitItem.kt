package com.angcyo.item.style

import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.dsladapter.item.IDslItem
import com.angcyo.widget.DslViewHolder

/**
 * 自动初始化, 继承此类的item, 可以实现自动初始化
 * [com.angcyo.dsladapter.DslAdapterItem._initItemConfig]
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/09/23
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
interface IAutoInitItem : IDslItem {
    override fun initItemConfig(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.initItemConfig(itemHolder, itemPosition, adapterItem, payloads)
        if (this is IImageItem) {
            initImageItem(itemHolder, payloads)
        }
        if (this is IBadgeItem) {
            initBadgeItem(itemHolder)
        }
        if (this is IDesItem) {
            initDesItem(itemHolder)
        }
        if (this is ITextItem) {
            initTextItem(itemHolder)
        }
        if (this is IEditItem) {
            initEditItem(itemHolder)
        }
        if (this is ILabelItem) {
            initLabelItem(itemHolder)
        }
        if (this is ITextInfoItem) {
            initInfoTextItem(itemHolder)
        }
        if (this is IBodyItem) {
            initBodyItem(itemHolder)
        }
        if (this is IButtonItem) {
            initButtonItem(itemHolder)
        }
        if (this is INestedRecyclerItem) {
            initNestedRecyclerItem(itemHolder, itemPosition, adapterItem, payloads)
        }
    }
}