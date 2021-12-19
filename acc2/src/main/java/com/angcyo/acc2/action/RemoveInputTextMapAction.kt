package com.angcyo.acc2.action

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.control.AccControl
import com.angcyo.acc2.control.ControlContext
import com.angcyo.acc2.control.log
import com.angcyo.acc2.parse.HandleResult
import com.angcyo.library.ex.subEnd

/**
 * [com.angcyo.acc2.action.Action.ACTION_REMOVE_INPUT_TEXT_MAP]
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/12/19
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class RemoveInputTextMapAction : BaseAction() {

    override fun interceptAction(control: AccControl, action: String): Boolean {
        return action.cmd(Action.ACTION_REMOVE_INPUT_TEXT_MAP)
    }

    override fun runAction(
        control: AccControl,
        controlContext:  ControlContext,
        nodeList: List<AccessibilityNodeInfoCompat>?,
        action: String
    ): HandleResult = handleResult {
        var key = action.subEnd(Action.ARG_SPLIT)
        if (key.isNullOrEmpty()) {
            key = Action.DEF
        }
        val text = control.accSchedule.removeLastInputText(key)
        success = true

        control.log("移除输入文本[$key][$text]:${success}")
    }
}