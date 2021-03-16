package com.angcyo.acc2.action

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.control.AccControl
import com.angcyo.acc2.control.log
import com.angcyo.acc2.parse.HandleResult
import com.angcyo.library.ex.subEnd
import com.angcyo.library.toastQQ
import com.angcyo.library.toastWX

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/02/01
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class ToastAction : BaseAction() {

    override fun interceptAction(control: AccControl, action: String): Boolean {
        return action.startsWith(Action.ACTION_TOAST)
    }

    override fun runAction(
        control: AccControl,
        nodeList: List<AccessibilityNodeInfoCompat>?,
        action: String
    ): HandleResult = handleResult {
        success = true
        val textParse = control.accSchedule.accParse.textParse
        val message = textParse.parse(action.subEnd(Action.ARG_SPLIT), true).firstOrNull()
        if (action.startsWith(Action.ACTION_TOAST_WX)) {
            toastWX(message)
        } else {
            toastQQ(message)
        }
        control.log("Toast[${message}]:$success")
    }
}