package com.angcyo.acc2.action

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.control.AccControl
import com.angcyo.acc2.control.log
import com.angcyo.acc2.parse.HandleResult
import com.angcyo.library.ex.subEnd

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/02/03
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class StopAction : BaseAction() {

    override fun interceptAction(control: AccControl, action: String): Boolean {
        return action.cmd(Action.ACTION_STOP)
    }

    override fun runAction(
        control: AccControl,
        nodeList: List<AccessibilityNodeInfoCompat>?,
        action: String
    ): HandleResult = handleResult {
        val reason = action.subEnd(Action.ARG_SPLIT) ?: "Stop"
        success = true
        control.log("StopAction:$reason")
        control.stop(reason)
    }
}