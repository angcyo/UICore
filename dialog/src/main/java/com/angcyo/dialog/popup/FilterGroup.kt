package com.angcyo.dialog.popup

/**
 * 过滤分组
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/12/14
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class FilterGroup {

    /**分组的名字*/
    var groupName: CharSequence? = null

    /**布局id, 如果为[null], 则使用[com.angcyo.dialog.popup.FilterPopupConfig.filterGroupLayout]中的值*/
    var groupLayoutId: Int? = null

    /**具体的过滤选项, 存放所有*/
    var groupFilterItemList: List<FilterGroupItem>? = null

    /**自定义数据存储*/
    var groupTag: Any? = null

    /**单独存放选中后的item列表*/
    var _groupFilterSelectedItemList: List<FilterGroupItem>? = null

    /**当前组, 是否选中全部, 标识*/
    var _groupSelectAll: Boolean = false
}

open class FilterGroupItem {

    /**布局id, 如果为[null], 则使用[com.angcyo.dialog.popup.FilterPopupConfig.filterItemLayout]中的值*/
    var itemLayoutId: Int? = null

    /**具体的过滤选项
     * [IToValue] [IToText] */
    var itemData: Any? = null

    /**是否选中*/
    var isSelected: Boolean = false

    /**在分组中的索引*/
    var _groupIndex: Int = -1
}

class FilterGroupItemAll : FilterGroupItem() {
    init {
        itemData = "全部"
        isSelected = true
    }
}

fun List<Any>.toFilterGroupItem(needSelectAll: Boolean = false): List<FilterGroupItem> {
    val result = mutableListOf<FilterGroupItem>()
    if (needSelectAll) {
        result.add(FilterGroupItemAll())
    }
    forEach {
        result.add(FilterGroupItem().apply {
            itemData = it
        })
    }
    return result
}