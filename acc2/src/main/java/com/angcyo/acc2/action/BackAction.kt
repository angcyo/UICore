package com.angcyo.acc2.action

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.control.AccControl
import com.angcyo.acc2.control.log
import com.angcyo.acc2.parse.HandleResult
import com.angcyo.library.ex.back
import com.angcyo.library.ex.nowTime
import com.angcyo.library.ex.subEnd

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/02/01
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class BackAction : BaseAction() {

    var lastBackTime = 0L

    override fun interceptAction(control: AccControl, action: String): Boolean {
        return action.startsWith(Action.ACTION_BACK)
    }

    override fun runAction(
        control: AccControl,
        nodeList: List<AccessibilityNodeInfoCompat>?,
        action: String
    ): HandleResult = handleResult {
        val arg = action.subEnd(Action.ARG_SPLIT)
        if (arg.isNullOrEmpty()) {
            success = control.accService()?.back() == true
            control.log("[BackAction]返回:$success")
        } else {
            val nowTime = nowTime()
            val parseAndCompute = control.accSchedule.accParse.expParse.parseAndCompute(
                arg,
                inputValue = (nowTime - lastBackTime).toFloat()
            )
            if (parseAndCompute) {
                success = control.accService()?.back() == true
                lastBackTime = nowTime
            }
            control.log("[BackAction][$arg]返回:$success")
        }
    }
}