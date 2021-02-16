package com.angcyo.acc2.action

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.control.AccControl
import com.angcyo.acc2.control.log
import com.angcyo.acc2.parse.HandleResult

/**
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/16
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class DisableHandleAction : BaseAction() {

    override fun interceptAction(control: AccControl, action: String): Boolean {
        return action.cmd(Action.ACTION_DISABLE_HANDLE)
    }

    override fun runAction(
        control: AccControl,
        nodeList: List<AccessibilityNodeInfoCompat>?,
        action: String
    ): HandleResult = handleResult {
        success = true
        control.log("禁用Handle:$success")
    }
}