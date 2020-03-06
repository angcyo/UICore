package com.angcyo.dialog

import android.app.Dialog
import android.content.Context
import android.view.View
import com.angcyo.dialog.dslitem.DslDialogTextItem
import com.angcyo.dsladapter.*
import com.angcyo.library.ex._color
import com.angcyo.library.ex.dpi
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.addDslItem
import com.angcyo.widget.base.layoutDelegate
import com.angcyo.widget.recycler.initDslAdapter

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/05
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
abstract class BaseRecyclerDialogConfig(context: Context? = null) : BaseDialogConfig(context) {

    /**底部取消按钮, 动态添加*/
    var dialogBottomCancelItem: DslDialogTextItem? = defaultCancelItem()

    /**选项*/
    val adapterItemList: MutableList<DslAdapterItem> = mutableListOf()

    /**支持单选多选模式*/
    var dialogSelectorModel: Int = ItemSelectorHelper.MODEL_NORMAL

    /**最大的高度*/
    var dialogMaxHeight: String = "0.5sh"

    //包裹取消按钮的布局id
    var _cancelItemWrapLayoutId = R.id.lib_dialog_root_layout

    /**对话框返回, 取消不会触发此回调
     * [dialogItemList] 数据列表
     * [dialogItemIndexList] 索引列表*/
    var onDialogResult: (itemList: List<DslAdapterItem>, indexList: List<Int>) -> Unit =
        { _, _ -> }

    //适配器
    lateinit var _dialogAdapter: DslAdapter

    init {
        dialogLayoutId = R.layout.lib_dialog_recycler_layout

        positiveButtonListener = { dialog, _ ->
            dialog.dismiss()
            if (dialogSelectorModel == ItemSelectorHelper.MODEL_SINGLE ||
                dialogSelectorModel == ItemSelectorHelper.MODEL_MULTI
            ) {
                onDialogResult(
                    _dialogAdapter.selector().getSelectorItemList(),
                    _dialogAdapter.selector().getSelectorIndexList()
                )
                //清空回调对象
                onDialogResult = { _, _ -> }
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

        dialogViewHolder.rv(R.id.lib_recycler_view)?.apply {
            //初始化DslAdapter
            _dialogAdapter = initDslAdapter() {
                adapterItemList.firstOrNull()?.apply {
                    itemTopInsert = 0
                }
                adapterItemList.lastOrNull()?.apply {
                    itemBottomInsert = 0
                }
                resetItem(adapterItemList)
                updateItemDepend(FilterParams(justRun = true, asyncDiff = false))
            }
            //设置选择模式
            _dialogAdapter.selector().apply {
                selectorModel = dialogSelectorModel
                observer {
                    onItemChange =
                        { _, selectorIndexList, _, _ ->
                            dialogViewHolder.enable(
                                R.id.positive_button,
                                selectorIndexList.isNotEmpty()
                            )
                        }
                }
            }
        }

        dialogBottomCancelItem?.apply {
            onItemClick = {
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

    /**Item点击事件*/
    open fun onDialogItemClick(dslItem: DslAdapterItem, view: View) {
        if (dialogSelectorModel == ItemSelectorHelper.MODEL_SINGLE) {
            _dialogAdapter.select(dslItem)
        } else if (dialogSelectorModel == ItemSelectorHelper.MODEL_MULTI) {
            _dialogAdapter.selectMutex(dslItem)
        } else {
            _dialog?.dismiss()
            onDialogResult(listOf(dslItem), listOf(dslItem.itemIndexPosition()))
        }
    }
}