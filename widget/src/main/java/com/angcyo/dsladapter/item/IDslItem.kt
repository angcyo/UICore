package com.angcyo.dsladapter.item

import androidx.recyclerview.widget.LinearLayoutManager
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.widget.DslViewHolder

/**
 * 空实现, 方便查找实现类
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/07/16
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
interface IDslItem {

    /**统一初始化入口*/
    fun initItemConfig(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        //default
    }

    /**是否是占满宽度的item*/
    fun isFullWidthItem(item: DslAdapterItem): Boolean {
        if (item.itemSpanCount == DslAdapterItem.FULL_ITEM) {
            return true
        }
        if (item.itemDslAdapter?._recyclerView?.layoutManager is LinearLayoutManager) {
            return true
        }
        return false
    }

}

/**
 * 基类, [IDslItem]的配置类
 */
interface IDslItemConfig {
}