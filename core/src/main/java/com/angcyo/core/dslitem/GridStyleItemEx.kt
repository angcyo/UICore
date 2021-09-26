package com.angcyo.core.dslitem

import com.angcyo.core.R
import com.angcyo.dsladapter.*
import com.angcyo.library.ex._drawable
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.layout.ILayoutDelegate

/**
 * 网格背景样式配置item, 同时还支持卡片样式
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/07/02
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
/**自动设置*/
fun DslAdapterItem.initGridStyleItem() {
    itemBindOverride = { itemHolder, itemPosition, adapterItem, payloads ->
        initGridStyleItem(itemHolder, itemPosition, adapterItem, payloads)
    }
}

/**网格背景设置, 需要在[itemBindOverride]中调用此方法*/
fun DslAdapterItem.initGridStyleItem(
    itemHolder: DslViewHolder,
    itemPosition: Int,
    adapterItem: DslAdapterItem,
    payloads: List<Any>
) {
    itemGroupParams.apply {
        val itemView = itemHolder.itemView
        if (itemView is ILayoutDelegate) {
            itemView.getCustomLayoutDelegate().bDrawable = null
        }

        if (isFullWidthItem()) {
            //全屏宽度
            when {
                isOnlyOne() -> itemView.background =
                    _drawable(R.drawable.lib_white_round_selector)
                isFirstPosition() || isGroupFirstRow() -> itemView.background =
                    _drawable(R.drawable.lib_white_top_round_selector)
                isLastPosition() || isGroupLastRow() -> itemView.background =
                    _drawable(R.drawable.lib_white_bottom_round_selector)
                else -> itemView.background = _drawable(R.drawable.lib_white_selector)
            }
        } else {
            when {
                //独一个
                isOnlyOne() -> itemView.background =
                    _drawable(R.drawable.lib_white_round_selector)
                //左边1个
                isEdgeGroupLeftTop() && isEdgeGroupLeftBottom() -> itemView.background =
                    _drawable(R.drawable.lib_white_left_round_selector)
                //右边1个
                isEdgeGroupRightTop() && isEdgeGroupRightBottom() -> itemView.background =
                    _drawable(R.drawable.lib_white_right_round_selector)
                //最后一行独1个
                isEdgeGroupLeftBottom() && isEdgeGroupRightBottom() -> itemView.background =
                    _drawable(R.drawable.lib_white_bottom_round_selector)
                //左上
                isEdgeGroupLeftTop() -> itemView.background =
                    _drawable(R.drawable.lib_white_lt_round_selector)
                //右上
                isEdgeGroupRightTop() -> itemView.background =
                    _drawable(R.drawable.lib_white_tr_round_selector)
                //左下
                isEdgeGroupLeftBottom() -> itemView.background =
                    _drawable(R.drawable.lib_white_lb_round_selector)
                //右下
                isEdgeGroupRightBottom() -> itemView.background =
                    _drawable(R.drawable.lib_white_br_round_selector)
                else -> itemView.background = _drawable(R.drawable.lib_white_selector)
            }
        }
    }
}