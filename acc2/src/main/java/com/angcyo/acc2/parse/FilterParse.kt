package com.angcyo.acc2.parse

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.bean.FilterBean

/**
 * 过滤解析器
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/01/30
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class FilterParse(val accParse: AccParse) {

    //<editor-fold desc="filter">

    /**
     * 通过过滤条件[filterBean], 过滤数组[originList]
     * 返回需要被移除的节点集合
     * */
    fun parse(
        originList: List<AccessibilityNodeInfoCompat>?,
        filterBean: FilterBean?
    ): List<AccessibilityNodeInfoCompat> {
        if (filterBean == null || originList.isNullOrEmpty()) {
            return emptyList()
        }

        var after: List<AccessibilityNodeInfoCompat> = originList
        val removeList = mutableListOf<AccessibilityNodeInfoCompat>()

        //过滤1: index
        val indexString = filterBean.index
        if (indexString != null) {
            val index = accParse.parseText(indexString).firstOrNull()
            originList.eachRangeItem(index) { item, isIn ->
                if (!isIn) {
                    removeList.add(item)
                }
            }
        }
        after = if (removeList.isEmpty()) after else after.filter { !removeList.contains(it) }

        //过滤2: containList
        if (filterBean.containList != null) {
            after.forEach { node ->
                accParse.findParse.parse(listOf(node), filterBean.containList).apply {
                    if (success && !nodeList.isNullOrEmpty()) {
                        //解析成功
                    } else {
                        //不包含目标, 不符合过滤条件
                        removeList.add(node)
                    }
                }
            }
        }
        after = if (removeList.isEmpty()) after else after.filter { !removeList.contains(it) }

        //过滤筛选步骤3: notContainList
        if (filterBean.notContainList != null) {
            after.forEach { node ->
                accParse.findParse.parse(listOf(node), filterBean.containList).apply {
                    if (success && !nodeList.isNullOrEmpty()) {
                        //包含目标, 不符合过滤条件
                        removeList.add(node)
                    }
                }
            }
        }
        //after = if (removeList.isEmpty()) after else after.filter { !removeList.contains(it) }

        return removeList
    }

    //</editor-fold desc="filter">
}