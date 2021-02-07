package com.angcyo.acc2.action

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.control.AccControl
import com.angcyo.acc2.control.log
import com.angcyo.acc2.parse.HandleResult
import com.angcyo.library.ex.subEnd

/**
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/09/21
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class HideWindowAction : BaseAction() {

    var hideWindowAction: (time: Long, count: Long) -> Boolean = { _, _ -> false }

    override fun interceptAction(control: AccControl, action: String): Boolean {
        return action.startsWith(Action.ACTION_HIDE_WINDOW)
    }

    override fun runAction(
        control: AccControl,
        nodeList: List<AccessibilityNodeInfoCompat>?,
        action: String
    ): HandleResult = handleResult {
        val arg = action.subEnd(Action.ARG_SPLIT)
        val time = arg?.toLongOrNull()

        if (time == null) {
            success = hideWindowAction(-1, -1)
        } else {
            val actionSize = control.accSchedule.actionSize()
            if (time in 1..actionSize.toLong()) {
                success = hideWindowAction(-1, time)

                control.log("隐藏浮窗Count[${time}]:$success")
            } else {
                //指定需要隐藏的时长, 毫秒
                success = hideWindowAction(time, -1)
                control.log("隐藏浮窗Time[${time}]:$success")
            }
        }
    }
}