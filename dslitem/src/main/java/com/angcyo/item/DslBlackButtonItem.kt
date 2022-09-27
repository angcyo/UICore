package com.angcyo.item

import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.widget.DslViewHolder

/**
 * 黑色按钮/带ripple
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/09/24
 */
open class DslBlackButtonItem : BaseButtonItem() {

    var itemButtonText: CharSequence? = null

    init {
        itemLayoutId = R.layout.dsl_black_button_item
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)

        itemHolder.tv(R.id.lib_button)?.apply {
            text = itemButtonText
        }
    }
}