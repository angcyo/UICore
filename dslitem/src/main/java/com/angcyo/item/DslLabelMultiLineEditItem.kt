package com.angcyo.item

import android.text.InputType
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.pager.TextIndicator

/**
 * 带有label的多行输入item
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/25
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class DslLabelMultiLineEditItem : DslLabelEditItem() {
    init {
        itemLayoutId = R.layout.dsl_label_multi_line_edit_item

        itemEditTextStyle.editMaxInputLength = 200
        itemEditTextStyle.editMaxLine = 80

        /**多行输入时, 需要 [InputType.TYPE_TEXT_FLAG_MULTI_LINE] 否则输入框, 不能输入 回车 */
        itemEditTextStyle.editInputType =
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)

        itemHolder.visible(R.id.lib_text_indicator, itemEditTextStyle.noEditModel && itemEnable)

        //输入框
        itemHolder.ev(R.id.lib_edit_view)?.apply {
            if (itemEditTextStyle.editMaxInputLength > 0) {
                itemHolder.v<TextIndicator>(R.id.lib_text_indicator)
                    ?.setupEditText(this, itemEditTextStyle.editMaxInputLength)
            }
        }
    }
}