package com.angcyo.acc2.parse

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.bean.FilterBean
import com.angcyo.acc2.eachChildDepth
import com.angcyo.library.ex.haveText

/**
 * 过滤解析器
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/01/30
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class FilterParse(val accParse: AccParse) : BaseParse() {

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

        //剩余节点
        var after: List<AccessibilityNodeInfoCompat> = originList
        //需要移除的节点
        val removeList = mutableListOf<AccessibilityNodeInfoCompat>()

        //过滤1: index
        val indexString = filterBean.index
        if (indexString != null) {
            val index = accParse.textParse.parseOrDef(indexString).firstOrNull()
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
                    val textList = accParse.textParse.parse(ignoreText)
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

        //过滤6: haveTextList
        if (filterBean.haveTextList != null) {
            after = if (removeList.isEmpty()) after else after.filter { !removeList.contains(it) }
            after.forEach { node ->
                var ignore = false
                for (text in filterBean.haveTextList!!) {
                    val textList = accParse.textParse.parse(text)
                    if (!nodeHaveText(node, textList)) {
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

        //过滤7: childCount
        if (filterBean.childCount != null) {
            after = if (removeList.isEmpty()) after else after.filter { !removeList.contains(it) }
            after.forEach { node ->
                val pass = accParse.expParse.parseAndCompute(
                    filterBean.childCount,
                    inputValue = node.childCount.toFloat()
                )
                if (!pass) {
                    //不符合
                    removeList.add(node)
                }
            }
        }

        //过滤8: sizeCount
        if (filterBean.sizeCount != null) {
            after = if (removeList.isEmpty()) after else after.filter { !removeList.contains(it) }
            after.forEach { node ->

                val allNode = filterBean.sizeCount?.contains("*") == true
                val sizeCountExp = filterBean.sizeCount?.replaceFirst("*", "")

                //无子节点的节点总数
                var childSize = 0
                //是否中断枚举
                var interrupt = false
                //node是否满足条件
                var allow = true

                node.eachChildDepth { child, depth ->
                    interrupt = false
                    if (allNode || child.childCount == 0) {
                        //空节点
                        childSize++

                        val pass = accParse.expParse.parseAndCompute(
                            sizeCountExp,
                            inputValue = childSize.toFloat()
                        )

                        allow = pass

                        if (sizeCountExp?.startsWith("<") == true) {
                            allow = true
                            //如果是小于运算符, 则没有匹配到时,才退出

                            if (!pass) {
                                allow = false
                                interrupt = true
                            }
                        } else if (pass) {
                            //其他运算符, 匹配到时, 则退出
                            allow = true
                            interrupt = true
                        }
                    }
                    interrupt
                }

                if (!allow) {
                    //不满足节点总数约束条件
                    removeList.add(node)
                }
            }
        }

        //------------------------后处理-------------------------

        if (filterBean.after != null) {
            after = if (removeList.isEmpty()) after else after.filter { !removeList.contains(it) }
            removeList.addAll(parse(after, filterBean.after))
        }

        return removeList
    }

    //</editor-fold desc="filter">

    /**
     * 如果节点包含[textList]中的一项,则过滤节点
     * 返回是否要过滤掉[node]*/
    fun filterNodeText(node: AccessibilityNodeInfoCompat, textList: List<String?>?): Boolean {
        if (textList.isNullOrEmpty()) {
            return false
        }
        var filter = false
        for (text in textList) {
            /*val list = text?.split(Action.TEXT_SPLIT)
            filter = if (list.size() <= 1) {
                filterNodeText(node, text)
            } else {
                filterNodeText(node, list)
            }*/
            filter = filterNodeText(node, text)
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

    /**节点是否包含指定的文本
     * [textList]必须全部包含*/
    fun nodeHaveText(node: AccessibilityNodeInfoCompat, textList: List<String?>?): Boolean {
        if (textList == null) {
            return false
        }
        if (textList.isEmpty()) {
            return true
        }
        var have = false
        for (text in textList) {
            have = nodeHaveText(node, text)
            if (!have) {
                break
            }
        }

        return have
    }

    /**如果节点[node]包含文本[text], 则返回true*/
    fun nodeHaveText(node: AccessibilityNodeInfoCompat, text: String?): Boolean {
        if (text == null) {
            return false
        }
        if (text.isEmpty()) {
            return true
        }
        return node.haveText(text)
    }
}