package com.angcyo.item

import android.graphics.drawable.Drawable
import android.view.ViewGroup
import com.angcyo.core.component.model.tintImageViewNight
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.dsladapter.isEdgeBottom
import com.angcyo.dsladapter.isEdgeLeft
import com.angcyo.dsladapter.isEdgeRight
import com.angcyo.dsladapter.isEdgeTop
import com.angcyo.dsladapter.padding
import com.angcyo.library.ex._drawable
import com.angcyo.library.ex.dpi
import com.angcyo.library.ex.setBgDrawable
import com.angcyo.library.ex.setWidth
import com.angcyo.library.ex.tooltipText
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget._img
import com.angcyo.widget.drawable.DslAttrBadgeDrawable

/**
 * 普通的网格item
 *
 * [com.angcyo.dialog.dslitem.DslDialogGridItem]
 * [com.angcyo.item.DslGridItem]
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/22
 */
open class DslGridItem : DslAdapterItem() {

    /**显示的文本*/
    var itemText: CharSequence? = null

    /**item的长按文件提示内容*/
    var itemTooltipText: CharSequence? = null

    /**图标*/
    var itemIcon: Int = -1

    /**图标背景*/
    var itemImageBg: Drawable? = null

    /**图片控件的填充*/
    var itemImagePadding: Int = 12 * dpi

    /**文本控件的填充*/
    var itemTextPadding: Int = 8 * dpi

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

        //长按提示
        itemHolder.itemView.tooltipText(itemTooltipText)

        //文本
        itemHolder.tv(R.id.lib_text_view)?.apply {
            text = itemText
            padding(itemTextPadding)
        }

        //图标
        itemHolder._img(R.id.lib_image_view)?.apply {
            tintImageViewNight()
            
            padding(itemImagePadding)

            updateBadge {
                badgeText = itemBadgeText
                itemConfigBadge(this)
            }
            setImageDrawable(_drawable(itemIcon))
            setWidth(itemIconSize)

            setBgDrawable(itemImageBg)
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