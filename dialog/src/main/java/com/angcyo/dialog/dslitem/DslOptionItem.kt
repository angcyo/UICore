package com.angcyo.dialog.dslitem

import com.angcyo.dialog.R
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.widget.DslViewHolder

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/12
 */
open class DslOptionItem : DslAdapterItem() {

    var itemOptionText: CharSequence? = null

    init {
        itemLayoutId = R.layout.item_option_layout
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)
        itemHolder.tv(R.id.lib_text_view)?.text = itemOptionText
        itemHolder.selected(R.id.lib_text_view, itemIsSelected)

        itemHolder.selected(R.id.lib_image_view, itemIsSelected)
        itemHolder.visible(R.id.lib_image_view, itemIsSelected)
    }
}