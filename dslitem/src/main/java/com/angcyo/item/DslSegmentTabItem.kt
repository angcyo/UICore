package com.angcyo.item

import android.widget.TextView
import androidx.annotation.LayoutRes
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.item.style.ITabLayoutItem
import com.angcyo.item.style.TabLayoutItemConfig
import com.angcyo.item.style.itemCurrentIndex
import com.angcyo.item.style.itemSelectIndexChangeAction
import com.angcyo.library.ex.find
import com.angcyo.library.ex.string
import com.angcyo.library.ex.toStr
import com.angcyo.library.extend.IToText
import com.angcyo.library.extend.IToValue
import com.angcyo.tablayout.DslTabLayout
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.resetChild

/**
 * 分段的[DslTabLayout]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/06/10
 */
open class DslSegmentTabItem : DslAdapterItem(), ITabLayoutItem {

    /**需要填充的布局*/
    @LayoutRes
    var itemSegmentLayoutId: Int = R.layout.lib_segment_layout

    /**分段的项*/
    var itemSegmentList = listOf<Any?>()

    /**将选项[item], 转成可以显示在界面的 文本类型*/
    var itemSegmentToText: (item: Any?) -> CharSequence? = { item ->
        if (item is IToText) {
            item.toText()
        } else {
            item.string()
        }
    }

    /**将选项[item], 转成表单上传的数据*/
    var itemSegmentToValue: (item: Any?) -> Any? = { item ->
        if (item is IToValue) {
            item.toValue()
        } else {
            item?.toStr()
        }
    }

    override var tabLayoutItemConfig: TabLayoutItemConfig = TabLayoutItemConfig()

    init {
        itemLayoutId = R.layout.dsl_segment_tab_item

        //回调监听
        itemSelectIndexChangeAction
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
                itemView.find<TextView>(R.id.lib_text_view)?.text = itemSegmentToText(item)
            }
        }
        super._initItemConfig(itemHolder, itemPosition, adapterItem, payloads)
    }

    override fun onItemChangeListener(item: DslAdapterItem) {
        super.onItemChangeListener(item)
        itemCurrentIndex //当前选中的索引
    }
}