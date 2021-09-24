package com.angcyo.item.style

import android.view.Gravity
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.dsladapter.item.IDslItem
import com.angcyo.dsladapter.item.IDslItemConfig
import com.angcyo.item.R
import com.angcyo.widget.DslButton
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.button

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/09/24
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
interface IButtonItem : IDslItem {

    var buttonItemConfig: ButtonItemConfig

    fun initButtonItem(itemHolder: DslViewHolder) {
        itemHolder.itemView.isClickable = false

        itemHolder.button(buttonItemConfig.itemButtonViewId)?.apply {
            buttonItemConfig.itemButtonStyle.updateStyle(this)
            buttonItemConfig.itemButtonConfig(this)

            if (this is DslAdapterItem) {
                setOnClickListener(_clickListener)
                setOnLongClickListener(_longClickListener)
            }
        }
    }
}

var IButtonItem.itemButtonText: CharSequence?
    get() = buttonItemConfig.itemButtonText
    set(value) {
        buttonItemConfig.itemButtonText = value
    }

class ButtonItemConfig : IDslItemConfig {

    var itemButtonViewId: Int = R.id.lib_button

    /**按钮显示的文本*/
    var itemButtonText: CharSequence? = null
        set(value) {
            field = value
            itemButtonStyle.text = value
        }

    /**按钮样式配置项*/
    var itemButtonStyle = ButtonStyleConfig().apply {
        textGravity = Gravity.CENTER
        themeStyle()
    }

    /**按钮配置回调*/
    var itemButtonConfig: (DslButton) -> Unit = {

    }
}