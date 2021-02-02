package com.angcyo.acc2.action

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.control.AccControl
import com.angcyo.acc2.control.log
import com.angcyo.acc2.parse.HandleResult
import com.angcyo.acc2.parse.toLog
import com.angcyo.library.ex.getScrollableParent
import com.angcyo.library.ex.isDebug
import com.angcyo.library.ex.scrollForward

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/02/01
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class ScrollForwardAction : BaseAction() {

    override fun interceptAction(control: AccControl, action: String): Boolean {
        return action.startsWith(Action.ACTION_SCROLL_FORWARD)
    }

    override fun runAction(
        control: AccControl,
        nodeList: List<AccessibilityNodeInfoCompat>?,
        action: String
    ): HandleResult = handleResult {
        nodeList?.forEach { node ->
            //如果滚动到底了, 会滚动失败
            val result = node.getScrollableParent()?.scrollForward() == true
            success = success || result
            if (result) {
                addNode(node)
            }
            control.log("向前滚动:$result ↓\n${node.toLog(isDebug())}")
        }
    }
}