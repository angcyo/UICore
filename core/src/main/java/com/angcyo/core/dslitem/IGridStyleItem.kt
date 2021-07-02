package com.angcyo.core.dslitem

import android.graphics.Rect
import com.angcyo.core.R
import com.angcyo.dsladapter.*
import com.angcyo.library.ex._drawable
import com.angcyo.widget.DslViewHolder

/**
 * 网格背景样式配置item
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/07/02
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
interface IGridStyleItem {

    fun initGridOffset(adapterItem: DslAdapterItem, outRect: Rect, insert: Int) {
        outRect.set(0, 0, 0, 0)
        adapterItem.itemGroupParams.apply {
            if (isEdgeLeft()) {
                outRect.left = insert
            }
            if (isEdgeTop()) {
                outRect.top = insert
            }
            if (isEdgeRight()) {
                outRect.right = insert
            }
            if (isEdgeBottom()) {
                outRect.bottom = insert
            }
        }
    }

    fun initGridStyleItem(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        adapterItem.itemGroupParams.apply {
            when {
                //独一个
                isOnlyOne() -> itemHolder.itemView.background =
                    _drawable(R.drawable.lib_white_round_shape)
                //左边1个
                isEdgeGroupLeftTop() && isEdgeGroupLeftBottom() -> itemHolder.itemView.background =
                    _drawable(R.drawable.lib_white_left_round_shape)
                //右边1个
                isEdgeGroupRightTop() && isEdgeGroupRightBottom() -> itemHolder.itemView.background =
                    _drawable(R.drawable.lib_white_right_round_shape)
                //最后一行独1个
                isEdgeGroupLeftBottom() && isEdgeGroupRightBottom() -> itemHolder.itemView.background =
                    _drawable(R.drawable.lib_white_bottom_round_shape)
                //左上
                isEdgeGroupLeftTop() -> itemHolder.itemView.background =
                    _drawable(R.drawable.lib_white_lt_round_shape)
                //右上
                isEdgeGroupRightTop() -> itemHolder.itemView.background =
                    _drawable(R.drawable.lib_white_tr_round_shape)
                //左下
                isEdgeGroupLeftBottom() -> itemHolder.itemView.background =
                    _drawable(R.drawable.lib_white_lb_round_shape)
                //右下
                isEdgeGroupRightBottom() -> itemHolder.itemView.background =
                    _drawable(R.drawable.lib_white_br_round_shape)
            }
        }
    }

}