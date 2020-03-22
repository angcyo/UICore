package com.angcyo.item

import android.view.ViewGroup
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.library.ex._drawable
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget._img
import com.angcyo.widget.base.setWidth

/**
 * 普通的网格item
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/22
 */
open class DslGridItem : DslAdapterItem() {

    /**显示的文本*/
    var itemText: CharSequence? = null

    /**图标*/
    var itemIcon: Int = -1

    /**图标是1:1的大小*/
    var itemIconSize: Int = ViewGroup.LayoutParams.WRAP_CONTENT

    var itemBadgeText: String? = null

    init {
        itemLayoutId = R.layout.dsl_grid_item
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)

        itemHolder.tv(R.id.lib_text_view)?.text = itemText

        itemHolder._img(R.id.lib_image_view)?.apply {
            updateBadge {
                badgeText = itemBadgeText
            }
            setImageDrawable(_drawable(itemIcon))
            setWidth(itemIconSize)
        }
    }
}