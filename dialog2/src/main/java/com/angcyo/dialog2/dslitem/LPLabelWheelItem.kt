package com.angcyo.dialog2.dslitem

import com.angcyo.dialog2.R
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.item.style.ILabelItem
import com.angcyo.item.style.LabelItemConfig
import com.angcyo.widget.DslViewHolder

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/09/05
 */
open class LPLabelWheelItem : DslAdapterItem(), ILabelItem, IWheelItem {

    override var labelItemConfig: LabelItemConfig = LabelItemConfig()

    override var wheelItemConfig: WheelItemConfig = WheelItemConfig()

    init {
        itemLayoutId = R.layout.lp_label_wheel_item
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)
        itemHolder.tv(R.id.lib_text_view)?.text = itemWheelText()
        itemHolder.click(R.id.lib_content_wrap_layout) {
            showItemWheelDialog(it.context)
        }
    }
}