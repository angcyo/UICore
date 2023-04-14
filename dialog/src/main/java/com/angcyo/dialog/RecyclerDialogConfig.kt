package com.angcyo.dialog

import android.app.Dialog
import android.content.Context
import com.angcyo.dialog.dslitem.DslDialogGridItem
import com.angcyo.dialog.dslitem.DslDialogTextItem
import com.angcyo.dialog.popup.MenuPopupConfig
import com.angcyo.dsladapter.DslAdapter
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.dsladapter.ItemSelectorHelper
import com.angcyo.dsladapter.itemIndexPosition
import com.angcyo.library.ex._color
import com.angcyo.library.ex._string
import com.angcyo.library.ex.dpi
import com.angcyo.library.ex.have
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.addDslItem
import com.angcyo.widget.base.layoutDelegate

/**
 * 带取消item, 带单选多选的[androidx.recyclerview.widget.RecyclerView]对话框配置
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/05
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class RecyclerDialogConfig(context: Context? = null) : BaseDialogConfig(context) {

    companion object {

        /**点击item的时候, 自动关闭[TargetWindow]*/
        const val FLAG_ITEM_DISMISS = MenuPopupConfig.FLAG_ITEM_DISMISS
    }

    /**底部取消按钮, 动态添加*/
    var dialogBottomCancelItem: DslDialogTextItem? = defaultCancelItem()

    /**支持单选多选模式*/
    var dialogSelectorModel: Int = ItemSelectorHelper.MODEL_NORMAL

    /**最大的高度*/
    var dialogMaxHeight: String? = "0.5sh"

    //包裹取消按钮的布局id
    var _cancelItemWrapLayoutId = R.id.lib_dialog_root_layout

    val _recyclerConfig = RecyclerConfig()

    /**对话框返回, 取消不会触发此回调
     * [dialogItemList] 数据列表
     * [dialogItemIndexList] 索引列表
     * @return true 拦截默认处理*/
    var dialogResult: (dialog: Dialog, itemList: List<DslAdapterItem>, indexList: List<Int>) -> Boolean =
        { _, _, _ -> false }

    init {
        dialogLayoutId = R.layout.lib_dialog_recycler_layout

        positiveButtonListener = { dialog, _ ->
            var dismiss = true
            if (dialogSelectorModel == ItemSelectorHelper.MODEL_SINGLE ||
                dialogSelectorModel == ItemSelectorHelper.MODEL_MULTI
            ) {
                if (dialogResult(
                        dialog,
                        _recyclerConfig.getSelectorItemList(),
                        _recyclerConfig.getSelectorIndexList()
                    )
                ) {
                    //拦截
                    dismiss = false
                } else {
                    //清空回调对象
                    dialogResult = { _, _, _ -> false }
                }
            }
            if (dismiss) {
                dialog.dismiss()
            }
        }
    }

    override fun initDialogView(dialog: Dialog, dialogViewHolder: DslViewHolder) {
        super.initDialogView(dialog, dialogViewHolder)
        dialogViewHolder.group(R.id.lib_dialog_root_layout)?.layoutDelegate {
            rMaxHeight = dialogMaxHeight
        }

        if (dialogSelectorModel == ItemSelectorHelper.MODEL_SINGLE ||
            dialogSelectorModel == ItemSelectorHelper.MODEL_MULTI
        ) {
            //选择模式
            dialogViewHolder.enable(R.id.dialog_positive_button, false)
        } else {
            //隐藏多余的按钮
            dialogViewHolder.gone(R.id.dialog_positive_button)
            dialogViewHolder.gone(R.id.dialog_negative_button)
            dialogViewHolder.gone(R.id.dialog_neutral_button)
        }

        //recycler
        initRecyclerConfig(dialog, dialogViewHolder)

        //返回按钮
        dialogBottomCancelItem?.apply {
            itemClick = {
                dialog.cancel()
            }
            dialogViewHolder.group(_cancelItemWrapLayoutId)?.also {
                it.addDslItem(this)
            }
        }
    }

    /**[androidx.recyclerview.widget.RecyclerView]
     * 此方法调用之前, 可能需要先调用[renderAdapter]方法
     * */
    open fun initRecyclerConfig(dialog: Dialog, dialogViewHolder: DslViewHolder) {
        _recyclerConfig.adapterSelectorModel = dialogSelectorModel
        _recyclerConfig.initRecycler(dialog, dialogViewHolder)
        _recyclerConfig.adapterItemClick = { dslItem, view ->
            if (dialogResult(_dialog!!, listOf(dslItem), listOf(dslItem.itemIndexPosition()))
            ) {
                //拦截
            } else if (dslItem.itemFlag.have(FLAG_ITEM_DISMISS)) {
                _dialog?.dismiss()
            }
        }
    }

    //---

    /**默认的返回按钮[DslDialogTextItem]*/
    open fun defaultCancelItem(): DslDialogTextItem {
        return DslDialogTextItem().apply {
            itemFlag = FLAG_ITEM_DISMISS
            itemTopInsert = 4 * dpi
            itemDecorationColor = _color(R.color.dialog_cancel_line)
            itemTextBold = true
            itemText = _string(R.string.dialog_negative)
        }
    }

    /**隐藏取消的item*/
    fun hideCancelItem() {
        dialogBottomCancelItem = null
    }

    /**配置取消按钮*/
    fun configCancelItem(action: DslDialogTextItem.() -> DslDialogTextItem?) {
        dialogBottomCancelItem = (dialogBottomCancelItem ?: defaultCancelItem()).run {
            action()
        }
    }

    //---

    /**直接渲染界面, 此方法需要在[com.angcyo.dialog.RecyclerConfig.initRecycler]之前调用*/
    fun renderAdapter(action: DslAdapter.() -> Unit) {
        _recyclerConfig.onRenderAction = { recyclerView, adapter ->
            adapter._recyclerView = recyclerView
            adapter.action()
        }
    }

    /**调用此方法, 添加[DslAdapterItem]
     * [DslAdapterItem]
     * [com.angcyo.dialog.RecyclerConfig.addItem]*/
    open fun addItem(item: DslAdapterItem) {
        _recyclerConfig.addItem(item)
    }

    /**添加[DslDialogTextItem]
     * ```
     * addDialogItem {
     *    itemText = tx()
     *    itemLeftDrawable = _drawable(R.drawable.lib_ic_error)
     *    itemClick = {
     *    }
     * }
     * ```
     * */
    open fun addDialogItem(action: DslDialogTextItem.() -> Unit) {
        _recyclerConfig.addDialogTextItem {
            itemFlag = FLAG_ITEM_DISMISS
            action()
        }
    }

    /**添加[DslDialogGridItem]*/
    open fun addGridItem(action: DslDialogGridItem.() -> Unit) {
        _recyclerConfig.addItem(DslDialogGridItem().apply {
            itemFlag = FLAG_ITEM_DISMISS
            action()
        })
    }
}