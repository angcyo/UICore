package com.angcyo.item

import android.util.TypedValue
import android.view.Gravity
import android.widget.TextView
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
        set(value) {
            field = value
            itemLabelTextStyle.text = value
        }

    /**统一样式配置*/
    var itemLabelTextStyle = TextStyleConfig()

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
            itemLabelTextStyle.updateStyle(this)
        }
    }

    open fun configLabelTextStyle(action: TextStyleConfig.() -> Unit) {
        itemLabelTextStyle.action()
    }
}

data class TextStyleConfig(
    var text: CharSequence? = null,
    var textBold: Boolean = false,
    var textColor: Int = undefined_color,
    var textSize: Float = undefined_float,
    var textGravity: Int = Gravity.LEFT or Gravity.CENTER_VERTICAL
)

fun TextStyleConfig.updateStyle(textView: TextView) {
    with(textView) {
        text = this@updateStyle.text

        gravity = textGravity

        setBoldText(textBold)

        if (textColor != undefined_color) {
            setTextColor(textColor)
        }

        if (textSize != undefined_float) {
            setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
        }
    }
}
