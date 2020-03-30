package com.angcyo.dialog

import android.app.Dialog
import android.content.Context
import com.angcyo.dialog.dslitem.DslDialogTextItem
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.dsladapter.ItemSelectorHelper
import com.angcyo.dsladapter.itemIndexPosition
import com.angcyo.library.ex._color
import com.angcyo.library.ex.dpi
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.addDslItem
import com.angcyo.widget.base.layoutDelegate

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/05
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class RecyclerDialogConfig(context: Context? = null) : BaseDialogConfig(context) {

    /**底部取消按钮, 动态添加*/
    var dialogBottomCancelItem: DslDialogTextItem? = defaultCancelItem()

    /**支持单选多选模式*/
    var dialogSelectorModel: Int = ItemSelectorHelper.MODEL_NORMAL

    /**最大的高度*/
    var dialogMaxHeight: String = "0.5sh"

    //包裹取消按钮的布局id
    var _cancelItemWrapLayoutId = R.id.lib_dialog_root_layout

    val _recyclerConfig = RecyclerConfig()

    /**对话框返回, 取消不会触发此回调
     * [dialogItemList] 数据列表
     * [dialogItemIndexList] 索引列表*/
    var onDialogResult: (dialog: Dialog, itemList: List<DslAdapterItem>, indexList: List<Int>) -> Boolean =
        { _, _, _ -> false }

    init {
        dialogLayoutId = R.layout.lib_dialog_recycler_layout

        positiveButtonListener = { dialog, _ ->
            var dismiss = true
            if (dialogSelectorModel == ItemSelectorHelper.MODEL_SINGLE ||
                dialogSelectorModel == ItemSelectorHelper.MODEL_MULTI
            ) {
                if (onDialogResult(
                        dialog,
                        _recyclerConfig.getSelectorItemList(),
                        _recyclerConfig.getSelectorIndexList()
                    )
                ) {
                    //拦截
                    dismiss = false
                } else {
                    //清空回调对象
                    onDialogResult = { _, _, _ -> false }
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
            dialogViewHolder.enable(R.id.positive_button, false)
        } else {
            //隐藏多余的按钮
            dialogViewHolder.gone(R.id.positive_button)
            dialogViewHolder.gone(R.id.negative_button)
            dialogViewHolder.gone(R.id.dialog_neutral_button)
        }

        _recyclerConfig.adapterSelectorModel = dialogSelectorModel
        _recyclerConfig.initRecycler(dialogViewHolder)
        _recyclerConfig.adapterItemClick = { dslItem, view ->
            if (onDialogResult(_dialog!!, listOf(dslItem), listOf(dslItem.itemIndexPosition()))
            ) {
                //拦截
            } else {
                _dialog?.dismiss()
            }
        }

        dialogBottomCancelItem?.apply {
            itemClick = {
                dialog.cancel()
            }
            dialogViewHolder.group(_cancelItemWrapLayoutId)?.also {
                it.addDslItem(this)
            }
        }
    }

    //默认的返回按钮
    open fun defaultCancelItem(): DslDialogTextItem {
        return DslDialogTextItem().apply {
            itemTopInsert = 4 * dpi
            itemDecorationColor = _color(R.color.dialog_cancel_line)
            itemTextBold = true
            itemText = "取消"
        }
    }

    /**配置取消按钮*/
    fun configCancelItem(action: DslDialogTextItem.() -> DslDialogTextItem?) {
        dialogBottomCancelItem = (dialogBottomCancelItem ?: defaultCancelItem()).run {
            action()
        }
    }
}