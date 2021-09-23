package com.angcyo.item

import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.item.style.ITextItem
import com.angcyo.item.style.TextItemConfig
import com.angcyo.library.ex.isDebug
import com.angcyo.library.ex.isDebugType
import com.angcyo.library.getAppVersionName
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.span.span

/**
 * 显示在RV最下面的item
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/09/18
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class DslLastTextItem : DslAdapterItem(), ITextItem {

    override var textItemConfig: TextItemConfig = TextItemConfig()

    init {
        itemLayoutId = R.layout.lib_item_last_text

        textItemConfig.itemText = span {
            append("当前版本:${getAppVersionName()}")
            if (isDebugType()) {
                append("-dev")
            } else if (isDebug()) {
                append("-debug")
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

        //初始化
        initTextItem(itemHolder)
    }

}