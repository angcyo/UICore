package com.angcyo.acc2.action

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.control.AccControl
import com.angcyo.acc2.control.log
import com.angcyo.acc2.parse.HandleResult
import com.angcyo.library.ex.sleep
import com.angcyo.library.ex.subEnd

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/02/01
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class SleepAction : BaseAction() {

    override fun interceptAction(control: AccControl, action: String): Boolean {
        return action.startsWith(Action.ACTION_SLEEP)
    }

    override fun runAction(
        control: AccControl,
        nodeList: List<AccessibilityNodeInfoCompat>?,
        action: String
    ): HandleResult = handleResult {
        success = true
        val arg = action.subEnd(Action.ARG_SPLIT)
        val argText = control.accSchedule.accParse.textParse.parse(arg).firstOrNull()
        val parseTime = control.accSchedule.accParse.parseTime(argText)
        control.log("休眠[${parseTime}]:$success")
        sleep(parseTime)
    }
}