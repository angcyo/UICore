package com.angcyo.core.dslitem

import android.graphics.Color
import android.graphics.Rect
import com.angcyo.core.R
import com.angcyo.dsladapter.*
import com.angcyo.dsladapter.item.IDslItem
import com.angcyo.library.ex._colorDrawable
import com.angcyo.library.ex._drawable
import com.angcyo.widget.DslViewHolder

/**
 * 网格背景样式配置item, 同时还支持卡片样式
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/07/02
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
interface IGridStyleItem : IDslItem {

    fun initGridOffset(adapterItem: DslAdapterItem, outRect: Rect, insert: Int) {
        initGridOffset(adapterItem, outRect, insert, insert, insert, insert)
    }

    fun initGridOffsetHorizontal(adapterItem: DslAdapterItem, outRect: Rect, insert: Int) {
        initGridOffset(adapterItem, outRect, insert, 0, insert, 0)
    }

    fun initGridOffsetVertical(adapterItem: DslAdapterItem, outRect: Rect, insert: Int) {
        initGridOffset(adapterItem, outRect, 0, insert, 0, insert)
    }

    fun initGridOffset(
        adapterItem: DslAdapterItem,
        outRect: Rect,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ) {
        outRect.set(0, 0, 0, 0)
        adapterItem.itemGroupParams.apply {
            if (adapterItem.itemSpanCount == DslAdapterItem.FULL_ITEM) {
                //全屏宽度
                if (isOnlyOne()) {
                    outRect.left = left
                    outRect.top = top
                    outRect.right = right
                    outRect.bottom = bottom
                } else if (isFirstPosition() || isGroupFirstRow()) {
                    outRect.left = left
                    outRect.right = right
                    outRect.top = top
                } else if (isLastPosition() || isGroupLastRow()) {
                    outRect.left = left
                    outRect.right = right
                    outRect.bottom = bottom
                } else {
                    outRect.left = left
                    outRect.right = right
                }
            } else {
                if (isEdgeLeft()) {
                    outRect.left = left
                }
                if (isEdgeTop()) {
                    outRect.top = top
                }
                if (isEdgeRight()) {
                    outRect.right = right
                }
                if (isEdgeBottom()) {
                    outRect.bottom = bottom
                }
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
            if (adapterItem.itemSpanCount == DslAdapterItem.FULL_ITEM) {
                //全屏宽度
                when {
                    isOnlyOne() -> itemHolder.itemView.background =
                        _drawable(R.drawable.lib_white_round_shape)
                    isFirstPosition() || isGroupFirstRow() -> itemHolder.itemView.background =
                        _drawable(R.drawable.lib_white_top_round_shape)
                    isLastPosition() || isGroupLastRow() -> itemHolder.itemView.background =
                        _drawable(R.drawable.lib_white_bottom_round_shape)
                    else -> itemHolder.itemView.background = _colorDrawable(Color.WHITE)
                }
            } else {
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
                    else -> itemHolder.itemView.background = _colorDrawable(Color.WHITE)
                }
            }
        }
    }

}