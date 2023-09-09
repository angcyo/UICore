package com.angcyo.item

import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.dsladapter.itemViewHolder
import com.angcyo.item.style.EditItemConfig
import com.angcyo.item.style.IEditItem
import com.angcyo.item.style.ILabelItem
import com.angcyo.item.style.LabelItemConfig
import com.angcyo.library.ex._string
import com.angcyo.widget.DslViewHolder

/**
 * 带有输入框的item, 默认是上下布局结构
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

        labelItemConfig.itemLabelText = _string(R.string.dialog_input_hint)
        labelItemConfig.itemLabelTextStyle.textBold = true

        editItemConfig.itemEditTextStyle.hint = _string(R.string.dialog_input_hint)
        editItemConfig.itemEditTextStyle.editMaxInputLength = -1
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem)
    }

    override fun onItemViewRecycled(itemHolder: DslViewHolder, itemPosition: Int) {
        super.onItemViewRecycled(itemHolder, itemPosition)
        clearEditListeners(itemHolder)
    }

    override fun onItemChangeListener(item: DslAdapterItem) {
        hookEditItemFocus(itemViewHolder())
        super.onItemChangeListener(item)
    }
}