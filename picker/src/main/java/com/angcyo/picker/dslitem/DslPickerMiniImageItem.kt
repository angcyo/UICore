package com.angcyo.picker.dslitem

import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.dsladapter.margin
import com.angcyo.glide.giv
import com.angcyo.item.DslImageItem
import com.angcyo.library.ex._color
import com.angcyo.library.ex.dpi
import com.angcyo.library.model.LoaderMedia
import com.angcyo.library.model.loadUri
import com.angcyo.library.model.mimeType
import com.angcyo.picker.R
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.setWidthHeight

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/19
 */

class DslPickerMiniImageItem : DslImageItem() {

    /**要加载的媒体*/
    var loaderMedia: LoaderMedia? = null
        get() = field ?: (itemData as? LoaderMedia)

    /**删除标识*/
    var itemIsDeleted: Boolean = false

    init {
        itemVideoCoverTipDrawable = -1
        margin(8 * dpi)
        onConfigImageView = {
            it.drawBorder = itemIsSelected
            it.borderColor = _color(R.color.picker_button_accent_bg_color)
        }
        itemLayoutId = R.layout.dsl_picker_mini_image_layout
    }

    override fun onSetItemData(data: Any?) {
        super.onSetItemData(data)
        if (data is LoaderMedia) {
            //显示不下
            //itemMediaDuration = value.duration
            itemLoadUri = data.loadUri()
            itemMimeType = data.mimeType()
        }
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        itemHolder.giv(R.id.lib_image_view)?.imageRadius = 0
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)
        itemHolder.itemView.setWidthHeight(80 * dpi, -2)

        itemHolder.visible(R.id.picker_delete_mask_view, itemIsDeleted)
        itemHolder.visible(R.id.picker_selector_mask_view, itemIsSelected)
    }
}