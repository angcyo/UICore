package com.angcyo.crop.ui.dslitem

import android.content.res.ColorStateList
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.angcyo.core.component.model.NightModel
import com.angcyo.core.vmApp
import com.angcyo.crop.R
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.library.ex._color
import com.angcyo.library.ex.setTintList
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

    /**被禁用时的图标颜色*/
    @ColorInt
    var itemIcoDisableColor: Int = _color(R.color.crop_item_disable)

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
        if (itemEnable) {
            imageView?.setTintList(null)
            vmApp<NightModel>().tintImageViewNight(imageView)
        } else {
            imageView?.setTintList(ColorStateList.valueOf(itemIcoDisableColor))
        }
        imageView?.setImageResource(itemIco)

        //
        itemHolder.gone(R.id.lib_text_view, itemText == null)
        itemHolder.tv(R.id.lib_text_view)?.text = itemText
    }
}