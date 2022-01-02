package com.angcyo.acc2.action

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.control.AccControl
import com.angcyo.acc2.control.ControlContext
import com.angcyo.acc2.control.log
import com.angcyo.acc2.parse.HandleResult
import com.angcyo.acc2.parse.arg

/**
 * 事件通知
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/01/02
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class EventAction : BaseAction() {

    init {
        ignoreResult = true
    }

    override fun interceptAction(control: AccControl, action: String): Boolean {
        return action.cmd(Action.ACTION_EVENT)
    }

    override fun runAction(
        control: AccControl,
        controlContext: ControlContext,
        nodeList: List<AccessibilityNodeInfoCompat>?,
        action: String
    ): HandleResult = handleResult {
        success = true
        val event = action.arg(Action.ACTION_EVENT)
        control.log("Event[${event}]:$success")
        control.onActionEvent(control, controlContext, nodeList, action, event ?: "")
    }

}