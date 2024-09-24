package com.angcyo.item

import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.dsladapter.updateItemSelected
import com.angcyo.item.style.DesItemConfig
import com.angcyo.item.style.IDesItem
import com.angcyo.item.style.ITextItem
import com.angcyo.item.style.TextItemConfig
import com.angcyo.library.ex._dimen
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.setBoldText

/**
 * 简单的文本显示item
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/04/23
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
open class DslTextItem : DslAdapterItem(), ITextItem, IDesItem {

    override var textItemConfig = TextItemConfig()

    override var desItemConfig = DesItemConfig()

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
    }
}

/**简单文本选中提示item*/
open class DslSingleSelectedTextItem : DslAdapterItem() {

    /**文本*/
    var itemText: CharSequence? = null

    /**右边的图标*/
    var itemRightIcon: Int? = null

    /**是否支持取消选中*/
    var itemSupportCancelSelected = false

    init {
        itemLayoutId = R.layout.dsl_single_selected_text_item
        itemSingleSelectMutex = true
        itemFirstPaddingTop = _dimen(R.dimen.lib_xhdpi)

        itemClick = {
            if (itemIsSelected) {
                if (itemSupportCancelSelected) {
                    updateItemSelected(false)
                    itemChanging = true
                    //
                    onSelfItemSelectChanged()
                }
            } else {
                updateItemSelected(true)
                itemChanging = true
                //
                onSelfItemSelectChanged()
            }
        }
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
            setBoldText(itemIsSelected)
        }

        itemHolder.img(R.id.lib_right_ico_view)?.apply {
            setImageResource(itemRightIcon ?: 0)
        }
    }

    /**选中状态改变时触发*/
    open fun onSelfItemSelectChanged() {}

}