package com.angcyo.acc2.action

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.control.AccControl
import com.angcyo.acc2.control.ControlContext
import com.angcyo.acc2.parse.HandleResult
import com.angcyo.acc2.parse.haveArg

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/04/26
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class PassAction : BaseAction() {

    override fun interceptAction(control: AccControl, action: String): Boolean {
        return action.startsWith(Action.ACTION_PASS)
    }

    override fun runAction(
        control: AccControl,
        controlContext: ControlContext,
        nodeList: List<AccessibilityNodeInfoCompat>?,
        action: String
    ): HandleResult = handleResult {
        success = true

        if (action.haveArg(Action.GROUP)) {
            //跳过整组
            controlContext.action?.let {
                control.accSchedule.nextScheduleActionByGroup(it, it.group)
            }
        } else {
            //跳过当前
        }

        controlContext.log {
            append("Pass[${action}]:${success}")
        }
    }
}