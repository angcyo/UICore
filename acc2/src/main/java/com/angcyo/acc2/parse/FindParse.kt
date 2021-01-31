package com.angcyo.acc2.parse

import android.view.accessibility.AccessibilityWindowInfo
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.action.Action
import com.angcyo.acc2.bean.FindBean
import com.angcyo.acc2.bean.WindowBean
import com.angcyo.acc2.core.BaseAccService
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

    /**通过*/
    fun parse(findList: List<FindBean>?): FindResult {
        //激活判断
        //准备window
        //准备Context

        val result = FindResult()

        //过滤
        //后处理
        return result
    }

    //<editor-fold desc="window">

    fun findWindowBy(packageName: String?): List<AccessibilityWindowInfo> {
        val result = mutableListOf<AccessibilityWindowInfo>()
        if (!packageName.isNullOrEmpty()) {
            BaseAccService.lastService?.windows?.forEach {
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
                BaseAccService.lastService?.rootInActiveWindow?.let {
                    result.add(it.wrap())
                }
            }
            //如果包名为空字符, 则支持所有window
            packageName.isEmpty() -> {
                BaseAccService.lastService?.windows?.forEach {
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
            }
        }
        return result
    }

    //</editor-fold desc="window">

    //<editor-fold desc="find">

    /**通过文本选择元素*/
    fun findNodeByText(
        originList: List<AccessibilityNodeInfoCompat>,
        findBean: FindBean
    ): List<AccessibilityNodeInfoCompat> {
        val result = mutableListOf<AccessibilityNodeInfoCompat>()
        if (findBean.textList.isNullOrEmpty()) {
            return result
        }
        originList.forEach { rootNode ->
            rootNode.eachChildDepth { node, depth ->
                findBean.textList?.forEachIndexed { index, text ->
                    if (node.haveText(text)) {

                    }
                }
                false
            }
        }
        return result
    }

    //</editor-fold desc="find">

    //<editor-fold desc="match">

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



        return false
    }

    //</editor-fold desc="match">
}