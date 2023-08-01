package com.angcyo.item.style

import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.dsladapter.annotation.ItemInitEntryPoint
import com.angcyo.dsladapter.item.IDslItemConfig
import com.angcyo.item.R
import com.angcyo.library.ex._dimen
import com.angcyo.library.ex.undefined_float
import com.angcyo.widget.DslViewHolder

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/09/18
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
interface ITextItem : IAutoInitItem {

    /**配置类 */
    var textItemConfig: TextItemConfig

    /**初始化*/
    @ItemInitEntryPoint
    fun initTextItem(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        itemHolder.tv(textItemConfig.itemTextViewId)?.apply {
            textItemConfig.itemTextStyle.updateStyle(this)
        }
    }

    fun configTextStyle(action: TextStyleConfig.() -> Unit) {
        textItemConfig.itemTextStyle.action()
    }

    /**加粗样式*/
    fun boldStyle() {
        configTextStyle {
            textBold = true
            if (textSize == undefined_float) {
                textSize = _dimen(R.dimen.text_sub_size).toFloat()
            }
        }
    }
}

/**item的文本, 内容*/
var ITextItem.itemText: CharSequence?
    get() = textItemConfig.itemText
    set(value) {
        textItemConfig.itemText = value
    }

/**内容的颜色*/
var ITextItem.itemTextColor: Int
    get() = textItemConfig.itemTextStyle.textColor
    set(value) {
        textItemConfig.itemTextStyle.textColor = value
    }


/**hint内容*/
var ITextItem.itemHint: CharSequence?
    get() = itemTextHint
    set(value) {
        itemTextHint = value
    }

var ITextItem.itemTextHint: CharSequence?
    get() = textItemConfig.itemTextStyle.hint
    set(value) {
        textItemConfig.itemTextStyle.hint = value
    }

class TextItemConfig : IDslItemConfig {

    /**[R.id.lib_text_view]*/
    var itemTextViewId: Int = R.id.lib_text_view

    /**条目文本*/
    var itemText: CharSequence? = null
        set(value) {
            field = value
            itemTextStyle.text = value
        }

    /**统一样式配置*/
    var itemTextStyle: TextStyleConfig = TextStyleConfig()

}