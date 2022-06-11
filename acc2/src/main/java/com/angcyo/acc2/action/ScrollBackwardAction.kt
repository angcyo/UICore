package com.angcyo.acc2.action

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.control.AccControl
import com.angcyo.acc2.control.log
import com.angcyo.acc2.parse.HandleResult
import com.angcyo.acc2.parse.arg
import com.angcyo.acc2.parse.toLog
import com.angcyo.library.ex.*

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/02/01
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class ScrollBackwardAction : BaseAction() {

    /**记录一下滚动前第一个child节点的文本信息,
     * 可以用来判断滚动之后界面是否变化了
     * 列表对应的是每一个滚动目标的node
     * */
    val headerNodeTextList = mutableListOf<String>()

    override fun interceptAction(control: AccControl, action: String): Boolean {
        return action.startsWith(Action.ACTION_SCROLL_BACKWARD)
    }

    fun targetNode(
        node: AccessibilityNodeInfoCompat,
        action: String
    ): AccessibilityNodeInfoCompat? {
        return if (action.arg(Action.ACTION_SCROLL_BACKWARD)?.contains(Action.PARENT) == true) {
            node.getScrollableParent()
        } else {
            node
        }
    }

    override fun runAction(
        control: AccControl,
        nodeList: List<AccessibilityNodeInfoCompat>?,
        action: String
    ): HandleResult = handleResult {
        headerNodeTextList.clear()
        nodeList?.forEach { node ->
            headerNodeTextList.add(node.firstChild()?.contactChildText() ?: "")

            //如果滚动到头部了, 会滚动失败
            val targetNode = targetNode(node, action)
            val result = targetNode?.scrollBackward() == true
            success = success || result
            if (result) {
                addNode(targetNode ?: node)
            }
            control.log("向后滚动:$result ↓\n${(targetNode ?: node).toLog(isShowDebug())}")
        }
    }

    var _lastNodeChildText: String? = null

    /**将节点滚动到底部*/
    fun scrollToHeader(node: AccessibilityNodeInfoCompat, wait: Long = 160) {
        _lastNodeChildText = node.contactChildText()
        if (node.scrollBackward()) {
            //滚动成功
            sleep(wait)
            val text = node.contactChildText()
            if (_lastNodeChildText == text) {
                //内容一样, 到底了
                return
            }
            scrollToHeader(node)
        }
    }

    /**滚动后, 节点的文本是否改变*/
    fun isScrollNodeTextChange(oldText: String?): Boolean {
        return headerNodeTextList.lastOrNull() != oldText
    }
}