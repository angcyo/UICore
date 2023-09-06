package com.angcyo.item

import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.dsladapter.itemViewHolder
import com.angcyo.item.style.ILabelItem
import com.angcyo.item.style.IOperateEditItem
import com.angcyo.item.style.LabelItemConfig
import com.angcyo.item.style.OperateEditItemConfig
import com.angcyo.widget.DslViewHolder

/**
 * 简单的输入item, 默认左右布局结构
 * 支持label
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/09/05
 */
open class DslSingleInputItem : DslAdapterItem(), ILabelItem, IOperateEditItem {

    override var labelItemConfig: LabelItemConfig = LabelItemConfig()

    override var operateEditItemConfig: OperateEditItemConfig = OperateEditItemConfig()

    init {
        itemLayoutId = R.layout.dsl_single_input_item
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)
    }

    override fun onItemChangeListener(item: DslAdapterItem) {
        hookOperateEditItemFocus(itemViewHolder())
        super.onItemChangeListener(item)
    }
}