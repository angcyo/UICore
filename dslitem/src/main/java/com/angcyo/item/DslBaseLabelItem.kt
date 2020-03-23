package com.angcyo.item

import android.util.TypedValue
import android.view.Gravity
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.library.ex.undefined_color
import com.angcyo.library.ex.undefined_float
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.setBoldText

/**
 * 带有Label的item
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/23
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

open class DslBaseLabelItem : DslAdapterItem() {

    /**左边的Label文本*/
    var itemLabelText: CharSequence? = null
    var itemLabelBold: Boolean = false
    var itemLabelTextColor: Int = undefined_color
    var itemLabelTextSize: Float = undefined_float

    var itemLabelGravity: Int = Gravity.LEFT or Gravity.CENTER_VERTICAL

    init {
        itemLayoutId = R.layout.dsl_label_item
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)

        itemHolder.gone(R.id.lib_label_view, itemLabelText.isNullOrBlank())

        itemHolder.tv(R.id.lib_label_view)?.apply {
            text = itemLabelText
            gravity = itemLabelGravity

            setBoldText(itemLabelBold)
            if (itemLabelTextColor != undefined_color) {
                setTextColor(itemLabelTextColor)
            }

            if (itemLabelTextSize != undefined_float) {
                setTextSize(TypedValue.COMPLEX_UNIT_PX, itemLabelTextSize)
            }
        }
    }
}