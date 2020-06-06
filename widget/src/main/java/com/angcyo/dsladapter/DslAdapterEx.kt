package com.angcyo.dsladapter

import android.graphics.Color
import androidx.annotation.LayoutRes
import com.angcyo.library.ex.dpi
import com.angcyo.library.model.Page
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.R
import com.angcyo.widget.base.Anim
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
    tag: String?,
    useFilterList: Boolean = true
): DslAdapterItem? {
    if (tag == null) {
        return null
    }
    return findItem(useFilterList) {
        it.itemTag == tag
    }
}

fun DslAdapter.findItemByGroup(
    groups: List<String>,
    useFilterList: Boolean = true
): List<DslAdapterItem> {
    return getDataList(useFilterList).findItemByGroup(groups)
}

/**通过Tag查找item*/
fun List<DslAdapterItem>.findItemByTag(tag: String?): DslAdapterItem? {
    if (tag == null) {
        return null
    }
    return find {
        it.itemTag == tag
    }
}

/**通过group查找item*/
fun List<DslAdapterItem>.findItemByGroup(groups: List<String>): List<DslAdapterItem> {
    val result = mutableListOf<DslAdapterItem>()

    groups.forEach { group ->
        forEach {
            if (it.itemGroups.contains(group)) {
                result.add(it)
            }
        }
    }
    return result
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
fun DslAdapter.renderEmptyItem(
    height: Int = 120 * dpi,
    color: Int = Color.TRANSPARENT,
    action: DslAdapterItem.() -> Unit = {}
) {
    val adapterItem = DslAdapterItem()
    adapterItem.itemLayoutId = R.layout.lib_empty_item
    adapterItem.itemBindOverride = { itemHolder, _, _, _ ->
        itemHolder.itemView.setBackgroundColor(color)
        itemHolder.itemView.setHeight(height)
    }
    adapterItem.action()
    addLastItem(adapterItem)
}

/**换个贴切的名字*/
fun DslAdapter.render(action: DslAdapter.() -> Unit) {
    this.action()
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

/**枚举所有Item*/
fun DslAdapter.eachItem(
    useFilterList: Boolean = true,
    action: (index: Int, dslAdapterItem: DslAdapterItem) -> Unit
) {
    getDataList(useFilterList).forEachIndexed(action)
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
fun Iterable<*>.isUpdateMedia(): Boolean {
    return count() <= 0 || containsPayload(DslAdapterItem.PAYLOAD_UPDATE_MEDIA)
}

/**需要更新媒体的负载*/
fun mediaPayload(): List<Int> =
    listOf(DslAdapterItem.PAYLOAD_UPDATE_PART, DslAdapterItem.PAYLOAD_UPDATE_MEDIA)

//</editor-fold desc="payload">

//<editor-fold desc="AdapterStatus">

fun DslAdapter.justRunFilterParams() = defaultFilterParams!!.apply {
    justRun = true
    asyncDiff = false
}

fun DslAdapter.toLoading(filterParams: FilterParams = justRunFilterParams()) {
    setAdapterStatus(DslAdapterStatusItem.ADAPTER_STATUS_LOADING, filterParams)
}

fun DslAdapter.toEmpty(filterParams: FilterParams = justRunFilterParams()) {
    setAdapterStatus(DslAdapterStatusItem.ADAPTER_STATUS_EMPTY, filterParams)
}

fun DslAdapter.toError(filterParams: FilterParams = justRunFilterParams()) {
    setAdapterStatus(DslAdapterStatusItem.ADAPTER_STATUS_ERROR, filterParams)
}

fun DslAdapter.toNone(filterParams: FilterParams = justRunFilterParams()) {
    setAdapterStatus(DslAdapterStatusItem.ADAPTER_STATUS_NONE, filterParams)
}

fun DslAdapter.toLoadMoreError() {
    setLoadMore(DslLoadMoreItem.LOAD_MORE_ERROR)
}

/**加载更多技术*/
fun DslAdapter.toLoadMoreEnd() {
    setLoadMore(DslLoadMoreItem.LOAD_MORE_NORMAL)
}

/**无更多*/
fun DslAdapter.toLoadNoMore() {
    setLoadMore(DslLoadMoreItem.LOAD_MORE_NO_MORE)
}

/**快速同时监听刷新/加载更多的回调*/
fun DslAdapter.onRefreshOrLoadMore(action: (itemHolder: DslViewHolder, loadMore: Boolean) -> Unit) {
    dslAdapterStatusItem.onRefresh = {
        action(it, false)
    }
    dslLoadMoreItem.onLoadMore = {
        action(it, true)
    }
}

/**
 * 单一数据类型加载完成后, 调用此方法.
 * 自动处理, 情感图切换, 加载更多切换.
 * */
fun <Item : DslAdapterItem, Bean> DslAdapter.loadDataEnd(
    itemClass: Class<Item>,
    dataList: List<Bean>?,
    error: Throwable?,
    page: Page,
    initItem: Item.(data: Bean) -> Unit = {}
) {
    if (error != null) {
        //加载失败
        if (adapterItems.isEmpty()) {
            dslAdapterStatusItem.onBindStateLayout = { itemHolder, state ->
                if (state == DslAdapterStatusItem.ADAPTER_STATUS_ERROR) {
                    itemHolder.tv(R.id.lib_text_view)?.text = error.message
                }
            }
            toError()
        } else {
            toLoadMoreError()
        }
        return
    } else {
        //加载成功
        page.pageLoadEnd()
    }

    //更新数据源
    updateData {
        updatePage = page.requestPageIndex
        pageSize = page.requestPageSize
        updateDataList = dataList as List<Any>?
        this.updateOrCreateItem = { oldItem, data, _ ->
            var newItem = oldItem
            if (oldItem == null) {
                newItem = itemClass.newInstance()
            }
            (newItem as Item?)?.apply {
                this.initItem(data as Bean)
            }
        }
    }
}

//</editor-fold desc="AdapterStatus">

//<editor-fold desc="Update">

/**立即更新*/
fun DslAdapter.updateNow(filterParams: FilterParams = justRunFilterParams()) =
    updateItemDepend(filterParams)

/**延迟通知*/
fun DslAdapter.delayNotify(filterParams: FilterParams = FilterParams(notifyDiffDelay = Anim.ANIM_DURATION)) {
    updateItemDepend(filterParams)
}

//</editor-fold desc="Update">

