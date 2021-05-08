package com.angcyo.acc2.action

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.control.AccControl
import com.angcyo.acc2.control.log
import com.angcyo.acc2.parse.HandleResult

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/02/16
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class ClearRunTimeAction : BaseClearAction() {

    override fun interceptAction(control: AccControl, action: String): Boolean {
        return action.startsWith(Action.ACTION_CLEAR_RUN_TIME)
    }

    override fun runAction(
        control: AccControl,
        nodeList: List<AccessibilityNodeInfoCompat>?,
        action: String
    ): HandleResult = handleResult {

        val clearActionIdList = getClearActionIdList(control, action)

        success = clearRunTime(control, clearActionIdList)

        control.log("清理运行时长[${clearActionIdList}]:${success}")
    }
}