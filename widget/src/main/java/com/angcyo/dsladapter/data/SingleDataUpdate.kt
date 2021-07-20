package com.angcyo.dsladapter.data

import com.angcyo.dsladapter.DslAdapter
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.dsladapter.afterItem
import com.angcyo.dsladapter.beforeItem
import com.angcyo.library.ex.className
import com.angcyo.library.ex.size

/**
 * [DslAdapter] 普通界面, 非列表界面的数据更新方式
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/07/20
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class SingleDataUpdate(val adapter: DslAdapter) {

    val opList = mutableListOf<Op>()

    fun remove(predicate: (index: Int, item: DslAdapterItem) -> Boolean) {
        adapter.adapterItems.forEachIndexed { index, dslAdapterItem ->
            if (predicate(index, dslAdapterItem)) {
                opList.add(Op(Op.REMOVE, dslAdapterItem))
            }
        }
    }

    inline fun <reified Item : DslAdapterItem> removeItem() {
        update { _, dslAdapterItem ->
            dslAdapterItem is Item
        }
    }

    fun add(item: DslAdapterItem, width: DslAdapterItem? = null) {
        opList.add(Op(Op.ADD, item, width, addItemList = listOf(item)))
    }


    fun addWidth(predicate: (index: Int, item: DslAdapterItem) -> Boolean, item: DslAdapterItem) {
        adapter.adapterItems.forEachIndexed { index, dslAdapterItem ->
            if (predicate(index, dslAdapterItem)) {
                opList.add(Op(Op.ADD, dslAdapterItem, dslAdapterItem, addItemList = listOf(item)))
            }
        }
    }

    /**需要更新的item
     * [predicate] 返回true, 表示当前的[DslAdapterItem]需要更新
     * */
    fun update(predicate: (index: Int, item: DslAdapterItem) -> Boolean) {
        adapter.adapterItems.forEachIndexed { index, dslAdapterItem ->
            if (predicate(index, dslAdapterItem)) {
                opList.add(Op(Op.UPDATE, dslAdapterItem))
            }
        }
    }

    /**
     * 通过类名, 快速匹配item进行更新
     * */
    inline fun <reified Item : DslAdapterItem> updateItem(crossinline init: Item.(index: Int) -> Unit) {
        update { index, dslAdapterItem ->
            if (dslAdapterItem is Item) {
                dslAdapterItem.init(index)
                true
            } else {
                false
            }
        }
    }

    /**开始批量更新item
     * [width] 从这个item后面开始批量更新相同类型的item, 不包含[width]*/
    fun <Item : DslAdapterItem> updateListWidth(
        itemClass: Class<Item>,
        width: DslAdapterItem?,
        dataList: List<Any?>?,
        initItem: Item.(data: Any?, dataIndex: Int) -> Unit = { _, _ -> }
    ) {
        val start = width?.afterItem(adapter)
        if (start == null) {
            //全部添加
            val newAddList = mutableListOf<DslAdapterItem>()
            dataList?.forEachIndexed { index, any ->
                val newItem = updateOrCreateItemByClass(itemClass, null) {
                    initItem(this, index)
                }
                if (newItem != null) {
                    //add item
                    newAddList.add(newItem)
                }
                if (newAddList.isNotEmpty()) {
                    opList.add(Op(Op.ADD, width, addAnchorItem = width, addItemList = newAddList))
                }
            }
        } else {
            updateListStart(itemClass, start, dataList, initItem)
        }
    }

    /**开始批量更新item
     * [start] 从这个item开始批量更新相同类型的item, 包含[start]*/
    fun <Item : DslAdapterItem> updateListStart(
        itemClass: Class<Item>,
        start: DslAdapterItem,
        dataList: List<Any?>?,
        initItem: Item.(data: Any?, dataIndex: Int) -> Unit = { _, _ -> }
    ) {

        //界面上已经存在的连续item
        val oldItemList = mutableListOf<DslAdapterItem>()
        var findAnchor = false
        adapter.adapterItems.forEach { item ->
            if (item == start) {
                findAnchor = true
            }
            if (findAnchor) {
                if (item.className() == start.className()) {
                    oldItemList.add(item)
                } else {
                    //不一样的item, 中断forEach
                    return@forEach
                }
            }
        }

        val updateStartIndex = 0
        val updateEndIndex = updateStartIndex + dataList.size()

        val newAddList = mutableListOf<DslAdapterItem>()

        //添加item操作时的锚点
        var addAnchorItem = start.beforeItem(adapter)

        for (index in updateStartIndex until updateEndIndex) {

            val data = dataList?.getOrNull(index)
            val oldItem = oldItemList.getOrNull(index)

            val newItem = updateOrCreateItemByClass(itemClass, oldItem) {
                initItem(this, index)
            }

            /*if (newItem != null) {
                newItem.itemChanging = true
                newItem.itemData = data
            }*/
            if (oldItem == null) {
                if (newItem != null) {
                    //add item
                    newAddList.add(newItem)
                }
            } else {
                addAnchorItem = oldItem
                when {
                    //remove old item
                    newItem == null -> {
                        opList.add(Op(Op.REMOVE, oldItem))
                    }
                    //replace old item
                    oldItem != newItem -> {
                        opList.add(Op(Op.REPLACE, oldItem, newItem))
                    }
                    //update old item
                    oldItem == newItem -> {
                        opList.add(Op(Op.UPDATE, oldItem))
                    }
                }
            }
        }

        //超范围的旧数据
        for (i in updateEndIndex until oldItemList.size) {
            opList.add(Op(Op.REMOVE, oldItemList[i]))
        }

        if (newAddList.isNotEmpty()) {
            opList.add(Op(Op.ADD, start, addAnchorItem = addAnchorItem, addItemList = newAddList))
        }
    }

    /**开发派发更新*/
    fun doIt() {
        if (opList.isEmpty()) {
            return
        }

        //先处理add, 防止锚点不见
        opList.forEach { op ->
            val adapterItems = adapter.adapterItems
            when (op.op) {
                Op.ADD -> {
                    val addAnchorItem = op.addAnchorItem

                    if (addAnchorItem == null) {
                        adapterItems.addAll(op.addItemList ?: emptyList())
                    } else {
                        val index = adapterItems.indexOfFirst {
                            it == addAnchorItem
                        }
                        if (index != -1) {
                            adapterItems.addAll(index + 1, op.addItemList ?: emptyList())
                        }
                    }
                }
            }
        }

        val adapterItems = adapter.adapterItems
        //在处理其他
        opList.forEach { op ->
            when (op.op) {
                Op.REMOVE -> {
                    adapterItems.remove(op.item)
                }
                Op.UPDATE -> {
                    op.item?.itemChanging = true
                }
                Op.REPLACE -> {
                    val index = adapterItems.indexOfFirst {
                        it == op.item
                    }
                    if (index != -1) {
                        op.replaceItem?.let {
                            adapterItems.set(index, it)
                        }
                    }
                }
            }
        }

        //触发更新
        adapter.updateItemDepend()
    }

    data class Op(
        val op: Int,
        val item: DslAdapterItem?,
        val addAnchorItem: DslAdapterItem? = null,
        val replaceItem: DslAdapterItem? = null,
        val addItemList: List<DslAdapterItem>? = null
    ) {
        companion object {
            //无操作
            const val NO = 0b000000000

            //添加操作
            const val ADD = 0b00000001

            //移除操作
            const val REMOVE = 0b00010

            //更新操作
            const val UPDATE = 0b00100

            //替换操作
            const val REPLACE = 0b01000
        }
    }
}

fun DslAdapter.updateAdapter(update: SingleDataUpdate.() -> Unit) {
    SingleDataUpdate(this).apply {
        update()
        doIt()
    }
}