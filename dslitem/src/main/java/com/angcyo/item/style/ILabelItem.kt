package com.angcyo.item.style

import com.angcyo.dsladapter.item.IDslItemConfig
import com.angcyo.item.R
import com.angcyo.widget.DslViewHolder

/**
 * 带Label的item
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/06/28
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
interface ILabelItem : IAutoInitItem {

    /**统一样式配置*/
    var labelItemConfig: LabelItemConfig

    /**初始化*/
    fun initLabelItem(itemHolder: DslViewHolder) {
        itemHolder.gone(
            labelItemConfig.itemLabelViewId,
            labelItemConfig.itemLabelTextStyle.text == null
        )
        itemHolder.tv(labelItemConfig.itemLabelViewId)?.apply {
            labelItemConfig.itemLabelTextStyle.updateStyle(this)
        }
    }

    fun configLabelTextStyle(action: TextStyleConfig.() -> Unit) {
        labelItemConfig.itemLabelTextStyle.action()
    }
}

var ILabelItem.itemLabelText: CharSequence?
    get() = labelItemConfig.itemLabelText
    set(value) {
        labelItemConfig.itemLabelText = value
    }

class LabelItemConfig : IDslItemConfig {

    /**[R.id.lib_label_view]*/
    var itemLabelViewId: Int = R.id.lib_label_view

    /**Label文本*/
    var itemLabelText: CharSequence? = null
        set(value) {
            field = value
            itemLabelTextStyle.text = value
        }

    /**统一样式配置*/
    var itemLabelTextStyle: TextStyleConfig = TextStyleConfig()
}