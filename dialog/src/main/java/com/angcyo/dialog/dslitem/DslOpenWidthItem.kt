package com.angcyo.dialog.dslitem

import android.graphics.drawable.Drawable
import com.angcyo.dialog.R
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.library.ex.undefined_res
import com.angcyo.widget.DslViewHolder

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/06/28
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class DslOpenWidthItem : DslAdapterItem() {

    /**图标*/
    var itemIcon: Int = undefined_res
    var itemDrawable: Drawable? = null

    /**文本*/
    var itemText: CharSequence? = null

    init {
        itemLayoutId = R.layout.item_open_width
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
        }

        itemHolder.img(R.id.lib_image_view)?.run {
            setOnClickListener(_clickListener)
            if (itemIcon != undefined_res) {
                setImageResource(itemIcon)
            } else {
                setImageDrawable(itemDrawable)
            }
        }
    }
}