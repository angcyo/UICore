package com.angcyo.item

import android.app.Dialog
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
class DslLabelOptionItem : DslBaseLabelItem() {

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

    init {
        itemLayoutId = R.layout.dsl_label_option_item

        itemClick = {
            it.context.optionDialog {
                dialogTitle = itemLabelText

                optionList = itemOptionList

                onOptionItemToString = itemOptionItemToText

                onOptionResult = { dialog, options ->
                    if (itemOptionResult(dialog, options)) {
                        //拦截了
                        true
                    } else {
                        itemChanging = true
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
        itemHolder.tv(R.id.lib_text_view)?.text = itemOptionListToText(itemOptionList)
    }
}