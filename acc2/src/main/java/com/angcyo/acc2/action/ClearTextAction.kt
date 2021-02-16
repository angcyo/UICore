package com.angcyo.acc2.action

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.control.AccControl
import com.angcyo.acc2.control.log
import com.angcyo.acc2.parse.HandleResult
import com.angcyo.acc2.parse.args
import com.angcyo.library.ex.subEnd

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/02/02
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class ClearTextAction : BaseAction() {

    override fun interceptAction(control: AccControl, action: String): Boolean {
        return action.cmd(Action.ACTION_CLEAR_TEXT)
    }

    override fun runAction(
        control: AccControl,
        nodeList: List<AccessibilityNodeInfoCompat>?,
        action: String
    ): HandleResult = handleResult {
        val key = action.subEnd(Action.ARG_SPLIT)

        if (key == Action.ALL) {
            control._taskBean?.textMap = null
            control._taskBean?.textListMap = null
        } else {
            key.args(" ") { index, arg ->
                control._taskBean?.textMap?.remove(arg)
                control._taskBean?.textListMap?.remove(arg)
            }
        }

        success = control._taskBean != null

        control.log("清除文本[$key]:$success")
    }
}