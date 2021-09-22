package com.angcyo.item.style

import com.angcyo.dsladapter.item.IDslItem
import com.angcyo.dsladapter.item.IDslItemConfig
import com.angcyo.item.R
import com.angcyo.widget.DslViewHolder

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/09/18
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
interface ITextItem : IDslItem {

    /**配置类
     * */
    var textItemConfig: TextItemConfig
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

/**初始化*/
fun ITextItem.initTextItem(itemHolder: DslViewHolder) {
    //itemHolder.gone(itemTextViewId, itemTextStyle.text == null)
    itemHolder.tv(textItemConfig.itemTextViewId)?.apply {
        textItemConfig.itemTextStyle.updateStyle(this)
    }
}

fun ITextItem.configTextStyle(action: TextStyleConfig.() -> Unit) {
    textItemConfig.itemTextStyle.action()
}