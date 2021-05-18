package com.angcyo.acc2.action

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.control.AccControl
import com.angcyo.acc2.control.ControlContext
import com.angcyo.acc2.parse.HandleResult
import com.angcyo.acc2.parse.arg
import com.angcyo.library.utils.getLongNumList

/**
 * 计数
 * [com.angcyo.acc2.control.AccSchedule.countMap]
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/05/18
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class CountAction : BaseAction() {

    override fun interceptAction(control: AccControl, action: String): Boolean {
        return action.cmd(Action.ACTION_COUNT)
    }

    override fun runAction(
        control: AccControl,
        controlContext: ControlContext,
        nodeList: List<AccessibilityNodeInfoCompat>?,
        action: String
    ): HandleResult = handleResult {
        val key =
            action.arg(Action.ACTION_COUNT) ?: controlContext.action?.actionId?.toString() ?: action
        val clear = action.contains(Action.CLEAR)

        val countMap = control.accSchedule.countMap
        if (clear) {
            countMap.remove(key)
        } else {
            action.getLongNumList()?.forEach {
                val count = countMap[key] ?: 0
                countMap[key] = count + it
            }
        }

        success = true

        controlContext.log {
            append("计数[$action]:$success↓\n${countMap}")
        }
    }
}