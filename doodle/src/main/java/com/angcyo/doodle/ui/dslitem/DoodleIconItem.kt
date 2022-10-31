package com.angcyo.doodle.ui.dslitem

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.angcyo.doodle.R
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.library.ex._color
import com.angcyo.library.ex._drawable
import com.angcyo.library.ex.color
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.span.span

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

    /**上角标*/
    var itemTextSuperscript: CharSequence? = null

    /**被禁用时的图标颜色*/
    @ColorInt
    var itemIcoDisableColor: Int = _color(R.color.doodle_item_disable)

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

        //
        val imageView = itemHolder.img(R.id.lib_image_view)
        if (itemEnable) {
            imageView?.setImageResource(itemIco)
        } else {
            val drawable = _drawable(itemIco)?.run {
                color(itemIcoDisableColor)
            }
            imageView?.setImageDrawable(drawable)
        }

        //
        itemHolder.gone(R.id.lib_text_view, itemText == null)
        itemHolder.tv(R.id.lib_text_view)?.text = span {
            append(itemText)
            append(itemTextSuperscript) {
                isSuperscript = true
                relativeSizeScale = 0.6f
            }
        }
    }
}