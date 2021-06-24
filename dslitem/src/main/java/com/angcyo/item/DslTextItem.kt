package com.angcyo.item

import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.item.style.TextStyleConfig
import com.angcyo.library.ex._dimen
import com.angcyo.widget.DslViewHolder

/**
 * 简单的文本显示item
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/04/23
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
open class DslTextItem : DslAdapterItem() {

    /**文本*/
    var itemText: CharSequence? = null
        set(value) {
            field = value
            itemTextStyle.text = value
        }

    /**统一样式配置*/
    var itemTextStyle = TextStyleConfig()

    /**文本*/
    var itemDes: CharSequence? = null
        set(value) {
            field = value
            itemDesStyle.text = value
        }

    /**统一样式配置*/
    var itemDesStyle = TextStyleConfig()

    init {
        itemLayoutId = R.layout.dsl_text_item
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)

        itemHolder.tv(R.id.lib_text_view)?.apply {
            itemTextStyle.updateStyle(this)
        }

        itemHolder.gone(R.id.lib_des_view, itemDes == null)
        itemHolder.tv(R.id.lib_des_view)?.apply {
            itemDesStyle.updateStyle(this)
        }
    }

    open fun configTextStyle(action: TextStyleConfig.() -> Unit) {
        itemTextStyle.action()
    }

    open fun configDesStyle(action: TextStyleConfig.() -> Unit) {
        itemDesStyle.action()
    }
}

/**加粗样式*/
fun DslTextItem.boldStyle() {
    configTextStyle {
        textBold = true
        textSize = _dimen(R.dimen.text_sub_size).toFloat()
    }
}