package com.angcyo.acc2.action

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.control.AccControl
import com.angcyo.acc2.control.log
import com.angcyo.acc2.parse.HandleResult
import com.angcyo.acc2.parse.arg
import com.angcyo.library.component.MainExecutor

/**
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/10/26
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class NotTouchableAction : BaseAction() {

    var notTouchableAction: (Boolean) -> Boolean = { false }

    override fun interceptAction(control: AccControl, action: String): Boolean {
        return action.startsWith(Action.ACTION_NOT_TOUCHABLE)
    }

    override fun runAction(
        control: AccControl,
        nodeList: List<AccessibilityNodeInfoCompat>?,
        action: String
    ): HandleResult = handleResult {
        val arg = action.arg(Action.ACTION_NOT_TOUCHABLE)
        val wait = action.arg("wait")?.toLongOrNull() ?: 0
        val async = action.contains(Action.ASYNC)

        val notTouchable = if (arg.isNullOrEmpty()) {
            control._taskBean?.notTouchable ?: true
        } else {
            arg == "true"
        }

        success = if (async) {
            MainExecutor.delay({
                notTouchableAction(notTouchable)
            }, wait)
            true
        } else {
            notTouchableAction(notTouchable)
        }
        control.log("设置浮窗不接受Touch事件[${arg}]:$success")
    }
}