package com.angcyo.acc2.action

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.control.AccControl
import com.angcyo.acc2.control.ControlContext
import com.angcyo.acc2.control.log
import com.angcyo.acc2.parse.HandleResult
import com.angcyo.acc2.parse.arg
import com.angcyo.library.ex.subEnd

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/12/19
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class SaveInputTextMapAction : BaseAction() {

    override fun interceptAction(control: AccControl, action: String): Boolean {
        return action.cmd(Action.ACTION_SAVE_INPUT_TEXT_MAP)
    }

    override fun runAction(
        control: AccControl,
        controlContext: ControlContext,
        nodeList: List<AccessibilityNodeInfoCompat>?,
        action: String
    ): HandleResult = handleResult {
        val arg = action.subEnd(Action.ARG_SPLIT)

        var key = arg?.arg("key")
        if (key.isNullOrEmpty()) {
            key = Action.DEF
        }

        val textArg = action.arg(Action.ACTION_SAVE_INPUT_TEXT_MAP)

        val textParse = control.accSchedule.accParse.textParse
        val text = textParse.parse(textArg, false).lastOrNull()

        control.accSchedule.saveInputText(text, key)
        success = true

        control.log("保存输入文本[$key][$text]:${success}")
    }
}