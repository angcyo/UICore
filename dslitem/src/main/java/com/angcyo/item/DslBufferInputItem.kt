package com.angcyo.item

import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.item.style.itemEditDigits
import com.angcyo.item.style.itemEditHint
import com.angcyo.item.style.itemInputFilterList
import com.angcyo.item.style.itemMaxInputLength
import com.angcyo.widget.DslViewHolder

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/07/18
 */
class DslBufferInputItem : DslBaseEditItem() {

    init {
        itemLayoutId = R.layout.dsl_buffer_input_item

        itemEditHint
        itemMaxInputLength
        itemInputFilterList
        itemEditDigits
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)
        /*itemHolder.v<BufferTextInputLayout>(R.id.lib_buffer_input_layout)?.apply {
            hint = itemEditHint
            counterMaxLength = itemMaxInputLength
        }*/
    }

}