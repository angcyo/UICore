package com.angcyo.dialog.dslitem

import android.graphics.drawable.Drawable
import android.view.View
import com.angcyo.dialog.R
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.library.ex._color
import com.angcyo.library.ex.setTintList
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.clickIt

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2024/09/06
 *
 * [DslDialogTextItem]
 * [DslDialogIconTextItem]
 */
class DslDialogIconTextItem : DslAdapterItem() {

    /**文本 支持span*/
    var itemText: CharSequence? = null

    /**图标-文本左边的*/
    var itemIcon: Drawable? = null

    /**图标-左边的*/
    var itemLeftIcon: Drawable? = null

    /**图标-右边的*/
    var itemRightIcon: Drawable? = null

    /**禁用时的颜色*/
    var itemDisabledColor: Int? = null

    /**左右图标的点击事件*/
    var itemLeftIconClick: ((View) -> Unit)? = null
    var itemRightIconClick: ((View) -> Unit)? = null

    init {
        itemLayoutId = R.layout.item_dialog_icon_text
        itemClick
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)

        //文本
        itemHolder.tv(R.id.lib_text_view)?.apply {
            /*setLeftIco(itemLeftDrawable)
            gravity = itemTextGravity
            setBoldText(itemTextBold)
            itemTextSize?.let { setTextSizePx(it) }*/
            text = itemText
            if (itemDisabledColor != null) {
                setTextColor(if (itemEnable) _color(R.color.lib_text_color) else itemDisabledColor!!)
            }
        }

        //icon
        itemHolder.img(R.id.lib_image_view)?.apply {
            setImageDrawable(itemIcon)
            if (itemDisabledColor != null) {
                setTintList(if (itemEnable) _color(R.color.lib_text_color) else itemDisabledColor!!)
            }
        }
        itemHolder.img(R.id.lib_left_image_view)?.apply {
            setImageDrawable(itemLeftIcon)
            if (itemDisabledColor != null) {
                setTintList(if (itemEnable) _color(R.color.lib_text_color) else itemDisabledColor!!)
            }
            clickIt(itemLeftIconClick)
        }
        itemHolder.img(R.id.lib_right_image_view)?.apply {
            setImageDrawable(itemRightIcon)
            if (itemDisabledColor != null) {
                setTintList(if (itemEnable) _color(R.color.lib_text_color) else itemDisabledColor!!)
            }
            clickIt(itemRightIconClick)
        }
    }

}