package com.angcyo.item

import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.item.style.CheckGroupItemConfig
import com.angcyo.item.style.ICheckGroupItem
import com.angcyo.item.style.ITextItem
import com.angcyo.item.style.TextItemConfig
import com.angcyo.widget.DslViewHolder

/**
 * 简版的[DslCheckGroupItem], 默认是单选切换的item
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/06/20
 */
open class DslCheckFlowItem : DslAdapterItem(), ITextItem, ICheckGroupItem {

    /**当前选中*/
    var itemCheckIndex: Int = -1
        set(value) {
            field = value
            checkGroupItemConfig.itemCheckedItems.clear()
            checkGroupItemConfig.itemCheckItems.getOrNull(value)?.let {
                checkGroupItemConfig.itemCheckedItems.add(it)
            }
        }

    override var textItemConfig: TextItemConfig = TextItemConfig()

    override var checkGroupItemConfig: CheckGroupItemConfig = CheckGroupItemConfig()

    init {
        itemLayoutId = R.layout.dsl_check_flow_item
        checkGroupItemConfig.itemCheckLayoutId = R.layout.layout_border_check
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)
    }

    override fun onCheckSelectIndexChange(
        fromIndex: Int,
        selectIndexList: List<Int>,
        reselect: Boolean,
        fromUser: Boolean
    ) {
        itemCheckIndex = selectIndexList.firstOrNull() ?: -1
        super.onCheckSelectIndexChange(fromIndex, selectIndexList, reselect, fromUser)
    }
}