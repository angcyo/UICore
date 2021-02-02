package com.angcyo.acc2.action

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.control.AccControl
import com.angcyo.acc2.control.log
import com.angcyo.acc2.parse.HandleResult
import com.angcyo.acc2.parse.toLog
import com.angcyo.library.ex.focus
import com.angcyo.library.ex.getFocusableParent
import com.angcyo.library.ex.isDebug

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/02/02
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class FocusAction : BaseAction() {

    override fun interceptAction(control: AccControl, action: String): Boolean {
        return action.startsWith(Action.ACTION_FOCUS)
    }

    override fun runAction(
        control: AccControl,
        nodeList: List<AccessibilityNodeInfoCompat>?,
        action: String
    ): HandleResult = handleResult {
        nodeList?.forEach { node ->
            val result = node.getFocusableParent()?.focus() == true
            success = success || result
            if (result) {
                addNode(node)
            }
            control.log("设置焦点:$result ↓\n${node.toLog(isDebug())}")
        }
    }
}