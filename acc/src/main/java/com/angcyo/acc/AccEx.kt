package com.angcyo.acc

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.graphics.Path
import android.graphics.Rect
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityWindowInfo
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.core.component.accessibility.action.AutoParser
import com.angcyo.library.ex.*

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/01/29
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */


fun AccessibilityNodeInfoCompat.getBrotherNodePrev(stateList: List<String>): AccessibilityNodeInfoCompat? {
    val beforeList = mutableListOf<AccessibilityNodeInfoCompat>()
    val afterList = mutableListOf<AccessibilityNodeInfoCompat>()

    var findAnchor = false

    parent?.eachChild { _, child ->
        if (child == this) {
            findAnchor = true
        } else {
            if (findAnchor) {
                afterList.add(child)
            } else {
                beforeList.add(child)
            }
        }
    }

    beforeList.reversed().forEach {
        if (AutoParser.matchNodeStateOr(it, stateList)) {
            return it
        }
    }

    return null
}


fun AccessibilityNodeInfoCompat.getBrotherNodeNext(stateList: List<String>): AccessibilityNodeInfoCompat? {
    val beforeList = mutableListOf<AccessibilityNodeInfoCompat>()
    val afterList = mutableListOf<AccessibilityNodeInfoCompat>()

    var findAnchor = false

    parent?.eachChild { _, child ->
        if (child == this) {
            findAnchor = true
        } else {
            if (findAnchor) {
                afterList.add(child)
            } else {
                beforeList.add(child)
            }
        }
    }

    afterList.forEach {
        if (AutoParser.matchNodeStateOr(it, stateList)) {
            return it
        }
    }

    return null
}

fun AccessibilityNodeInfoCompat.getParentOrChildNodeUp(stateList: List<String>): AccessibilityNodeInfoCompat? {
    var target: AccessibilityNodeInfoCompat? = this
    do {
        val parent = target?.parent ?: return null
        if (AutoParser.matchNodeStateOr(parent, stateList)) {
            return parent
        }
        target = parent
    } while (target != null)
    return null
}

fun AccessibilityNodeInfoCompat.getParentOrChildNodeDown(stateList: List<String>): AccessibilityNodeInfoCompat? {
    forEachChild {
        if (AutoParser.matchNodeStateOr(it, stateList)) {
            return it
        }
    }
    return null
}

fun AccessibilityService.rootNodeInfo(event: AccessibilityEvent? = null): AccessibilityNodeInfo? {
    var maxHeightWindow: AccessibilityWindowInfo? = null
    var maxHeight = 0

    windows.forEach {
        it.getBoundsInScreen(tempRect)
        val height = tempRect.height()
        if (height > maxHeight) {
            maxHeightWindow = it
            maxHeight = height
        }
    }

    val activeWindow = windows.find { it.isActive && it.isFocused }
    return maxHeightWindow?.root ?: (activeWindow?.root ?: event?.source)
}

/**根据给定包名, 获取对应的根节点
 * [packageNameList] 只需要指定的包名, 空表示所有
 *
 * 此方法会带来以下警告:[android.view.accessibility.AccessibilityWindowInfo.getRoot]
 * AccessibilityInteractionClient: old interaction Id is: -1,current interaction Id is:0
 *
 * [onlyTopWindow] 是否只对应包名的顶层window中的节点信息, 否则会获取所有window中的节点信息
 * [ignoreMainWindow] 不抓取主程序的节点信息
 * */
fun AccessibilityService.findNodeInfoList(
    packageNameList: List<String>? = null,
    onlyTopWindow: Boolean = false,
    ignoreMainWindow: Boolean = true
): List<AccessibilityNodeInfo> {
    //需要返回的根节点信息
    val result: MutableList<AccessibilityNodeInfo> = mutableListOf()

    fun addResult(node: AccessibilityNodeInfo) {
        if (!result.contains(node)) {
            if (ignoreMainWindow && node.packageName?.str() == packageName) {
                //忽略主程序
            } else {
                result.add(node)
            }
        }
    }

    if (onlyTopWindow) {
        val topPackageName = windows.lastOrNull()?.root?.packageName
        if (!topPackageName.isNullOrEmpty()) {
            //顶层的应用包名
            windows.forEach { windowInfo ->
                windowInfo.root?.let { root ->
                    if (root.packageName == topPackageName) {
                        addResult(root)
                    }
                }
            }
        }
    } else {
        windows.forEach { windowInfo ->
            windowInfo.root?.let { root ->
                if (packageNameList.isNullOrEmpty() || packageNameList.contains(root.packageName)) {
                    addResult(root)
                }
            }
        }
    }

    rootInActiveWindow?.let { node ->
        if (packageNameList.isNullOrEmpty() || packageNameList.contains(node.packageName)) {
            addResult(node)
        }
    }

//    allNode.sortWith(Comparator { nodeInfo1, nodeInfo2 ->
//        nodeInfo1.getBoundsInScreen(tempRect)
//        val height1 = tempRect.height()
//        nodeInfo2.getBoundsInScreen(tempRect)
//        val height2 = tempRect.height()
//
//        when {
//            height1 < height2 -> 1  //节点高度越小, 放在列表的后面. 使得列表的头部是高度很高的node
//            height1 == height2 -> 0
//            else -> -1
//        }
//    })

    return result
}

/**通过给定的文本, 查找匹配的所有[AccessibilityNodeInfo]*/
fun AccessibilityService.findNodeByText(
    text: String?,
    event: AccessibilityEvent? = null
): List<AccessibilityNodeInfo> {
    val rootNodeInfo = rootNodeInfo(event)
    val nodes = mutableListOf<AccessibilityNodeInfo>()
    rootNodeInfo?.findAccessibilityNodeInfosByText(text)?.let {
        nodes.addAll(it)
    }
    return nodes
}


/**是否有指定的文本对应的[AccessibilityNodeInfo]*/
fun AccessibilityService.haveNode(
    text: String?,
    event: AccessibilityEvent? = null
): Boolean {
    return findNodeByText(text, event).isNotEmpty()
}

fun AccessibilityService.haveNodeOrText(
    text: String?,
    event: AccessibilityEvent? = null
): Boolean {
    if (text.isNullOrEmpty()) {
        return false
    }
    return haveNode(text, event) || event?.haveText(text) ?: false
}

fun AccessibilityService.getLikeText(text: String?): List<CharSequence> {
    val result = mutableListOf<CharSequence>()
    findNodeByText(text).forEach {
        result.add(it.text)
    }
    return result
}

/**
 * [id] id/button1 传入参数.
 * -> button1
 *
 * 自动根据包名补齐id: com.ss.android.ugc.aweme:id/cxw
 * */
fun AccessibilityService.findNodeById(
    id: String,
    event: AccessibilityEvent? = null
): List<AccessibilityNodeInfo> {
    val rootNodeInfo = rootNodeInfo(event)
    val idString = event?.id(id) ?: rootNodeInfo?.id(id)
    val nodes = mutableListOf<AccessibilityNodeInfo>()
    if (idString != null) {
        rootNodeInfo?.findAccessibilityNodeInfosByViewId(idString)?.let {
            nodes.addAll(it)
        }
    }
    return nodes
}


/**返回 文本 node 在屏幕中的 矩形坐标*/
fun AccessibilityService.findRectByText(
    text: String,
    event: AccessibilityEvent? = null
): Array<Rect> {
    val rootNodeInfo = rootNodeInfo(event)

    val nodes = rootNodeInfo?.findAccessibilityNodeInfosByText(text)
    val rectList = mutableListOf<Rect>()

    nodes?.mapIndexed { _, accessibilityNodeInfo ->
        rectList.add(accessibilityNodeInfo.toRect())
    }
    return rectList.toTypedArray()
}


/**
 * id 全路径 "com.xunmeng.pinduoduo:id/ll_tab"
 * 但是 只需要传 ll_tab 就行
 * */
fun AccessibilityService.findRectById(id: String, event: AccessibilityEvent? = null): Array<Rect> {
    val rootNodeInfo = rootNodeInfo(event)

    val idString = event?.id(id) ?: return emptyArray()

    val nodes = rootNodeInfo?.findAccessibilityNodeInfosByViewId(idString)
    val rectList = mutableListOf<Rect>()

    nodes?.mapIndexed { _, accessibilityNodeInfo ->
        val rect = Rect()
        accessibilityNodeInfo.getBoundsInScreen(rect)
        rectList.add(rect)
    }
    return rectList.toTypedArray()
}

/**返回中心点坐标*/
fun AccessibilityService.findPathByText(
    text: String,
    event: AccessibilityEvent? = null
): Array<Path> {
    val rectList = findRectByText(text, event)
    val pathList = mutableListOf<Path>()

    rectList.mapIndexed { _, rect ->
        val path = Path().apply {
            moveTo(rect.centerX().toFloat(), rect.centerY().toFloat())
        }
        pathList.add(path)
    }

    return pathList.toTypedArray()
}

/**给定一组矩形, 返回在屏幕底部的矩形*/
fun Context.findBottomRect(rects: Array<Rect>): Rect {
    var targetRect = Rect()
    val point = displaySize()
    rects.map {
        if (it.centerY() > point.y / 2) {
            if (it.centerY() > targetRect.centerY()) {
                targetRect = it
            }
        }
    }
    return targetRect
}

/**从顶部查询*/
fun Context.findTopRect(rects: Array<Rect>): Rect {
    val point = displaySize()
    var targetRect = Rect(0, 0, point.x, point.y)
    rects.map {
        if (it.centerY() < point.y / 2) {
            if (it.centerY() < targetRect.centerY()) {
                targetRect = it
            }
        }
    }
    return targetRect
}

/**点击文本指定的[AccessibilityNodeInfo]*/
fun AccessibilityService.clickByText(
    text: String?,
    event: AccessibilityEvent? = null
): Boolean {
    val allNodeList = findNodeByText(text, event)
    val allClickNodeList = mutableListOf<AccessibilityNodeInfo>()

    allNodeList.forEach {
        if (it.isClickable) {
            allClickNodeList.add(it)
        }
    }

    return allClickNodeList.firstOrNull()?.run {
        click()
    } ?: false
}

/**点击文本指定的[AccessibilityNodeInfo]*/
fun AccessibilityService.clickById(
    id: String,
    event: AccessibilityEvent? = null
): Boolean {
    val allNodeList = findNodeById(id, event)
    val allClickNodeList = mutableListOf<AccessibilityNodeInfo>()

    allNodeList.forEach {
        if (it.isClickable) {
            allClickNodeList.add(it)
        }
    }

    return allClickNodeList.firstOrNull()?.run {
        click()
    } ?: false
}


fun AccessibilityEvent.eventTypeStr(): String = AccessibilityEvent.eventTypeToString(eventType)

fun AccessibilityEvent.contentChangeTypesStr(): String = when (contentChangeTypes) {
    AccessibilityEvent.CONTENT_CHANGE_TYPE_CONTENT_DESCRIPTION -> "CONTENT_CHANGE_TYPE_CONTENT_DESCRIPTION"
    AccessibilityEvent.CONTENT_CHANGE_TYPE_SUBTREE -> "CONTENT_CHANGE_TYPE_SUBTREE"
    AccessibilityEvent.CONTENT_CHANGE_TYPE_TEXT -> "CONTENT_CHANGE_TYPE_TEXT"
    AccessibilityEvent.CONTENT_CHANGE_TYPE_PANE_TITLE -> "CONTENT_CHANGE_TYPE_PANE_TITLE"
    AccessibilityEvent.CONTENT_CHANGE_TYPE_UNDEFINED -> "CONTENT_CHANGE_TYPE_UNDEFINED"
    AccessibilityEvent.CONTENT_CHANGE_TYPE_PANE_APPEARED -> "CONTENT_CHANGE_TYPE_PANE_APPEARED"
    AccessibilityEvent.CONTENT_CHANGE_TYPE_PANE_DISAPPEARED -> "CONTENT_CHANGE_TYPE_PANE_DISAPPEARED"
    else -> Integer.toHexString(contentChangeTypes)
}

fun AccessibilityService.findNode(predicate: (node: AccessibilityNodeInfoCompat) -> Boolean) {
    rootNodeInfo()?.findNode(predicate = {
        if (predicate(it)) {
            -2
        } else {
            -1
        }
    })
}