package com.angcyo.item

import androidx.annotation.DrawableRes
import com.angcyo.drawable.color
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.library.ex.undefined_res
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.getDrawable
import com.angcyo.widget.base.setRightIco

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/08/09
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class DslTextInfoItem : DslBaseInfoItem() {
    init {
        itemExtendLayoutId = R.layout.dsl_extent_text_item
    }

    /**显示未读小红点*/
    var itemShowNoRead: Boolean = false

    /**描述文本*/
    var itemDarkText: CharSequence? = null

    @DrawableRes
    var itemDarkIcon: Int = undefined_res
    var itemDarkIconColor: Int = undefined_res

    /**未读数*/
    var itemNoReadNumString: String? = null

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem)

        //文本
        itemHolder.tv(R.id.sub_text_view)?.apply {
            text = itemDarkText

            if (itemDarkIconColor == undefined_res) {
                setRightIco(itemDarkIcon)
            } else {
                setRightIco(getDrawable(itemDarkIcon).color(itemDarkIconColor))
            }
//
//            setShowNoRead(itemShowNoRead)
        }
//
//        //未读数
//        itemHolder.v<RDrawNoReadNumView>(R.id.read_num_view)?.apply {
//            marginLayoutParams {
//                rightMargin = if (itemDarkIcon > 0) {
//                    40 * dpi
//                } else {
//                    16 * dpi
//                }
//            }
//            getDrawReadNum().readNumString = itemNoReadNumString
//        }
    }
}