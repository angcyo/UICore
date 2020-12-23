package com.angcyo.dsladapter

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


/**
 * 群组/分组 助手
 *
 * https://github.com/angcyo/DslAdapter/wiki/%E7%BE%A4%E7%BB%84%E5%8A%9F%E8%83%BD
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/10/16
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

/**查找与[dslAdapterItem]相同分组的所有[DslAdapterItem]*/
fun DslAdapter.findItemGroupParams(dslAdapterItem: DslAdapterItem): ItemGroupParams {
    val params = ItemGroupParams()
    params.currentAdapterItem = dslAdapterItem

    val allItemList = getValidFilterDataList()
    var interruptGroup = false
    var findAnchor = false

    //分组数据计算
    for (i in allItemList.indices) {
        val newItem = allItemList[i]

        if (!interruptGroup) {
            when {
                newItem == dslAdapterItem -> {
                    params.groupItems.add(newItem)
                    findAnchor = true
                }
                dslAdapterItem.isItemInGroups(newItem) -> params.groupItems.add(newItem)
                //如果不是连续的, 则中断分组
                findAnchor -> interruptGroup = true
            }
        }

        if (interruptGroup) {
            break
        }
    }

    params.indexInGroup = params.groupItems.indexOf(dslAdapterItem)

    //网格边界计算
    val layoutManager = _recyclerView?.layoutManager

    if (layoutManager is GridLayoutManager) {
        layoutManager.apply {
            val itemPosition = allItemList.indexOf(dslAdapterItem)

            val groupItemList = params.groupItems

            val spanSizeLookup = spanSizeLookup ?: GridLayoutManager.DefaultSpanSizeLookup()

            //当前位置
            val currentSpanParams =
                getSpanParams(spanSizeLookup, itemPosition, spanCount, params.indexInGroup)

            //下一个的信息
            val nextItemPosition: Int = itemPosition + 1
            val nextSpanParams = if (allItemList.size > nextItemPosition) {
                getSpanParams(
                    spanSizeLookup,
                    nextItemPosition,
                    spanCount,
                    groupItemList.indexOf(allItemList[nextItemPosition])
                )
            } else {
                SpanParams()
            }

            //分组第一个
            val firstItemPosition = allItemList.indexOf(groupItemList.firstOrNull())
            val firstSpanParams = if (firstItemPosition == -1) {
                SpanParams()
            } else {
                getSpanParams(
                    spanSizeLookup,
                    firstItemPosition,
                    spanCount,
                    groupItemList.indexOf(allItemList[firstItemPosition])
                )
            }

            //分组最后一个
            val lastItemPosition = allItemList.indexOf(groupItemList.lastOrNull())
            val lastSpanParams = if (lastItemPosition == -1) {
                SpanParams()
            } else {
                getSpanParams(
                    spanSizeLookup,
                    lastItemPosition,
                    spanCount,
                    groupItemList.indexOf(allItemList[lastItemPosition])
                )
            }

            //adapter第一个
            val firstPosition = 0
            val firstParams = if (allItemList.isEmpty()) {
                SpanParams()
            } else {
                getSpanParams(
                    spanSizeLookup,
                    firstPosition,
                    spanCount,
                    groupItemList.indexOf(allItemList[firstPosition])
                )
            }

            //adapter最后一个
            val lastPosition = allItemList.lastIndex
            val lastParams = if (allItemList.isEmpty()) {
                SpanParams()
            } else {
                getSpanParams(
                    spanSizeLookup,
                    lastPosition,
                    spanCount,
                    groupItemList.indexOf(allItemList[lastPosition])
                )
            }

            params.edgeGridParams = EdgeGridParams(
                currentSpanParams, nextSpanParams,
                firstSpanParams, lastSpanParams,
                firstParams, lastParams
            )
        }
    } else if (layoutManager is LinearLayoutManager) {
        //todo
    }

    return params
}

fun getSpanParams(
    spanSizeLookup: GridLayoutManager.SpanSizeLookup,
    itemPosition: Int,
    spanCount: Int,
    indexInGroup: Int = -1
): SpanParams {

    val spanParams = SpanParams(indexInGroup = indexInGroup)
    spanParams.spanCount = spanCount
    spanParams.itemPosition = itemPosition
    spanParams.spanGroupIndex = spanSizeLookup.getSpanGroupIndex(itemPosition, spanCount)
    spanParams.spanIndex = spanSizeLookup.getSpanIndex(itemPosition, spanCount)
    spanParams.spanSize = spanSizeLookup.getSpanSize(itemPosition)

    return spanParams
}

/**[DslAdapterItem]分组信息. 分组依据[DslAdapterItem.isItemInGroups]*/
data class ItemGroupParams(
    /**当前[item]在一组中的索引值[index]*/
    var indexInGroup: Int = RecyclerView.NO_POSITION,
    var currentAdapterItem: DslAdapterItem? = null,
    /**一组中的所有[item]*/
    var groupItems: MutableList<DslAdapterItem> = mutableListOf(),

    var edgeGridParams: EdgeGridParams = EdgeGridParams()
)

/**网格分组边界[Edge]信息*/
data class EdgeGridParams(

    //当前位置的参数
    var currentSpanParams: SpanParams = SpanParams(),
    //下一个位置的参数
    var nextSpanParams: SpanParams = SpanParams(),

    //分组中第一个位置的数据
    var firstSpanParams: SpanParams = SpanParams(),

    //分组中最后一个位置的数据
    var lastSpanParams: SpanParams = SpanParams(),

    //adapter第一个位置的数据
    var firstParams: SpanParams = SpanParams(),

    //adapter最后一个位置的数据
    var lastParams: SpanParams = SpanParams()
)

/**网格分组中[Span]的信息*/
data class SpanParams(
    //androidx.recyclerview.widget.GridLayoutManager.getSpanCount
    var spanCount: Int = 1,
    //当前的位置, 在适配器中的索引
    var itemPosition: Int = RecyclerView.NO_POSITION,
    //当前的索引在群组中
    var indexInGroup: Int = RecyclerView.NO_POSITION,
    //在第几行
    var spanGroupIndex: Int = RecyclerView.NO_POSITION,
    //在第几列
    var spanIndex: Int = RecyclerView.NO_POSITION,
    //占多少列
    var spanSize: Int = RecyclerView.NO_POSITION
)

//是否是首列
fun SpanParams.isFirstSpan(): Boolean = spanIndex == 0

//是否是尾列
fun SpanParams.isLastSpan(): Boolean = spanIndex + spanSize == spanCount

//<editor-fold desc="线性布局中的分组信息扩展方法">

/**一组中,仅有一个*/
fun ItemGroupParams.isOnlyOne(): Boolean = groupItems.size == 1

/**是否是一组中的第一个位置*/
fun ItemGroupParams.isFirstPosition(): Boolean = indexInGroup == 0 && currentAdapterItem != null

/**是否是一组中的最后一个位置*/
fun ItemGroupParams.isLastPosition(): Boolean =
    currentAdapterItem != null && indexInGroup == groupItems.lastIndex

//</editor-fold desc="线性布局中的分组信息扩展方法">

//<editor-fold desc="网格布局中的边界扩展方法">

/**网格布局, 边界扩展方法*/
//是否在4条边上

/**在网格的左边*/
fun ItemGroupParams.isEdgeLeft(): Boolean = edgeGridParams.currentSpanParams.spanIndex == 0

/**在网格的右边*/
fun ItemGroupParams.isEdgeRight(): Boolean = edgeGridParams.currentSpanParams.run {
    spanIndex + spanSize == spanCount
}

/**在网格的上边*/
fun ItemGroupParams.isEdgeTop(): Boolean = edgeGridParams.currentSpanParams.spanGroupIndex == 0

/**在网格的下边*/
fun ItemGroupParams.isEdgeBottom(): Boolean = edgeGridParams.run {
    lastParams.spanGroupIndex == currentSpanParams.spanGroupIndex
}

/**全屏占满网格整个一行*/
fun ItemGroupParams.isEdgeHorizontal(): Boolean = isEdgeLeft() && isEdgeRight()

/**全屏占满网格整个一列*/
fun ItemGroupParams.isEdgeVertical(): Boolean = isEdgeTop() && isEdgeBottom()

//</editor-fold desc="网格布局中的边界扩展方法">

//<editor-fold desc="细粒度 分组边界扩展">

//是否在分组4个角上

/**是否在分组左上角*/
fun ItemGroupParams.isEdgeGroupLeftTop(): Boolean = edgeGridParams.run {
    currentSpanParams.spanIndex == 0 && currentSpanParams.spanGroupIndex == firstSpanParams.spanGroupIndex
}

/**是否在分组右上角*/
fun ItemGroupParams.isEdgeGroupRightTop(): Boolean = edgeGridParams.run {
    isEdgeRight() && isEdgeGroupTop()
}

/**是否在分组左下角*/
fun ItemGroupParams.isEdgeGroupLeftBottom(): Boolean = edgeGridParams.run {
    isEdgeLeft() && isEdgeGroupBottom()
}

/**是否在一组中的最后一个位置*/
fun ItemGroupParams.isLastInGroup() = indexInGroup == groupItems.lastIndex

/**是否在分组右下角*/
fun ItemGroupParams.isEdgeGroupRightBottom(): Boolean = edgeGridParams.run {
    isLastInGroup() && isEdgeGroupBottom()
}

/**在一组中的第一行中*/
fun ItemGroupParams.isEdgeGroupTop(): Boolean = edgeGridParams.run {
    currentSpanParams.spanGroupIndex == firstSpanParams.spanGroupIndex
}

/**在一组中的最后一行中*/
fun ItemGroupParams.isEdgeGroupBottom(): Boolean = edgeGridParams.run {
    currentSpanParams.spanGroupIndex == lastSpanParams.spanGroupIndex
}

/**占满整个一行(允许非全屏)*/
fun ItemGroupParams.isEdgeGroupHorizontal(): Boolean =
    (isEdgeGroupLeftTop() && isEdgeGroupRightTop()) || (isEdgeGroupLeftBottom() && isEdgeGroupRightBottom())

/**占满整个一列(允许非全屏)*/
fun ItemGroupParams.isEdgeGroupVertical(): Boolean =
    (isEdgeGroupLeftTop() && isEdgeGroupLeftBottom()) || (isEdgeGroupRightTop() && isEdgeGroupRightBottom())

/**在一组中的第一列上, 支持横竖布局*/
fun ItemGroupParams.isGroupFirstColumn(): Boolean = edgeGridParams.run {
    currentSpanParams.spanIndex == firstSpanParams.spanIndex
}

/**在一组中的最后一列上, 支持横竖布局, 如果这一列未满, 也属于最后一列*/
fun ItemGroupParams.isGroupLastColumn(): Boolean = edgeGridParams.run {
    currentSpanParams.spanIndex == lastSpanParams.spanIndex
}

/**在一组中的第一行上, 支持横竖布局*/
fun ItemGroupParams.isGroupFirstRow(): Boolean = edgeGridParams.run {
    currentSpanParams.spanGroupIndex == firstSpanParams.spanGroupIndex
}

/**在一组中的最后一行上, 支持横竖布局*/
fun ItemGroupParams.isGroupLastRow(): Boolean = edgeGridParams.run {
    currentSpanParams.spanGroupIndex == lastSpanParams.spanGroupIndex
}

//</editor-fold desc="细粒度 分组边界扩展">

