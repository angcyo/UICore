package com.angcyo.item

import androidx.core.view.updateLayoutParams
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.item.style.itemCurrentIndex
import com.angcyo.item.style.itemTabSelectIndexChangeAction
import com.angcyo.tablayout.DslTabLayout
import com.angcyo.widget.DslViewHolder

/**
 * 块状颜色分段的[DslSegmentTabItem]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/09/27
 */
open class DslSegmentSolidTabItem : DslSegmentTabItem() {

    /**指定[DslTabLayout]布局的宽度*/
    var itemTabLayoutWidth: Int? = null

    init {
        itemLayoutId = R.layout.dsl_segment_solid_tab_item

        //当前选中的索引
        itemCurrentIndex

        //选项列表
        itemSegmentList

        //回调监听
        itemTabSelectIndexChangeAction
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)

        itemTabLayoutWidth?.let {
            itemHolder.v<DslTabLayout>(tabLayoutItemConfig.itemTabLayoutViewId)?.apply {
                updateLayoutParams {
                    width = it
                }
            }
        }
    }

}