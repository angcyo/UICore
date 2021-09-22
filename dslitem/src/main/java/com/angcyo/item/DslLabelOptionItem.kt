package com.angcyo.item

import android.app.Dialog
import android.content.Context
import android.view.View
import com.angcyo.dialog.OptionDialogConfig
import com.angcyo.dialog.optionDialog
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.library.ex.string
import com.angcyo.widget.DslViewHolder

/**
 * 万级联动输入item
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/23
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class DslLabelOptionItem : DslLabelTextItem() {

    /**默认选中项*/
    var itemOptionList = mutableListOf<Any>()

    var itemOptionListToText: (List<Any>) -> CharSequence? = {
        buildString {
            it.forEachIndexed { index, any ->
                append(itemOptionItemToText(any))
                if (index != it.lastIndex) {
                    append("/")
                }
            }
        }
    }

    /**
     * 选项返回回调
     * 返回 true, 则不会自动 调用 dismiss
     * */
    var itemOptionResult: (dialog: Dialog, options: MutableList<Any>) -> Boolean = { _, _ ->
        false
    }

    /**将选项[item], 转成可以显示在界面的 文本类型*/
    var itemOptionItemToText: (item: Any) -> CharSequence = { item ->
        item.string()
    }

    var itemConfigDialog: (OptionDialogConfig) -> Unit = {

    }

    /**点击item之前拦截处理, 返回true拦截默认处理*/
    var itemClickBefore: (clickView: View) -> Boolean = { false }

    init {
        itemLayoutId = R.layout.dsl_label_option_item

        itemClick = {
            if (itemEnable && !itemClickBefore(it)) {
                showOptionDialog(it.context)
            }
        }
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        itemText = itemOptionListToText(itemOptionList)
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)
        itemHolder.visible(R.id.lib_right_ico_view, itemEnable)
    }

    /**显示dialog*/
    open fun showOptionDialog(context: Context) {
        context.optionDialog {
            dialogTitle = labelItemConfig.itemLabelText

            optionList.addAll(itemOptionList)

            onOptionItemToString = itemOptionItemToText

            onOptionResult = { dialog, options ->
                if (itemOptionResult(dialog, options)) {
                    //拦截了
                    true
                } else {
                    itemOptionList.clear()
                    itemOptionList.addAll(options)
                    itemChanging = true
                    false
                }
            }

            itemConfigDialog(this)
        }
    }
}