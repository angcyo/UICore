package com.angcyo.item

import androidx.recyclerview.widget.RecyclerView
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.item.style.itemNestedLayoutManager
import com.angcyo.library.app
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.recycler.GridLayoutManagerWrap

/**
 * 网格
 * [DslNestedRecyclerItem]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/07/20
 */
class DslNestedGridRecyclerItem : DslNestedRecyclerItem() {

    /**网格数量*/
    var itemGridSpanCount = -1
        set(value) {
            field = value
            itemNestedLayoutManager =
                GridLayoutManagerWrap(app(), value, RecyclerView.VERTICAL, false)
        }

    init {
        itemGridSpanCount = 2
        //可以通过以下2个属性调整item的宽高
        //itemWidth = ViewGroup.LayoutParams.MATCH_PARENT
        //itemHeight = ViewGroup.LayoutParams.WRAP_CONTENT
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)
    }

}