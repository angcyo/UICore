package com.angcyo.item

import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.button

/**
 * 单一按钮item
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/09/27
 */
abstract class BaseButtonItem : DslAdapterItem() {

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)

        itemHolder.itemView.isClickable = false

        itemHolder.button(R.id.lib_button)?.apply {
            setOnClickListener(_clickListener)
            setOnLongClickListener(_longClickListener)
        }
    }

    override fun _initItemListener(itemHolder: DslViewHolder) {
        //去掉整体item的事件监听
        //super._initItemListener(itemHolder)
    }

}