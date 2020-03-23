package com.angcyo.item

import android.view.ViewGroup
import com.angcyo.dsladapter.*
import com.angcyo.library.ex._drawable
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget._img
import com.angcyo.widget.base.setWidth
import com.angcyo.widget.drawable.DslAttrBadgeDrawable

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

    /**开启智能分割线, 只有在非边界的item才绘制*/
    var itemGridInsert = -1

    /**角标配置*/
    var itemConfigBadge: (DslAttrBadgeDrawable) -> Unit = {}

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

        //文本
        itemHolder.tv(R.id.lib_text_view)?.text = itemText

        //图标
        itemHolder._img(R.id.lib_image_view)?.apply {
            updateBadge {
                badgeText = itemBadgeText
                itemConfigBadge(this)
            }
            setImageDrawable(_drawable(itemIcon))
            setWidth(itemIconSize)
        }

        //智能分割线
        if (itemGridInsert > 0) {
            itemGroupParams.apply {
                //itemLeftInsert = itemGridInsert
                //itemTopInsert = itemGridInsert
                itemRightInsert = itemGridInsert
                itemBottomInsert = itemGridInsert
                if (isEdgeLeft()) {
                    itemLeftInsert = 0
                }
                if (isEdgeTop()) {
                    itemTopInsert = 0
                }
                if (isEdgeRight()) {
                    itemRightInsert = 0
                }
                if (isEdgeBottom()) {
                    itemBottomInsert = 0
                }
            }
        }
    }
}