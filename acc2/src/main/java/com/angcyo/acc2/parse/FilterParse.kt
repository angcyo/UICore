package com.angcyo.acc2.parse

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.action.Action
import com.angcyo.acc2.bean.FilterBean
import com.angcyo.library.ex.haveText
import com.angcyo.library.ex.size

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

        //过滤2: containList
        if (filterBean.containList != null) {
            after = if (removeList.isEmpty()) after else after.filter { !removeList.contains(it) }
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

        //过滤3: notContainList
        if (filterBean.notContainList != null) {
            after = if (removeList.isEmpty()) after else after.filter { !removeList.contains(it) }
            after.forEach { node ->
                accParse.findParse.parse(listOf(node), filterBean.containList).apply {
                    if (success && !nodeList.isNullOrEmpty()) {
                        //包含目标, 不符合过滤条件
                        removeList.add(node)
                    }
                }
            }
        }

        //过滤4: rectList
        if (filterBean.rectList != null) {
            after = if (removeList.isEmpty()) after else after.filter { !removeList.contains(it) }
            after.forEach { node ->
                var match = false
                for (rect in filterBean.rectList!!) {
                    if (accParse.findParse.matchNodeRect(node, rect)) {
                        match = true
                        break
                    }
                }
                if (!match) {
                    //不符合矩形条件
                    removeList.add(node)
                }
            }
        }

        //过滤5: ignoreTextList
        if (filterBean.ignoreTextList != null) {
            after = if (removeList.isEmpty()) after else after.filter { !removeList.contains(it) }
            after.forEach { node ->
                var ignore = false
                for (ignoreText in filterBean.ignoreTextList!!) {
                    val textList = accParse.parseText(ignoreText)
                    if (filterNodeText(node, textList)) {
                        ignore = true
                    }
                    if (ignore) {
                        break
                    }
                }
                if (ignore) {
                    //忽略元素
                    removeList.add(node)
                }
            }
        }

        return removeList
    }

    //</editor-fold desc="filter">

    /**返回是否要过滤掉[node]*/
    fun filterNodeText(node: AccessibilityNodeInfoCompat, textList: List<String>?): Boolean {
        if (textList.isNullOrEmpty()) {
            return false
        }
        var filter = false
        for (text in textList) {
            val list = text.split(Action.TEXT_SPLIT)
            filter = if (list.size() <= 1) {
                filterNodeText(node, text)
            } else {
                filterNodeText(node, list)
            }
            if (filter) {
                break
            }
        }

        return filter
    }

    /**如果节点[node]包含执行文本[text], 则过滤.*/
    fun filterNodeText(node: AccessibilityNodeInfoCompat, text: String?): Boolean {
        if (text.isNullOrEmpty()) {
            return false
        }
        return node.haveText(text)
    }
}