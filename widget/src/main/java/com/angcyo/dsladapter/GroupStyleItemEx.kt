package com.angcyo.dsladapter

import android.graphics.Rect
import com.angcyo.library.ex._dimen
import com.angcyo.widget.R

/**
 * 网格背景样式配置item, 同时还支持卡片样式
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/09/23
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */

/**网格四边插入偏移量, 自动设置*/
fun DslAdapterItem.initGridOffset(
    left: Int = _dimen(R.dimen.lib_padding_left),
    top: Int = _dimen(R.dimen.lib_padding_left),
    right: Int = _dimen(R.dimen.lib_padding_left),
    bottom: Int = _dimen(R.dimen.lib_padding_left),
    linearOffsetBottom: Int = 0
) {
    onSetItemOffset = {
        initGridOffset(it, left, top, right, bottom, linearOffsetBottom)
    }
}

/**需要在[onSetItemOffset]中调用此方法
 *
 * 四边和组之间都有偏移
 * */
fun DslAdapterItem.initGridOffset(
    outRect: Rect,
    left: Int,
    top: Int,
    right: Int,
    bottom: Int,
    linearOffsetBottom: Int = 0 /*线性连续item之间的间隙*/
) {
    if (onSetItemOffset == null) {
        onSetItemOffset = {
            initGridOffset(it, left, top, right, bottom)
        }
    } else {
        outRect.set(0, 0, 0, 0)
        itemGroupParams.apply {
            if (isFullWidthItem()) {
                //全屏宽度
                outRect.left = left
                outRect.right = right

                if (isOnlyOne()) {
                    outRect.top = top
                    if (isLastGroup()) {
                        outRect.bottom = bottom
                    }
                } else if (isFirstPosition() || isGroupFirstRow()) {
                    outRect.top = top
                    if (isInLinearLayoutManager()) {
                        outRect.bottom = linearOffsetBottom
                    }
                } else if (isLastPosition() || isGroupLastRow()) {
                    outRect.bottom = bottom
                } else {
                    //中间item
                    if (isInLinearLayoutManager()) {
                        outRect.bottom = linearOffsetBottom
                    }
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
}

/**网格四边插入偏移量*/
fun DslAdapterItem.initGridInset2(
    horizontal: Int = _dimen(R.dimen.lib_padding_left),
    vertical: Int = _dimen(R.dimen.lib_item_height)
) {
    initGridInset(horizontal, vertical, horizontal, vertical)
}

/**自动设置*/
fun DslAdapterItem.initGridInset(
    left: Int = _dimen(R.dimen.lib_padding_left),
    top: Int = _dimen(R.dimen.lib_padding_left),
    right: Int = _dimen(R.dimen.lib_padding_left),
    bottom: Int = _dimen(R.dimen.lib_padding_left)
) {
    onSetItemOffset = {
        initGridInset(it, left, top, right, bottom)
    }
}

/**RecyclerView内边距插入, item之间不处理
 * 四边有偏移, 组之间无*/
fun DslAdapterItem.initGridInset(outRect: Rect, left: Int, top: Int, right: Int, bottom: Int) {
    if (onSetItemOffset == null) {
        onSetItemOffset = {
            initGridOffset(it, left, top, right, bottom)
        }
    } else {
        outRect.set(itemLeftInsert, itemTopInsert, itemRightInsert, itemBottomInsert) //初始化成默认的值
        itemGroupParams.apply {
            if (isFullWidthItem()) {
                //全屏宽度
                outRect.left = left
                outRect.right = right

                if (isFirstGroup() || isLastGroup()) {
                    if (isOnlyOne()) {
                        outRect.top = top
                        if (isLastGroup()) {
                            outRect.bottom = bottom
                        }
                    } else if (isFirstPosition() || isGroupFirstRow()) {
                        outRect.top = top
                    } else if (isLastPosition() || isGroupLastRow()) {
                        outRect.bottom = bottom
                    }
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
}

/**自动设置*/
fun DslAdapterItem.initLinearOffset(
    left: Int = _dimen(R.dimen.lib_padding_left),
    top: Int = _dimen(R.dimen.lib_padding_left),
    right: Int = _dimen(R.dimen.lib_padding_left),
    bottom: Int = _dimen(R.dimen.lib_padding_left)
) {
    onSetItemOffset = {
        initLinearOffset(it, left, top, right, bottom)
    }
}

/**RecyclerView内边距插入, item之间同步处理
 * 四边有偏移, 组之间和组内item都有偏移*/
fun DslAdapterItem.initLinearOffset(outRect: Rect, left: Int, top: Int, right: Int, bottom: Int) {
    if (onSetItemOffset == null) {
        onSetItemOffset = {
            initLinearOffset(it, left, top, right, bottom)
        }
    } else {
        outRect.set(0, 0, 0, 0)
        itemGroupParams.apply {
            outRect.top = top
            outRect.left = left
            outRect.right = right
            if (isOnlyOne() || isLastPosition() || isGroupLastRow()) {
                outRect.bottom = bottom
            }
        }
    }
}

//<editor-fold desc="分组与边距, 分组与分组之间的偏移">

/**自动设置*/
fun DslAdapterItem.initGroupOffset(
    left: Int = _dimen(R.dimen.lib_padding_left),
    top: Int = _dimen(R.dimen.lib_padding_left),
    right: Int = _dimen(R.dimen.lib_padding_left),
    bottom: Int = _dimen(R.dimen.lib_padding_left)
) {
    onSetItemOffset = {
        initGroupOffset(it, left, top, right, bottom)
    }
}

/**RecyclerView内边距插入, item之间不处理
 * 四边有偏移, 组之间无*/
fun DslAdapterItem.initGroupOffset(outRect: Rect, left: Int, top: Int, right: Int, bottom: Int) {
    if (onSetItemOffset == null) {
        onSetItemOffset = {
            initGroupOffset(it, left, top, right, bottom)
        }
    } else {
        outRect.set(itemLeftInsert, itemTopInsert, itemRightInsert, itemBottomInsert) //初始化成默认的值
        itemGroupParams.apply {
            if (isFullWidthItem()) {
                //全屏宽度
                outRect.left = left
                outRect.right = right

                if (isFirstPosition()) {
                    outRect.top = top
                }

                if (isLastGroup() && isLastPosition()) {
                    outRect.bottom = bottom
                }
            } else {
                //待测试
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
}

//</editor-fold desc="分组与边距, 分组与分组之间的偏移">