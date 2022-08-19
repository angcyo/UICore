package com.angcyo.doodle.ui.dslitem

import androidx.annotation.DrawableRes
import com.angcyo.doodle.R
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.widget.DslViewHolder

/**
 * 涂鸦功能item
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/08/19
 */
open class DoodleIconItem : DslAdapterItem() {

    /**图标资源*/
    @DrawableRes
    var itemIco: Int = 0

    /**文本*/
    var itemText: CharSequence? = null

    init {
        itemLayoutId = R.layout.item_doodle_icon_layout
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)

        //itemHolder.img(R.id.lib_image_view)?.setImageDrawable(drawable)
        itemHolder.img(R.id.lib_image_view)?.setImageResource(itemIco)

        itemHolder.gone(R.id.lib_text_view, itemText == null)
        itemHolder.tv(R.id.lib_text_view)?.text = itemText
    }
}