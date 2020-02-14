package com.angcyo.item

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.library.ex.color
import com.angcyo.library.ex.undefined_res
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.getDrawable
import com.angcyo.widget.base.inflate
import com.angcyo.widget.base.setLeftIco
import com.angcyo.widget.layout.RLinearLayout

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/08/09
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class DslBaseInfoItem : DslAdapterItem() {

    /**背景*/
    var itemBackgroundDrawable: Drawable? = ColorDrawable(Color.WHITE)

    /**条目文本*/
    var itemInfoText: CharSequence? = null

    @DrawableRes
    var itemInfoIcon: Int = undefined_res

    var itemInfoIconColor: Int = undefined_res

    /**扩展布局信息*/
    @LayoutRes
    var itemExtendLayoutId: Int = undefined_res

    init {
        itemLayoutId = R.layout.dsl_info_item
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)

        (itemHolder.itemView as? RLinearLayout)?.layoutDelegate?.bDrawable = itemBackgroundDrawable

        //文本信息
        itemHolder.tv(R.id.lib_text_view)?.apply {
            text = itemInfoText

            if (itemInfoIconColor == undefined_res) {
                setLeftIco(itemInfoIcon)
            } else {
                setLeftIco(getDrawable(itemInfoIcon).color(itemInfoIconColor))
            }
        }

        //扩展布局
        if (itemExtendLayoutId != undefined_res) {
            var inflateLayoutId = undefined_res //已经inflate的布局id
            itemHolder.group(R.id.wrap_layout)?.apply {
                if (childCount > 0) {
                    inflateLayoutId = (getChildAt(0).getTag(R.id.tag) as? Int) ?: undefined_res
                }

                if (itemExtendLayoutId != inflateLayoutId) {
                    //两次inflate的布局不同
                    itemHolder.clear()
                    inflate(itemExtendLayoutId, true)
                    val view = getChildAt(0)
                    view.setTag(R.id.tag, itemExtendLayoutId)
                }
            }
        } else {
            itemHolder.group(R.id.wrap_layout)?.removeAllViews()
        }
    }
}