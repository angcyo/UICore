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

/**网格四边插入偏移量*/
fun DslAdapterItem.initGridOffset(insert: Int = _dimen(R.dimen.lib_padding_left)) {
    initGridOffset(insert, insert, insert, insert)
}

fun DslAdapterItem.initGridOffsetHorizontal(insert: Int) {
    initGridOffset(insert, 0, insert, 0)
}

fun DslAdapterItem.initGridOffsetVertical(insert: Int) {
    initGridOffset(0, insert, 0, insert)
}

/**自动设置*/
fun DslAdapterItem.initGridOffset(left: Int, top: Int, right: Int, bottom: Int) {
    onSetItemOffset = {
        initGridOffset(it, left, top, right, bottom)
    }
}

/**网格四边插入偏移量*/
fun DslAdapterItem.initGridOffset(outRect: Rect, insert: Int) {
    initGridOffset(outRect, insert, insert, insert, insert)
}

fun DslAdapterItem.initGridOffsetHorizontal(outRect: Rect, insert: Int) {
    initGridOffset(outRect, insert, 0, insert, 0)
}

fun DslAdapterItem.initGridOffsetVertical(outRect: Rect, insert: Int) {
    initGridOffset(outRect, 0, insert, 0, insert)
}

/**需要在[onSetItemOffset]中调用此方法*/
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
                    } else {
                        if (isInLinearLayoutManager()) {
                            outRect.bottom = linearOffsetBottom
                        }
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
fun DslAdapterItem.initGridInset(insert: Int = _dimen(R.dimen.lib_padding_left)) {
    initGridInset(insert, insert, insert, insert)
}

fun DslAdapterItem.initGridInset2(
    horizontal: Int = _dimen(R.dimen.lib_padding_left),
    vertical: Int = _dimen(R.dimen.lib_item_height)
) {
    initGridInset(horizontal, vertical, horizontal, vertical)
}

/**自动设置*/
fun DslAdapterItem.initGridInset(left: Int, top: Int, right: Int, bottom: Int) {
    onSetItemOffset = {
        initGridInset(it, left, top, right, bottom)
    }
}

/**RecyclerView内边距插入, item之间不处理*/
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

fun DslAdapterItem.initLinearOffset(insert: Int = _dimen(R.dimen.lib_padding_left)) {
    initLinearOffset(insert, insert, insert, insert)
}

/**自动设置*/
fun DslAdapterItem.initLinearOffset(left: Int, top: Int, right: Int, bottom: Int) {
    onSetItemOffset = {
        initLinearOffset(it, left, top, right, bottom)
    }
}

/**RecyclerView内边距插入, item之间同步处理*/
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