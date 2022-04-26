package com.angcyo.item

import android.graphics.drawable.Drawable
import android.view.View
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.item.style.ITextItem
import com.angcyo.item.style.TextItemConfig
import com.angcyo.item.style.TextStyleConfig
import com.angcyo.library.ex.dpi
import com.angcyo.library.ex.gone
import com.angcyo.library.ex.loadDrawable
import com.angcyo.library.ex.visible
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.throttleClickIt

/**
 * 简单的文本显示item, 支持右图标设置
 *
 * [DslInlineLabelTextItem]
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/23
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class DslLabelTextItem : DslBaseLabelItem(), ITextItem {

    /**右边按钮*/
    var itemRightIcon: Int = -1

    /**优先于属性[itemRightIcon]*/
    var itemRightDrawable: Drawable? = null

    /**右边图标点击事件*/
    var itemRightIcoClick: ((DslViewHolder, View) -> Unit)? = null

    /**右边的文本*/
    var itemRightText: CharSequence? = null
        set(value) {
            field = value
            itemRightTextStyle.text = value
        }

    /**统一样式配置*/
    var itemRightTextStyle = TextStyleConfig()

    override var textItemConfig: TextItemConfig = TextItemConfig().apply {
        itemTextStyle.goneOnTextEmpty = true
    }

    init {
        itemLayoutId = R.layout.dsl_label_text_item
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)

        itemHolder.img(R.id.lib_right_ico_view)?.apply {
            if (itemRightIcoClick == null) {
                setOnClickListener(null)
                isClickable = false
            } else {
                throttleClickIt {
                    itemRightIcoClick?.invoke(itemHolder, it)
                }
            }
            val drawable = itemRightDrawable ?: loadDrawable(itemRightIcon)
            if (drawable == null) {
                gone()
            } else {
                visible()
                setImageDrawable(drawable)
            }
        }

        itemHolder.gone(R.id.lib_right_text_view, itemRightTextStyle.text == null)
        itemHolder.tv(R.id.lib_right_text_view)?.apply {
            itemRightTextStyle.updateStyle(this)
        }
    }

    /**配置最小高度样式*/
    fun minHeightStyle(padding: Int = 3 * dpi) {
        //itemMinHeight = _dimen(R.dimen.lib_min_item_height)
        itemPaddingTop = padding
        itemPaddingBottom = padding
    }
}