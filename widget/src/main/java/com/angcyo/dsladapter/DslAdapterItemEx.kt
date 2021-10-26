package com.angcyo.dsladapter

import android.graphics.Color
import androidx.annotation.LayoutRes
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.angcyo.dsladapter.data.UpdateDataConfig
import com.angcyo.dsladapter.data.updateData
import com.angcyo.library.L
import com.angcyo.library.ex._color
import com.angcyo.library.ex._dimen
import com.angcyo.library.ex.dpi
import com.angcyo.library.model.Page
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.R
import com.angcyo.widget.layout.touch.SwipeBackLayout.Companion.clamp

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/22
 */

//<editor-fold desc="DslAdapterItem结构转换">

/**
 * 将list结构体, 打包成dslItem
 * */
fun List<Any>.toDslItemList(
    @LayoutRes layoutId: Int = -1,
    config: DslAdapterItem.() -> Unit = {}
): MutableList<DslAdapterItem> {
    return toDslItemList(DslAdapterItem::class.java, layoutId, config)
}

fun List<Any>.toDslItemList(
    dslItem: Class<out DslAdapterItem>,
    @LayoutRes layoutId: Int = -1,
    config: DslAdapterItem.() -> Unit = {}
): MutableList<DslAdapterItem> {
    return toDslItemList(itemFactory = { _, item ->
        dslItem.newInstance().apply {
            if (layoutId != -1) {
                itemLayoutId = layoutId
            }
            config()
        }
    })
}

fun List<Any>.toDslItemList(
    itemBefore: (itemList: MutableList<DslAdapterItem>, index: Int, item: Any) -> Unit = { _, _, _ -> },
    itemFactory: (index: Int, item: Any) -> DslAdapterItem,
    itemAfter: (itemList: MutableList<DslAdapterItem>, index: Int, item: Any) -> Unit = { _, _, _ -> }
): MutableList<DslAdapterItem> {
    return toAnyList(itemBefore, { index, any ->
        val item = itemFactory(index, any)
        item.itemData = any
        item
    }, itemAfter)
}

fun <T> List<Any>.toAnyList(
    itemBefore: (itemList: MutableList<T>, index: Int, item: Any) -> Unit = { _, _, _ -> },
    itemFactory: (index: Int, item: Any) -> T,
    itemAfter: (itemList: MutableList<T>, index: Int, item: Any) -> Unit = { _, _, _ -> }
): MutableList<T> {
    val result = mutableListOf<T>()

    forEachIndexed { index, any ->
        itemBefore(result, index, any)
        val item = itemFactory(index, any)
        result.add(item)
        itemAfter(result, index, any)
    }
    return result
}

//</editor-fold desc="DslAdapterItem结构转换">

//<editor-fold desc="分割线操作扩展">

fun DslAdapterItem.setTopInsert(insert: Int, leftOffset: Int = 0, rightOffset: Int = 0) {
    itemTopInsert = insert
    itemRightOffset = rightOffset
    itemLeftOffset = leftOffset
}

fun DslAdapterItem.setBottomInsert(insert: Int, leftOffset: Int = 0, rightOffset: Int = 0) {
    itemBottomInsert = insert
    itemRightOffset = rightOffset
    itemLeftOffset = leftOffset
}

fun DslAdapterItem.setLeftInsert(insert: Int, topOffset: Int = 0, bottomOffset: Int = 0) {
    itemLeftInsert = insert
    itemBottomOffset = bottomOffset
    itemTopOffset = topOffset
}

fun DslAdapterItem.setRightInsert(insert: Int, topOffset: Int = 0, bottomOffset: Int = 0) {
    itemRightInsert = insert
    itemBottomOffset = bottomOffset
    itemTopOffset = topOffset
}

fun DslAdapterItem.margin(margin: Int, color: Int = Color.TRANSPARENT) {
    itemLeftInsert = margin
    itemRightInsert = margin
    itemTopInsert = margin
    itemBottomInsert = margin

    itemLeftOffset = 0
    itemRightOffset = 0
    itemTopOffset = 0
    itemBottomOffset = 0

    onlyDrawOffsetArea = false
    itemDecorationColor = color
}

fun DslAdapterItem.marginVertical(top: Int, bottom: Int = top, color: Int = Color.TRANSPARENT) {
    itemLeftOffset = 0
    itemRightOffset = 0
    itemTopInsert = top
    itemBottomInsert = bottom
    onlyDrawOffsetArea = false
    itemDecorationColor = color
}

fun DslAdapterItem.marginHorizontal(left: Int, right: Int = left, color: Int = Color.TRANSPARENT) {
    itemTopOffset = 0
    itemBottomOffset = 0

    itemLeftInsert = left
    itemRightInsert = right
    onlyDrawOffsetArea = false
    itemDecorationColor = color
}

fun DslAdapterItem.padding(padding: Int) {
    itemPaddingLeft = padding
    itemPaddingTop = padding
    itemPaddingRight = padding
    itemPaddingBottom = padding
}

fun DslAdapterItem.paddingVertical(padding: Int) {
    paddingVertical(padding, padding)
}

fun DslAdapterItem.paddingHorizontal(padding: Int) {
    paddingHorizontal(padding, padding)
}

fun DslAdapterItem.paddingVertical(top: Int, bottom: Int) {
    itemPaddingTop = top
    itemPaddingBottom = bottom
}

fun DslAdapterItem.paddingHorizontal(left: Int, right: Int) {
    itemPaddingLeft = left
    itemPaddingRight = right
}

/**仅绘制左边区域的分割线*/
fun DslAdapterItem.drawLeft(
    offsetLeft: Int,
    insertTop: Int = 1 * dpi,
    color: Int = Color.WHITE
) {
    itemLeftOffset = offsetLeft
    itemRightOffset = 0

    itemTopInsert = insertTop
    itemBottomInsert = 0

    onlyDrawOffsetArea = true
    itemDecorationColor = color
}

/**在底部绘制分割线*/
fun DslAdapterItem.drawBottom(
    insertBottom: Int = _dimen(R.dimen.lib_line_px),
    offsetLeft: Int = _dimen(R.dimen.lib_padding_left),
    offsetRight: Int = _dimen(R.dimen.lib_padding_left),
    color: Int = _color(R.color.lib_line_dark)
) {
    itemLeftOffset = offsetLeft
    itemRightOffset = offsetRight

    itemTopInsert = 0
    itemBottomInsert = insertBottom

    onlyDrawOffsetArea = false
    itemDecorationColor = color
}

/**在底部两边绘制分割线*/
fun DslAdapterItem.drawBottomOffset(
    insertBottom: Int = _dimen(R.dimen.lib_line_px),
    offsetLeft: Int = _dimen(R.dimen.lib_padding_left),
    offsetRight: Int = _dimen(R.dimen.lib_padding_left),
    color: Int = _color(R.color.lib_white)
) {
    itemLeftOffset = offsetLeft
    itemRightOffset = offsetRight

    itemTopInsert = 0
    itemBottomInsert = insertBottom

    onlyDrawOffsetArea = true
    itemDecorationColor = color
}

/**清空分割线绘制参数*/
fun DslAdapterItem.noDraw() {
    itemLeftOffset = 0
    itemTopOffset = 0
    itemRightOffset = 0
    itemBottomOffset = 0

    itemLeftInsert = 0
    itemTopInsert = 0
    itemRightInsert = 0
    itemBottomInsert = 0

    onlyDrawOffsetArea = false
    itemDecorationColor = Color.TRANSPARENT
}

/**不绘制分割线*/
fun DslAdapterItem.noDrawDecoration() {
    noDraw()
}

//</editor-fold desc="分割线操作扩展">

//<editor-fold desc="操作扩展">

/**[item]在[adapterItems]中的索引*/
fun DslAdapterItem.itemIndexPosition(
    dslAdapter: DslAdapter? = null,
    useFilterList: Boolean = true
) = (dslAdapter ?: itemDslAdapter)?.getDataList(useFilterList)?.indexOf(this)
    ?: RecyclerView.NO_POSITION

/**是否在Adapter中最后一个*/
fun DslAdapterItem.isItemLastInAdapter(
    dslAdapter: DslAdapter? = null,
    useFilterList: Boolean = true
): Boolean {
    val list = (dslAdapter ?: itemDslAdapter)?.getDataList(useFilterList)
    return (list?.indexOf(this) ?: RecyclerView.NO_POSITION) == list?.lastIndex
}

/**[item]在[dataItems]中的索引*/
fun DslAdapterItem.itemIndexDataPosition(
    dslAdapter: DslAdapter? = null,
    useFilterList: Boolean = true
) = (dslAdapter ?: itemDslAdapter)?.getDataList(useFilterList)
    ?.filter { (dslAdapter ?: itemDslAdapter)?.dataItems?.contains(it) == true }?.indexOf(this)
    ?: RecyclerView.NO_POSITION

fun DslAdapterItem.itemViewHolder(recyclerView: RecyclerView? = itemDslAdapter?._recyclerView): DslViewHolder? {
    val position = itemIndexPosition()
    return if (position != RecyclerView.NO_POSITION) {
        recyclerView?.findViewHolderForAdapterPosition(position) as? DslViewHolder
    } else {
        null
    }
}

fun DslAdapterItem.isItemAttached(): Boolean {
    return lifecycle.currentState == Lifecycle.State.RESUMED
}

/**是否是占满宽度的item*/
fun DslAdapterItem.isFullWidthItem(): Boolean {
    return isInLinearLayoutManager() || itemSpanCount == DslAdapterItem.FULL_ITEM
}

fun DslAdapterItem.isInLinearLayoutManager(): Boolean {
    if (itemDslAdapter?._recyclerView?.layoutManager is LinearLayoutManager) {
        return true
    }
    return false
}

/**提供和[DslAdapter]相同的使用方式, 快速创建[DslAdapterItem]集合*/
fun renderItemList(render: DslAdapter.() -> Unit): List<DslAdapterItem> {
    return DslAdapter().run {
        dslDataFilter = null
        render()
        adapterItems
    }
}

/**当前的item, 是否包含指定的分组信息*/
fun DslAdapterItem.haveGroup(vararg group: String): Boolean {
    return group.find { itemGroups.contains(it) } != null
}

/**指定的item, 是否在当前item的分组中*/
fun DslAdapterItem.isInGroupItem(targetItem: DslAdapterItem?): Boolean {
    if (targetItem == null) {
        return false
    }
    if (this == targetItem) {
        return true
    }
    return isItemInGroups(targetItem)
}

fun DslAdapterItem.afterItem(
    adapter: DslAdapter,
    useFilterList: Boolean = true,
    predicate: (item: DslAdapterItem, offset: Int) -> Boolean
): DslAdapterItem? {

    var findAnchor = false
    var startIndex = -1
    var result: DslAdapterItem? = null
    adapter.getDataList(useFilterList).forEachIndexed { index, dslAdapterItem ->
        if (this == dslAdapterItem) {
            findAnchor = true
            startIndex = index
        } else {
            if (findAnchor) {
                if (predicate(dslAdapterItem, index - startIndex)) {
                    result = dslAdapterItem
                    return@forEachIndexed
                }
            }
        }
    }
    return result
}

fun DslAdapterItem.beforeItem(
    adapter: DslAdapter,
    useFilterList: Boolean = true,
    predicate: (item: DslAdapterItem, offset: Int) -> Boolean
): DslAdapterItem? {

    var findAnchor = false
    var startIndex = -1
    var result: DslAdapterItem? = null

    val dataList = adapter.getDataList(useFilterList)
    for (index in dataList.lastIndex downTo 0) {
        val dslAdapterItem = dataList[index]

        if (this == dslAdapterItem) {
            findAnchor = true
            startIndex = index
        } else {
            if (findAnchor) {
                if (predicate(dslAdapterItem, startIndex - index)) {
                    result = dslAdapterItem
                    break
                }
            }
        }
    }

    return result
}

/**获取当前[DslAdapterItem]后, 偏移位置的item*/
fun DslAdapterItem.afterItem(
    adapter: DslAdapter,
    useFilterList: Boolean = true,
    offset: Int = 1
): DslAdapterItem? {
    return afterItem(adapter, useFilterList) { item, o ->
        offset == o
    }
}

/**获取当前[DslAdapterItem]前, 偏移位置的item*/
fun DslAdapterItem.beforeItem(
    adapter: DslAdapter,
    useFilterList: Boolean = true,
    offset: Int = 1
): DslAdapterItem? {
    return beforeItem(adapter, useFilterList) { item, o ->
        offset == o
    }
}

/**快速设置item的分组信息*/
var DslAdapterItem.itemGroup: String?
    get() = itemGroups.firstOrNull()
    set(value) {
        if (value == null) {
            itemGroups = listOf()
        } else if (!itemGroups.contains(value)) {
            val groups = itemGroups
            if (groups is MutableList) {
                groups.add(value)
            } else {
                val list = mutableListOf<String>()
                list.addAll(groups)
                list.add(value)
                itemGroups = list
            }
        }
    }

/**找到[DslAdapterItem]对应的*/
fun DslAdapterItem.findViewHolder(recyclerView: RecyclerView? = itemDslAdapter?._recyclerView): RecyclerView.ViewHolder? {
    return itemViewHolder(recyclerView)
}

//</editor-fold desc="操作扩展">

//<editor-fold desc="更新指定的Item">

fun DslAdapter.removeHeaderItem(itemTag: String?, item: DslAdapterItem? = null) {
    val target = item ?: findItemByTag(itemTag, false)
    if (target == null) {
        L.w("移除的目标不存在")
    } else {
        changeHeaderItems {
            it.remove(target)
        }
    }
}

fun DslAdapter.removeItem(itemTag: String?, item: DslAdapterItem? = null) {
    val target = item ?: findItemByTag(itemTag, false)
    if (target == null) {
        L.w("移除的目标不存在")
    } else {
        changeDataItems {
            it.remove(target)
        }
    }
}

fun DslAdapter.removeFooterItem(itemTag: String?, item: DslAdapterItem? = null) {
    val target = item ?: findItemByTag(itemTag, false)
    if (target == null) {
        L.w("移除的目标不存在")
    } else {
        changeFooterItems {
            it.remove(target)
        }
    }
}

/**
 * 更新或者插入指定的Item
 * 如果目标item已存在, 则更新Item, 否则创建新的插入
 * */
inline fun <reified Item : DslAdapterItem> DslAdapter.updateOrInsertItem(
    itemTag: String? /*需要更新的Item*/,
    insertIndex: Int = 0 /*当需要插入时, 插入到列表中的位置*/,
    crossinline updateOrCreateItem: (oldItem: Item) -> Item?
    /*返回null, 则会删除对应的[oldItem], 返回与[oldItem]不一样的item, 则会替换原来的[oldItem]*/
) {
    changeDataItems {
        _updateOrInsertItem(it, itemTag, insertIndex, updateOrCreateItem)
    }
}

inline fun <reified Item : DslAdapterItem> DslAdapter.updateOrInsertHeaderItem(
    itemTag: String?,
    insertIndex: Int = 0,
    crossinline updateOrCreateItem: (oldItem: Item) -> Item?
) {
    changeHeaderItems {
        _updateOrInsertItem(it, itemTag, insertIndex, updateOrCreateItem)
    }
}

inline fun <reified Item : DslAdapterItem> DslAdapter.updateOrInsertFooterItem(
    itemTag: String?,
    insertIndex: Int = 0,
    crossinline updateOrCreateItem: (oldItem: Item) -> Item?
) {

    changeFooterItems {
        _updateOrInsertItem(it, itemTag, insertIndex, updateOrCreateItem)
    }
}

inline fun <reified Item : DslAdapterItem> DslAdapter._updateOrInsertItem(
    itemList: MutableList<DslAdapterItem>,
    itemTag: String? /*需要更新的Item*/,
    insertIndex: Int = 0 /*当需要插入时, 插入到列表中的位置*/,
    crossinline updateOrCreateItem: (oldItem: Item) -> Item?
    /*返回null, 则会删除对应的[oldItem], 返回与[oldItem]不一样的item, 则会替换原来的[oldItem]*/
) {

    //查找已经存在的item
    val findItem = findItemByTag(itemTag, false)

    val oldItem: Item

    //不存在, 或者存在的类型不匹配, 则创建新item
    oldItem = if (findItem == null || findItem !is Item) {
        Item::class.java.newInstance()
    } else {
        findItem
    }

    //回调处理
    val newItem = updateOrCreateItem(oldItem)
    newItem?.itemTag = itemTag

    if (findItem == null && newItem == null) {
        return
    }

    itemList.let {
        if (newItem == null) {
            //需要移除处理
            if (findItem != null) {
                it.remove(findItem)
            }
        } else {
            if (findItem == null) {
                //需要insert处理
                it.add(clamp(insertIndex, 0, it.size), newItem)
            } else {
                //需要更新处理
                findItem.itemChanging = true
                val indexOf = it.indexOf(findItem)
                if (indexOf != -1) {
                    it[indexOf] = newItem
                }
            }
        }
    }
}

/**[itemSubList]*/
fun DslAdapterItem.updateSubItem(action: UpdateDataConfig.() -> Unit) {
    val config = UpdateDataConfig()
    config.updatePage = Page.FIRST_PAGE_INDEX
    config.pageSize = Int.MAX_VALUE
    config.adapterUpdateResult = {
        //no op
    }
    config.adapterCheckLoadMore = {
        //no op
    }
    config.action()

    val subItemList = itemSubList
    val result = config.updateData(subItemList)
    itemSubList.clear()
    itemSubList.addAll(result)

    updateItemDepend(config.filterParams)
}

//</editor-fold desc="更新指定的Item">