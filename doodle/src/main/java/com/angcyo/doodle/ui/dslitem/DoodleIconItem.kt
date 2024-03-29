package com.angcyo.doodle.ui.dslitem

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.angcyo.core.component.model.NightModel
import com.angcyo.core.vmApp
import com.angcyo.doodle.R
import com.angcyo.drawable.base.dslGradientDrawable
import com.angcyo.drawable.base.solidCircle
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.item.style.INewItem
import com.angcyo.item.style.NewItemConfig
import com.angcyo.library.ex._color
import com.angcyo.library.ex.setBgDrawable
import com.angcyo.library.ex.setTintList
import com.angcyo.library.ex.tooltipText
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.span.span

/**
 * 涂鸦功能item
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/08/19
 */
open class DoodleIconItem : DslAdapterItem(), INewItem {

    /**图标资源*/
    @DrawableRes
    var itemIco: Int = 0

    /**文本*/
    var itemText: CharSequence? = null

    /**item的长按文件提示内容*/
    var itemTooltipText: CharSequence? = null

    /**上角标*/
    var itemTextSuperscript: CharSequence? = null

    /**被禁用时的图标颜色*/
    @ColorInt
    var itemIcoDisableColor: Int = _color(R.color.doodle_item_disable)

    /**check提示的颜色*/
    var itemCheckColor: Int = Color.TRANSPARENT

    /**回调, 用来更新[]itemCheckColor*/
    var itemUpdateCheckColorAction: (() -> Int)? = null

    override var newItemConfig: NewItemConfig = NewItemConfig()

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

        //长按提示
        itemHolder.itemView.tooltipText(itemTooltipText)

        //check tip
        itemUpdateCheckColorAction?.let {
            itemCheckColor = it.invoke()
        }
        if (itemCheckColor == Color.TRANSPARENT) {
            itemHolder.gone(R.id.check_tip_view)
        } else {
            itemHolder.visible(R.id.check_tip_view)
            itemHolder.view(R.id.check_tip_view)?.setBgDrawable(circleDrawable(itemCheckColor))
        }

        //
        val imageView = itemHolder.img(R.id.lib_image_view)
        if (itemEnable) {
            imageView?.setTintList(null)
            vmApp<NightModel>().tintImageViewNight(imageView)
        } else {
            imageView?.setTintList(ColorStateList.valueOf(itemIcoDisableColor))
        }
        imageView?.setImageResource(itemIco)

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

    /**圆*/
    fun circleDrawable(color: Int): Drawable =
        dslGradientDrawable {
            solidCircle(color)
        }
}