package com.angcyo.item

import android.widget.TextView
import androidx.annotation.LayoutRes
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.item.style.ITabLayoutItem
import com.angcyo.item.style.TabLayoutItemConfig
import com.angcyo.library.ex.find
import com.angcyo.tablayout.DslTabLayout
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.resetChild

/**
 * 分段的[DslTabLayout]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/06/10
 */
class DslSegmentTabItem : DslAdapterItem(), ITabLayoutItem {

    /**需要填充的布局*/
    @LayoutRes
    var itemSegmentLayoutId: Int = R.layout.lib_segment_layout

    /**项*/
    var itemSegmentList = mutableListOf<CharSequence?>()

    override var tabLayoutItemConfig: TabLayoutItemConfig = TabLayoutItemConfig()

    init {
        itemLayoutId = R.layout.dsl_segment_tab_item
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)
    }

    override fun _initItemConfig(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        //填充布局
        itemHolder.v<DslTabLayout>(tabLayoutItemConfig.itemTabLayoutViewId)?.apply {
            resetChild(itemSegmentList, itemSegmentLayoutId) { itemView, item, itemIndex ->
                itemView.find<TextView>(R.id.lib_text_view)?.text = item
            }
        }
        super._initItemConfig(itemHolder, itemPosition, adapterItem, payloads)
    }
}