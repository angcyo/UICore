package com.angcyo.item.style

import com.angcyo.dsladapter.item.IDslItem
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
interface ILabelItem : IDslItem {

    /**统一样式配置*/
    var labelItemConfig: LabelItemConfig
}

class LabelItemConfig : IDslItemConfig {
    /**[R.id.lib_label_view]*/
    var itemLabelTextViewId: Int = R.id.lib_label_view

    /**Label文本*/
    var itemLabelText: CharSequence? = null
        set(value) {
            field = value
            itemLabelTextStyle.text = value
        }

    /**统一样式配置*/
    var itemLabelTextStyle: TextStyleConfig = TextStyleConfig()
}

/**初始化*/
fun ILabelItem.initLabelItem(itemHolder: DslViewHolder) {
    itemHolder.gone(
        labelItemConfig.itemLabelTextViewId,
        labelItemConfig.itemLabelTextStyle.text == null
    )
    itemHolder.tv(labelItemConfig.itemLabelTextViewId)?.apply {
        labelItemConfig.itemLabelTextStyle.updateStyle(this)
    }
}

fun ILabelItem.configLabelTextStyle(action: TextStyleConfig.() -> Unit) {
    labelItemConfig.itemLabelTextStyle.action()
}