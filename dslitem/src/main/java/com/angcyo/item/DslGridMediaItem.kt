package com.angcyo.item

import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.item.style.GridMediaItemConfig
import com.angcyo.item.style.IGridMediaItem
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.R

/**
 * 网格图片展示item
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/08/04
 */
open class DslGridMediaItem : DslAdapterItem(), IGridMediaItem {

    override var gridMediaItemConfig: GridMediaItemConfig = GridMediaItemConfig()

    init {
        itemLayoutId = R.layout.dsl_nested_recycler_item

        itemWidth = -1
        itemHeight = -2
    }

    override fun onItemViewRecycled(itemHolder: DslViewHolder, itemPosition: Int) {
        super.onItemViewRecycled(itemHolder, itemPosition)
        onGridMediaRecyclerViewRecycled(itemHolder, itemPosition)
    }

}