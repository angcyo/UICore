package com.angcyo.item

import android.graphics.drawable.Drawable
import android.view.ViewGroup
import androidx.annotation.Px
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.library.ex._dimen
import com.angcyo.library.ex._drawable
import com.angcyo.library.ex.setBgDrawable
import com.angcyo.library.ex.setWidthHeight
import com.angcyo.widget.DslViewHolder

/**
 * 一根线
 * [com.angcyo.item.DslEmptyItem]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/09/24
 */
class DslLineItem : DslAdapterItem() {

    /**线的高度*/
    @Px
    var itemLineHeight: Int = _dimen(R.dimen.lib_line)

    /**线的颜色*/
    var itemLineDrawable: Drawable? = _drawable(R.color.lib_line)

    init {
        itemLayoutId = R.layout.lib_line_item
        itemWidth = ViewGroup.LayoutParams.MATCH_PARENT
        itemHeight = ViewGroup.LayoutParams.WRAP_CONTENT
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)

        //线的高度
        itemHolder.view(R.id.lib_line_view)?.apply {
            setWidthHeight(itemWidth, itemLineHeight)
            setBgDrawable(itemLineDrawable)
        }
    }

}