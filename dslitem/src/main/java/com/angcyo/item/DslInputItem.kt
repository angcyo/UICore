package com.angcyo.item

import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.item.style.EditStyleConfig
import com.angcyo.item.style.IEditItem
import com.angcyo.item.style.ILabelItem
import com.angcyo.item.style.TextStyleConfig
import com.angcyo.widget.DslViewHolder

/**
 * 带有输入框的item
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/06/28
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
open class DslInputItem : DslAdapterItem(), ILabelItem, IEditItem {

    //<editor-fold desc="label">

    override var itemLabelText: CharSequence? = null
        set(value) {
            field = value
            itemLabelTextStyle.text = value
        }

    override var itemLabelTextStyle: TextStyleConfig = TextStyleConfig()

    //</editor-fold desc="label">

    //<editor-fold desc="edit">

    override var itemEditText: CharSequence? = null
        set(value) {
            field = value
            itemEditTextStyle.text = value
        }

    override var itemEditTextStyle: EditStyleConfig = EditStyleConfig()

    override var itemTextChange: (CharSequence) -> Unit = {
        onItemTextChange(it)
    }

    override var itemTextChangeShakeDelay: Long = DslBaseEditItem.DEFAULT_INPUT_SHAKE_DELAY

    override var _lastEditSelectionStart: Int = -1

    override var _lastEditSelectionEnd: Int = -1

    //</editor-fold desc="edit">

    init {
        itemLayoutId = R.layout.dsl_input_item

        itemLabelText = "请输入"
        itemLabelTextStyle.textBold = true

        itemEditTextStyle.hint = "请输入..."
        itemEditTextStyle.editMaxInputLength = -1
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