package com.angcyo.acc2.action

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.control.AccControl
import com.angcyo.acc2.control.log
import com.angcyo.acc2.parse.HandleResult
import com.angcyo.acc2.parse.arg
import com.angcyo.acc2.parse.toLog
import com.angcyo.library.ex.getLongClickParent
import com.angcyo.library.ex.isDebug
import com.angcyo.library.ex.isShowDebug
import com.angcyo.library.ex.longClick

/**
 * 长按节点
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/09/21
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class LongClickAction : BaseAction() {

    override fun interceptAction(control: AccControl, action: String): Boolean {
        return action.startsWith(Action.ACTION_LONG_CLICK)
    }

    fun targetNode(
        node: AccessibilityNodeInfoCompat,
        action: String
    ): AccessibilityNodeInfoCompat? {
        return if (action.arg(Action.ACTION_LONG_CLICK)?.contains(Action.NOT_PARENT) == true) {
            node
        } else {
            node.getLongClickParent()
        }
    }

    override fun runAction(
        control: AccControl,
        nodeList: List<AccessibilityNodeInfoCompat>?,
        action: String
    ): HandleResult = handleResult {
        nodeList?.forEach { node ->
            val result = targetNode(node, action)?.longClick() ?: false
            success = result || success
            control.log("长按节点:$result ↓\n${node.toLog(isShowDebug())}")
        }
    }
}