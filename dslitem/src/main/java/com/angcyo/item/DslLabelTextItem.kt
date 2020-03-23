package com.angcyo.item

import android.util.TypedValue
import android.view.Gravity
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.library.ex.undefined_color
import com.angcyo.library.ex.undefined_float
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.setBoldText

/**
 * 简单的文本显示item
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/23
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class DslLabelTextItem : DslBaseLabelItem() {

    var itemText: CharSequence? = null
    var itemBold: Boolean = true
    var itemTextColor: Int = undefined_color
    var itemTextSize: Float = undefined_float
    var itemTextGravity: Int = Gravity.LEFT or Gravity.CENTER_VERTICAL

    init {
        itemLayoutId = R.layout.dsl_label_text_item
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)

        itemHolder.tv(R.id.lib_text_view)?.apply {
            text = itemText
            setBoldText(itemBold)
            gravity = itemTextGravity
            if (itemTextColor != undefined_color) {
                setTextColor(itemTextColor)
            }

            if (itemTextSize != undefined_float) {
                setTextSize(TypedValue.COMPLEX_UNIT_PX, itemTextSize)
            }
        }
    }
}