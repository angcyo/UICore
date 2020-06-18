package com.angcyo.item

import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.item.style.TextStyleConfig
import com.angcyo.widget.DslViewHolder

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
        adapterItem: DslAdapterItem
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem)

        itemHolder.gone(R.id.lib_label_view, itemLabelTextStyle.text == null)
        itemHolder.tv(R.id.lib_label_view)?.apply {
            itemLabelTextStyle.updateStyle(this)
        }
    }

    open fun configLabelTextStyle(action: TextStyleConfig.() -> Unit) {
        itemLabelTextStyle.action()
    }
}


