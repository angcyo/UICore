package com.angcyo.item

import android.view.Gravity
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.item.style.ButtonStyleConfig
import com.angcyo.widget.DslButton
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.button

/**
 * 带有[DslButton]的item
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/26
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class DslButtonItem : DslAdapterItem() {

    /**按钮显示的文本*/
    var itemButtonText: CharSequence? = null
        set(value) {
            field = value
            itemButtonStyle.text = value
        }

    /**按钮样式配置项*/
    var itemButtonStyle = ButtonStyleConfig().apply {
        textGravity = Gravity.CENTER
    }

    /**按钮配置回调*/
    var itemButtonConfig: (DslButton) -> Unit = {

    }

    init {
        itemLayoutId = R.layout.dsl_button_item
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)
        itemHolder.itemView.isClickable = false

        itemHolder.button(R.id.lib_button)?.apply {
            itemButtonStyle.updateStyle(this)
            itemButtonConfig(this)

            setOnClickListener(_clickListener)
            setOnLongClickListener(_longClickListener)
        }
    }

    open fun configButtonStyle(action: ButtonStyleConfig.() -> Unit) {
        itemButtonStyle.action()
    }
}