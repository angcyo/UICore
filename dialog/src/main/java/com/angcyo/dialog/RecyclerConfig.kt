package com.angcyo.dialog

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.angcyo.dialog.dslitem.DslDialogIconTextItem
import com.angcyo.dialog.dslitem.DslDialogTextItem
import com.angcyo.dialog.popup.MenuPopupConfig
import com.angcyo.dsladapter.DslAdapter
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.dsladapter.ItemSelectorHelper
import com.angcyo.dsladapter.OnItemSelectorListener
import com.angcyo.dsladapter.filter.RemoveItemDecorationFilterAfterInterceptor
import com.angcyo.dsladapter.select
import com.angcyo.dsladapter.selectMutex
import com.angcyo.dsladapter.selector
import com.angcyo.dsladapter.toEmpty
import com.angcyo.dsladapter.updateNow
import com.angcyo.library.ex._color
import com.angcyo.library.ex._dimen
import com.angcyo.library.ex.have
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.recycler.initDslAdapter

/**
 * 提供RecyclerView快速配置相关类
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/30
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
open class RecyclerConfig {

    /**item项*/
    var adapterItemList: MutableList<DslAdapterItem> = mutableListOf()

    /**支持单选多选模式*/
    var adapterSelectorModel: Int = ItemSelectorHelper.MODEL_NORMAL

    var adapterItemSelectorListener: OnItemSelectorListener? = null

    /**事件回调*/
    var adapterItemClick: (dslItem: DslAdapterItem, view: View) -> Unit = { _, _ -> }

    /**移除收尾分割线*/
    var removeFirstLastItemDecoration: Boolean = true

    /**渲染回调*/
    var onRenderAction: ((recyclerView: RecyclerView, adapter: DslAdapter) -> Unit)? = null

    //适配器
    lateinit var _adapter: DslAdapter

    /**初始化视图*/
    open fun initRecycler(dialog: TargetWindow, holder: DslViewHolder) {
        holder.rv(R.id.lib_recycler_view)?.apply {
            //初始化DslAdapter
            _adapter = initDslAdapter() {
                if (removeFirstLastItemDecoration) {
                    dslDataFilter?.dataAfterInterceptorList?.add(
                        RemoveItemDecorationFilterAfterInterceptor()
                    )
                }
                adapterItemList.forEach {
                    if (it.itemData == null) {
                        //方便操作[Dialog]
                        it.itemData = dialog
                    }
                }
                resetItem(adapterItemList)

                if (onRenderAction == null) {
                    if (adapterItemList.isEmpty()) {
                        toEmpty()
                    }
                } else {
                    onRenderAction?.invoke(this@apply, this)
                }

                //托管itemClick
                wrapItemClick(dialog)

                updateNow()
            }
            //设置选择模式
            _adapter.selector().apply {
                selectorModel = adapterSelectorModel
                adapterItemSelectorListener?.apply { addObserver(this) }
            }
        }
    }

    /**自动销毁[window]*/
    fun DslAdapter.wrapItemClick(window: TargetWindow) {
        getDataList(false).forEach { item ->
            if (item.itemClick != null && item.itemFlag.have(MenuPopupConfig.FLAG_ITEM_DISMISS)) {
                val oldClick = item.itemClick
                item.itemClick = {
                    window.dismissWindow()
                    oldClick?.invoke(it)
                }
            }
        }
    }

    /**添加item
     * 点击后自动关闭pop
     * itemFlag = MenuPopupConfig.FLAG_ITEM_DISMISS
     * */
    open fun addItem(item: DslAdapterItem) {
        val oldItemClick = item.itemClick
        adapterItemList.add(item.apply {
            itemClick = {
                onItemClick(this, it)
                if (oldItemClick != itemClick) {
                    oldItemClick?.invoke(it)
                }
            }
        })
    }

    /**添加[DslDialogTextItem]*/
    open fun addDialogTextItem(action: DslDialogTextItem.() -> Unit) {
        addItem(DslDialogTextItem().apply {
            itemTopInsert = _dimen(R.dimen.lib_line_px)
            itemDecorationColor = _color(R.color.dialog_line)
            action()
        })
    }

    /**添加[DslDialogIconTextItem]*/
    open fun addDialogIconTextItem(action: DslDialogIconTextItem.() -> Unit) {
        addItem(DslDialogIconTextItem().apply {
            itemTopInsert = _dimen(R.dimen.lib_line_px)
            itemDecorationColor = _color(R.color.dialog_line)
            action()
        })
    }

    /**Item点击事件*/
    open fun onItemClick(dslItem: DslAdapterItem, view: View) {
        when (adapterSelectorModel) {
            ItemSelectorHelper.MODEL_SINGLE -> _adapter.select(dslItem)
            ItemSelectorHelper.MODEL_MULTI -> _adapter.selectMutex(dslItem)
            else -> adapterItemClick(dslItem, view)
        }
    }

    /**获取选中项列表*/
    fun getSelectorItemList(): List<DslAdapterItem> = _adapter.selector().getSelectorItemList()

    /**获取选中项的索引列表*/
    fun getSelectorIndexList(): List<Int> = _adapter.selector().getSelectorIndexList()
}