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
class ScrollForwardAction : BaseAction() {

    /**记录一下滚动前最后一个child节点的文本信息,
     * 可以用来判断滚动之后界面是否变化了
     * 列表对应的是每一个滚动目标的node
     * */
    val footerNodeTextList = mutableListOf<String>()

    override fun interceptAction(control: AccControl, action: String): Boolean {
        return action.startsWith(Action.ACTION_SCROLL_FORWARD)
    }

    fun targetNode(
        node: AccessibilityNodeInfoCompat,
        action: String
    ): AccessibilityNodeInfoCompat? {
        return if (action.arg(Action.ACTION_SCROLL_FORWARD)?.contains(Action.PARENT) == true) {
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
        footerNodeTextList.clear()
        nodeList?.forEach { node ->
            footerNodeTextList.add(node.lastChild()?.contactChildText() ?: "")

            //如果滚动到底了, 会滚动失败
            val targetNode = targetNode(node, action)
            val result = targetNode?.scrollForward() == true
            success = success || result
            if (result) {
                addNode(targetNode ?: node)
            }
            control.log("向前滚动:$result ↓\n${(targetNode ?: node).toLog(isShowDebug())}")
        }
    }

    var _lastNodeChildText: String? = null

    /**将节点滚动到底部*/
    fun scrollToFooter(node: AccessibilityNodeInfoCompat, wait: Long = 160) {
        _lastNodeChildText = node.contactChildText()
        if (node.scrollForward()) {
            //滚动成功
            sleep(wait)
            val text = node.contactChildText()
            if (_lastNodeChildText == text) {
                //内容一样, 到底了
                return
            }
            scrollToFooter(node)
        }
    }

    /**滚动后, 节点的文本是否改变*/
    fun isScrollNodeTextChange(oldText: String?): Boolean {
        return footerNodeTextList.lastOrNull() != oldText
    }
}