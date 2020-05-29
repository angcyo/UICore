package com.angcyo.core.dslitem

import com.angcyo.core.R
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.widget.DslViewHolder


/**
 * [DslStatusTipItem]
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/11/11
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

open class DslStatusTipItem : DslAdapterItem() {
    init {
        itemLayoutId = R.layout.dsl_status_tip_item
    }

    /**状态提示的文本*/
    var itemStatusText: CharSequence? = null

    /**状态提示的数量*/
    var itemStatusCount: Int = -1

    /**是否开启红点模式*/
    var itemShowDot = false

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem)

        itemHolder.itemView.isSelected = itemIsSelected

        itemHolder.tv(R.id.text_view)?.text = itemStatusText

        when {
            itemShowDot && itemStatusCount == 0 -> {
                itemHolder.visible(R.id.count_view)
                itemHolder.tv(R.id.count_view)?.text = ""
            }
            !itemShowDot && itemStatusCount == 0 -> {
                itemHolder.gone(R.id.count_view)
            }
            itemStatusCount < 0 -> {
                itemHolder.gone(R.id.count_view)
            }
            else -> {
                itemHolder.visible(R.id.count_view)
                itemHolder.tv(R.id.count_view)?.text = "$itemStatusCount"
            }
        }
    }
}