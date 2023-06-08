package com.angcyo.web.core

import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.library.ex._drawable
import com.angcyo.web.R
import com.angcyo.widget.DslViewHolder

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/04/22
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
open class DslBaseWebMenuItem : DslAdapterItem() {

    var menuText: CharSequence? = null
    var menuIcon: Int = -1

    init {
        itemLayoutId = R.layout.web_menu_item
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)

        //文本
        itemHolder.tv(R.id.lib_text_view)?.apply {
            text = menuText
        }

        //图标
        itemHolder.img(R.id.lib_image_view)?.apply {
            setImageDrawable(_drawable(menuIcon))
        }
    }
}