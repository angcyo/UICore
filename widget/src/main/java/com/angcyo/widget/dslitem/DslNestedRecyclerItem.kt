package com.angcyo.widget.dslitem

import androidx.recyclerview.widget.RecyclerView
import com.angcyo.dsladapter.DslAdapter
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.dsladapter.updateNow
import com.angcyo.library.app
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.R
import com.angcyo.widget.recycler.*

/**
 * 内嵌[RecyclerView]的item
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/19
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class DslNestedRecyclerItem : DslAdapterItem() {

    /**内嵌适配器*/
    var itemNestedAdapter: DslAdapter = DslAdapter().apply {
        //关闭内部情感图状态
        dslAdapterStatusItem.itemEnable = false
    }

    /**布局管理,
     * 请注意使用:recycleChildrenOnDetach*/
    var itemNestedLayoutManager: RecyclerView.LayoutManager? =
        LinearLayoutManagerWrap(app()).apply {
            recycleChildrenOnDetach = true
        }

    /**自动恢复滚动位置*/
    var itemKeepScrollPosition = true

    /**渲染内部[DslAdapter]数据*/
    var itemRenderNestedAdapter: DslAdapter.() -> Unit = {}

    /**内部[RecyclerView]配置回调*/
    var itemNestedRecyclerViewConfig: RecyclerView.() -> Unit = {}

    init {
        itemLayoutId = R.layout.dsl_nested_recycler_item
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)

        itemHolder.rv(R.id.lib_nested_recycler_view)?.apply {
            onBindRecyclerView(this, itemHolder, itemPosition, adapterItem, payloads)
        }
    }

    var _onScrollListener: RecyclerView.OnScrollListener? = null

    var _scrollPositionConfig: ScrollPositionConfig? = null

    open fun onBindRecyclerView(
        recyclerView: RecyclerView,
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        //列表
        recyclerView.apply {
            //优先清空[OnScrollListener]
            clearOnScrollListeners()
            clearItemDecoration()
            initDsl()

            if (layoutManager != itemNestedLayoutManager) {
                layoutManager = itemNestedLayoutManager
            }

            //关键地方, 如果每次都赋值[adapter], 系统会重置所有缓存.
            if (adapter != itemNestedAdapter) {
                adapter = itemNestedAdapter
            }

            //渲染数据
            if (adapter is DslAdapter) {
                (adapter as DslAdapter).apply {
                    clearItems()
                    itemRenderNestedAdapter()
                    updateNow()
                }
            }

            if (itemKeepScrollPosition) {
                _scrollPositionConfig?.run { restoreScrollPosition(this) }
            }

            object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    _scrollPositionConfig = saveScrollPosition()
                }
            }.apply {
                _onScrollListener = this
                addOnScrollListener(this)
            }

            //配置
            itemNestedRecyclerViewConfig()
        }
    }
}