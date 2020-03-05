package com.angcyo.dsladapter

import android.graphics.Color
import androidx.annotation.LayoutRes
import com.angcyo.library.ex.dpi
import com.angcyo.widget.R
import com.angcyo.widget.base.setHeight


/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/08/09
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

//<editor-fold desc="Item操作">

/**
 * 通过条件, 查找[DslAdapterItem].
 *
 * @param useFilterList 是否使用过滤后的数据源. 通常界面上显示的是过滤后的数据, 所有add的数据源在非过滤列表中
 * */
fun DslAdapter.findItem(
    useFilterList: Boolean = true,
    predicate: (DslAdapterItem) -> Boolean
): DslAdapterItem? {
    return getDataList(useFilterList).find(predicate)
}

fun DslAdapter.updateItem(
    payload: Any? = DslAdapterItem.PAYLOAD_UPDATE_PART,
    useFilterList: Boolean = true,
    predicate: (DslAdapterItem) -> Boolean
): DslAdapterItem? {
    return findItem(useFilterList, predicate)?.apply {
        updateAdapterItem(payload, useFilterList)
    }
}

fun DslAdapter.findItemByTag(
    tag: String,
    useFilterList: Boolean = true
): DslAdapterItem? {
    return findItem(useFilterList) {
        it.itemTag == tag
    }
}

fun DslAdapter.dslItem(@LayoutRes layoutId: Int, config: DslAdapterItem.() -> Unit = {}) {
    val item = DslAdapterItem()
    item.itemLayoutId = layoutId
    addLastItem(item)
    item.config()
}

fun <T : DslAdapterItem> DslAdapter.dslItem(
    dslItem: T,
    config: T.() -> Unit = {}
) {
    dslCustomItem(dslItem, config)
}

fun <T : DslAdapterItem> DslAdapter.dslCustomItem(
    dslItem: T,
    config: T.() -> Unit = {}
) {
    addLastItem(dslItem)
    dslItem.config()
}

/**空的占位item*/
fun DslAdapter.renderEmptyItem(height: Int = 120 * dpi, color: Int = Color.TRANSPARENT) {
    val adapterItem = DslAdapterItem()
    adapterItem.itemLayoutId = R.layout.lib_empty_item
    adapterItem.itemBindOverride = { itemHolder, _, _, _ ->
        itemHolder.itemView.setBackgroundColor(color)
        itemHolder.itemView.setHeight(height)
    }
    addLastItem(adapterItem)
}

fun DslAdapter.renderItem(count: Int = 1, init: DslAdapterItem.(index: Int) -> Unit) {
    for (i in 0 until count) {
        val adapterItem = DslAdapterItem()
        adapterItem.init(i)
        addLastItem(adapterItem)
    }
}

fun <T> DslAdapter.renderItem(data: T, init: DslAdapterItem.() -> Unit) {
    val adapterItem = DslAdapterItem()
    adapterItem.itemData = data
    adapterItem.init()
    addLastItem(adapterItem)
}

/**获取所有指定类型的数据集合*/
inline fun <reified ItemData> DslAdapter.getAllItemData(useFilterList: Boolean = true): List<ItemData> {
    val result = mutableListOf<ItemData>()
    val itemList = getDataList(useFilterList)
    for (item in itemList) {
        if (item.itemData is ItemData) {
            result.add(item.itemData as ItemData)
        }
    }
    return result
}

//</editor-fold desc="Item操作">

//<editor-fold desc="payload">

/**是否包含指定的[payload]*/
fun Iterable<*>.containsPayload(any: Any): Boolean {
    var result = false
    for (payload in this) {
        result = if (payload is Iterable<*>) {
            payload.containsPayload(any)
        } else {
            payload == any
        }
        if (result) {
            break
        }
    }
    return result
}

/**是否要更新媒体, 比如:图片*/
fun Iterable<*>.updateMedia(): Boolean {
    return count() <= 0 || containsPayload(DslAdapterItem.PAYLOAD_UPDATE_MEDIA)
}

//</editor-fold desc="payload">

