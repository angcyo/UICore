package com.angcyo.item

import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.item.style.*
import com.angcyo.widget.DslViewHolder

/**
 * 带有输入框的item
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/06/28
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
open class DslInputItem : DslAdapterItem(), ILabelItem, IEditItem {

    override var labelItemConfig: LabelItemConfig = LabelItemConfig()

    override var editItemConfig: EditItemConfig = EditItemConfig()

    //</editor-fold desc="edit">

    init {
        itemLayoutId = R.layout.dsl_input_item

        labelItemConfig.itemLabelText = "请输入"
        labelItemConfig.itemLabelTextStyle.textBold = true

        editItemConfig.itemEditTextStyle.hint = "请输入..."
        editItemConfig.itemEditTextStyle.editMaxInputLength = -1
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem)
        initLabelItem(itemHolder)
        initEditItem(itemHolder)
    }

    override fun onItemViewRecycled(itemHolder: DslViewHolder, itemPosition: Int) {
        super.onItemViewRecycled(itemHolder, itemPosition)
        clearEditListeners(itemHolder)
    }
}