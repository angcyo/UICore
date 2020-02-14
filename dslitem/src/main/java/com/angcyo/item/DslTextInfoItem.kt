package com.angcyo.item

import androidx.annotation.DrawableRes
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.library.ex.color
import com.angcyo.library.ex.undefined_res
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget._tv
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

    /**描述文本*/
    var itemDarkText: CharSequence? = null

    @DrawableRes
    var itemDarkIcon: Int = undefined_res
    var itemDarkIconColor: Int = undefined_res

    /**未读数,空字符串会绘制成小圆点 */
    var itemNoReadNumString: String? = null

    init {
        itemExtendLayoutId = R.layout.dsl_extent_text_item
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)

        //文本
        itemHolder._tv(R.id.lib_dark_text_view)?.apply {
            text = itemDarkText

            if (itemDarkIconColor == undefined_res) {
                setRightIco(itemDarkIcon)
            } else {
                setRightIco(getDrawable(itemDarkIcon).color(itemDarkIconColor))
            }

            dslBadeDrawable.badgeText = itemNoReadNumString
        }
    }
}