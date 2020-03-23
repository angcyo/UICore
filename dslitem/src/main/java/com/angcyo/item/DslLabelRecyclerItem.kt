package com.angcyo.item

import android.app.Dialog
import com.angcyo.dialog.ItemDialogConfig
import com.angcyo.dialog.dslitem.DslDialogTextItem
import com.angcyo.dialog.itemsDialog
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.dsladapter.ItemSelectorHelper
import com.angcyo.library.ex.string
import com.angcyo.widget.DslViewHolder

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/23
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
class DslLabelRecyclerItem : DslBaseLabelItem() {

    /**选项*/
    val itemRecyclerList: MutableList<DslAdapterItem> = mutableListOf()

    /**选中的选项*/
    val itemSelectorList: MutableList<DslAdapterItem> = mutableListOf()

    /**对话框返回, 取消不会触发此回调
     * [dialogItemList] 数据列表
     * [dialogItemIndexList] 索引列表*/
    var itemRecyclerResult: (dialog: Dialog, itemList: List<DslAdapterItem>, indexList: List<Int>) -> Boolean =
        { _, _, _ -> false }

    /**将选项[item], 转成可以显示在界面的 文本类型*/
    var itemOptionItemToText: (item: DslAdapterItem) -> CharSequence = { item ->
        (if (item is DslDialogTextItem) {
            item.itemText
        } else {
            item.itemTag
        }) ?: item.string()
    }

    /**判断两个[DslAdapterItem]是否相等*/
    var itemRecyclerEqual: (from: DslAdapterItem, to: DslAdapterItem) -> Boolean = { from, to ->
        from == to
    }

    var itemRecyclerListToText: (List<DslAdapterItem>) -> CharSequence? = {
        buildString {
            it.forEachIndexed { index, any ->
                append(itemOptionItemToText(any))
                if (index != it.lastIndex) {
                    append("/")
                }
            }
        }
    }

    var itemConfigDialog: (ItemDialogConfig) -> Unit = {

    }

    init {
        itemLayoutId = R.layout.dsl_label_recycler_item

        itemClick = {
            it.context.itemsDialog {
                dialogTitle = itemLabelText

                dialogBottomCancelItem = null

                adapterItemList.addAll(itemRecyclerList)

                onDialogResult = { dialog, itemList, indexList ->
                    if (itemRecyclerResult(dialog, itemList, indexList)) {
                        //拦截了
                        true
                    } else {
                        val from = itemSelectorList.firstOrNull()
                        val to = itemList.firstOrNull()

                        itemSelectorList.clear()
                        itemSelectorList.addAll(itemList)
                        if (dialogSelectorModel == ItemSelectorHelper.MODEL_NORMAL) {
                            if (from != null && to != null && !itemRecyclerEqual(from, to)) {
                                itemChanging = true
                            } else if (from == null) {
                                itemChanging = true
                            }
                        } else {
                            itemChanging = true
                        }
                        false
                    }
                }

                itemConfigDialog(this)
            }
        }
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)
        itemHolder.tv(R.id.lib_text_view)?.text = itemRecyclerListToText(itemSelectorList)
    }
}