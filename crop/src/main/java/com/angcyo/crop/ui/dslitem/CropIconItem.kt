package com.angcyo.crop.ui.dslitem

import androidx.annotation.DrawableRes
import com.angcyo.crop.R
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.widget.DslViewHolder

/**
 * 裁剪功能item
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/08/22
 */
open class CropIconItem : DslAdapterItem() {

    /**图标资源*/
    @DrawableRes
    var itemIco: Int = 0

    /**图标旋转的角度*/
    var itemIcoRotate: Float = 0f

    /**文本*/
    var itemText: CharSequence? = null

    init {
        itemLayoutId = R.layout.item_crop_icon_layout
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)

        //
        val imageView = itemHolder.img(R.id.lib_image_view)
        imageView?.rotation = itemIcoRotate
        imageView?.setImageResource(itemIco)

        //
        itemHolder.gone(R.id.lib_text_view, itemText == null)
        itemHolder.tv(R.id.lib_text_view)?.text = itemText
    }
}