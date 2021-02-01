package com.angcyo.acc2.parse

import android.view.accessibility.AccessibilityWindowInfo
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.action.Action
import com.angcyo.acc2.bean.FindBean
import com.angcyo.acc2.bean.WindowBean
import com.angcyo.acc2.eachChildDepth
import com.angcyo.library.ex.*


/**
 * 查找元素
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/01/30
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class FindParse(val accParse: AccParse) {

    //<editor-fold desc="parse">

    /**通过一组规则, 查找满足规则的节点集合, 就终止*/
    fun parse(
        rootList: List<AccessibilityNodeInfoCompat>?,
        findList: List<FindBean>?
    ): FindResult {
        //激活判断

        //准备Context

        var result = FindResult()

        if (!findList.isNullOrEmpty()) {
            for (findBean in findList) {
                val parseResult = parse(rootList, findBean)
                if (parseResult.success) {
                    //匹配成功, 中断查询, 提升效率
                    result = parseResult
                    break
                }
            }
        }

        return result
    }

    /**返回满足规则的节点集合*/
    fun parse(rootList: List<AccessibilityNodeInfoCompat>?, findBean: FindBean): FindResult {
        val result = FindResult()

        //准备window

        //根节点选择
        val rootNodeList = if (findBean.window == null) {
            rootList
        } else {
            val accSchedule = accParse.accControl.accSchedule
            findRootNode(
                findBean.window
                    ?: accSchedule._runActionBean?.window
                    ?: accSchedule._currentActionBean?.window
            )
        }

        if (rootNodeList.isNullOrEmpty()) {
            return result
        }

        //-----------------------选择元素------------------------

        //找到的元素
        val findNodeList = mutableListOf<AccessibilityNodeInfoCompat>()

        val text = findBean.textList != null
        val cls = findBean.clsList != null
        val id = findBean.idList != null
        val rect = findBean.rectList != null
        val state = findBean.stateList != null

        when {
            text -> findNodeList.addAll(findNode(rootNodeList, findBean, findBean.textList))
            cls -> findNodeList.addAll(findNode(rootNodeList, findBean, findBean.clsList))
            id -> findNodeList.addAll(findNode(rootNodeList, findBean, findBean.idList))
            rect -> findNodeList.addAll(findNode(rootNodeList, findBean, findBean.rectList))
            state -> findNodeList.addAll(findNode(rootNodeList, findBean, findBean.stateList))
            //空的选择器
            else -> findNodeList.addAll(rootNodeList)
        }

        //------------------------后处理-------------------------

        //需要过滤
        if (findBean.filter != null) {
            findNodeList.removeAll(accParse.filterParse.parse(findNodeList, findBean.filter))
        }

        //递归处理
        val after = findBean.after
        var afterResult: FindResult? = null
        if (after != null) {
            afterResult = parse(findNodeList, after)
        }

        //-----------------------返回结果--------------------------

        return afterResult ?: result.apply {
            success = findNodeList.isNotEmpty()
            nodeList = findNodeList
            if (success) {
                this.findBean = findBean
            }
        }
    }

    /**检查是否需要中断枚举查找元素*/
    fun checkFindLimit(nodeList: List<AccessibilityNodeInfoCompat>?, depth: Int): Boolean {
        var result = if (accParse.accContext.findLimit >= 0) {
            nodeList.size() >= accParse.accContext.findLimit
        } else {
            false
        }

        if (!result) {
            //没有超限, 第二层判断
            result = if (accParse.accContext.findDepth >= 0) {
                depth >= accParse.accContext.findDepth
            } else {
                false
            }
        }

        return result
    }

    //</editor-fold desc="parse">

    //<editor-fold desc="window">

    fun findWindowBy(packageName: String?): List<AccessibilityWindowInfo> {
        val result = mutableListOf<AccessibilityWindowInfo>()
        if (!packageName.isNullOrEmpty()) {
            accParse.accControl.accService()?.windows?.forEach {
                if (it.root?.packageName?.have(packageName) == true) {
                    result.add(it)
                }
            }
        }
        return result
    }

    fun findRootNodeBy(packageName: String?): List<AccessibilityNodeInfoCompat>? {
        val result = mutableListOf<AccessibilityNodeInfoCompat>()
        when {
            //无包名字段, 则使用活动窗口
            packageName == null -> {
                accParse.accControl.accService()?.rootInActiveWindow?.let {
                    result.add(it.wrap())
                }
            }
            //如果包名为空字符, 则支持所有window
            packageName.isEmpty() -> {
                accParse.accControl.accService()?.windows?.forEach {
                    it.root?.let { node -> result.add(node.wrap()) }
                }
            }
            else -> {
                findWindowBy(packageName).forEach { window -> result.add(window.root.wrap()) }
            }
        }
        return result
    }

    /**根据[WindowBean]的描述, 获取根节点集合*/
    fun findRootNode(windowBean: WindowBean?): List<AccessibilityNodeInfoCompat>? {
        val result = mutableListOf<AccessibilityNodeInfoCompat>()
        if (windowBean == null) {
            //未指定, 采用默认处理
            val taskPackageName = accParse.accControl._taskBean?.packageName
            return findRootNodeBy(taskPackageName)
        } else {
            //解析window约束
            if (windowBean.packageName != null) {
                //指定了要重新获取根节点
                result.addAll(findRootNodeBy(windowBean.packageName) ?: emptyList())
            } else {
                //活动节点
                accParse.accControl.accService()?.rootInActiveWindow?.let {
                    result.add(it.wrap())
                }
            }
        }
        return result
    }

    //</editor-fold desc="window">

    //<editor-fold desc="find">

    /**查找节点*/
    fun findNode(
        originList: List<AccessibilityNodeInfoCompat>,
        findBean: FindBean,
        list: List<String?>?
    ): List<AccessibilityNodeInfoCompat> {
        val result = mutableListOf<AccessibilityNodeInfoCompat>()
        if (list.isNullOrEmpty()) {
            return result
        }
        originList.forEach { rootNode ->
            rootNode.eachChildDepth { node, depth ->
                list.forEachIndexed { index, _ ->
                    if (matchNode(node, findBean, index)) {
                        result.add(node)
                        if (checkFindLimit(result, depth)) {
                            return@eachChildDepth true
                        }
                    }
                }
                false
            }
        }
        return result
    }

    //</editor-fold desc="find">

    //<editor-fold desc="match">

    /**节点必须要满足的条件*/
    fun matchNode(node: AccessibilityNodeInfoCompat, findBean: FindBean, index: Int): Boolean {
        return matchNodeText(node, findBean.textList?.getOrNull(index)) &&
                matchNodeClass(node, findBean.clsList?.getOrNull(index)) &&
                matchNodeId(node, findBean.idList?.getOrNull(index)) &&
                matchNodeState(node, findBean.stateList?.getOrNull(index)) &&
                matchNodeRect(node, findBean.rectList?.getOrNull(index))
    }

    /**检查节点是否满足text*/
    fun matchNodeText(node: AccessibilityNodeInfoCompat, text: String?): Boolean {
        if (text.isNullOrBlank()) {
            return true
        }
        return node.haveText(text)
    }

    /**检查节点是否满足cls*/
    fun matchNodeClass(node: AccessibilityNodeInfoCompat, cls: String?): Boolean {
        if (cls.isNullOrBlank()) {
            return true
        }
        return node.className.have(cls)
    }

    /**检查节点是否满足id*/
    fun matchNodeId(node: AccessibilityNodeInfoCompat, id: String?): Boolean {
        if (id.isNullOrBlank()) {
            return true
        }
        return node.viewIdName() == node.packageName.id(id)
    }

    /**检查节点是否满足state*/
    fun matchNodeState(node: AccessibilityNodeInfoCompat, state: String?): Boolean {
        if (state.isNullOrBlank()) {
            return true
        }
        var match = true
        when (state) {
            Action.STATE_CLICKABLE -> {
                //需要具备可以点击的状态
                if (!node.isClickable) {
                    match = false
                }
            }
            Action.STATE_UN_CLICKABLE -> {
                //需要具备不可以点击的状态
                if (node.isClickable) {
                    match = false
                }
            }
            Action.STATE_FOCUSABLE -> {
                //需要具备可以获取焦点状态
                if (!node.isFocusable) {
                    match = false
                }
            }
            Action.STATE_UN_FOCUSABLE -> {
                //需要具备不可以获取焦点状态
                if (node.isFocusable) {
                    match = false
                }
            }
            Action.STATE_FOCUSED -> {
                //需要具备焦点状态
                if (!node.isFocused) {
                    match = false
                }
            }
            Action.STATE_UNFOCUSED -> {
                //需要具备无焦点状态
                if (node.isFocused) {
                    match = false
                }
            }
            Action.STATE_SELECTED -> {
                //需要具备选中状态
                if (!node.isSelected) {
                    match = false
                }
            }
            Action.STATE_UNSELECTED -> {
                //需要具备不选中状态
                if (node.isSelected) {
                    match = false
                }
            }
            Action.STATE_SCROLLABLE -> {
                //需要具备可滚动状态
                if (!node.isScrollable) {
                    match = false
                }
            }
            Action.STATE_UN_SCROLLABLE -> {
                //需要具备不可滚动状态
                if (node.isScrollable) {
                    match = false
                }
            }
            Action.STATE_LONG_CLICKABLE -> {
                //需要具备可以长按的状态
                if (!node.isLongClickable) {
                    match = false
                }
            }
            Action.STATE_UN_LONG_CLICKABLE -> {
                //需要具备不可以长按的状态
                if (node.isLongClickable) {
                    match = false
                }
            }
            Action.STATE_ENABLE -> {
                //需要具备激活状态
                if (!node.isEnabled) {
                    match = false
                }
            }
            Action.STATE_DISABLE -> {
                //需要具备禁用状态
                if (node.isEnabled) {
                    match = false
                }
            }
            Action.STATE_PASSWORD -> {
                //需要具备密码状态
                if (!node.isPassword) {
                    match = false
                }
            }
            Action.STATE_UN_PASSWORD -> {
                //需要具备非密码状态
                if (node.isPassword) {
                    match = false
                }
            }
            Action.STATE_CHECKABLE -> {
                if (!node.isCheckable) {
                    match = false
                }
            }
            Action.STATE_UN_CHECKABLE -> {
                if (node.isCheckable) {
                    match = false
                }
            }
            Action.STATE_CHECKED -> {
                if (!node.isChecked) {
                    match = false
                }
            }
            Action.STATE_UNCHECKED -> {
                if (node.isChecked) {
                    match = false
                }
            }
        }
        return match
    }

    /**检查节点是否满足rect*/
    fun matchNodeRect(node: AccessibilityNodeInfoCompat, rect: String?): Boolean {
        if (rect == null) {
            //空对象
            return true
        }
        val bound = node.bounds()
        if (rect.isEmpty()) {
            //空字符, 只要节点有大小即可
            return !bound.isEmpty
        }

        return accParse.rectParse.parse(rect, bound)
    }

    //</editor-fold desc="match">
}