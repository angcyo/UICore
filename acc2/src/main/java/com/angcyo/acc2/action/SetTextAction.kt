package com.angcyo.acc2.action

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.bean.putMap
import com.angcyo.acc2.control.AccControl
import com.angcyo.acc2.control.ControlContext
import com.angcyo.acc2.parse.HandleResult
import com.angcyo.acc2.parse.arg
import com.angcyo.library.ex.subEnd

/**
 * 直接给[com.angcyo.acc2.bean.TaskBean.textMap]赋值
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/05/11
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class SetTextAction : BaseAction() {

    override fun interceptAction(control: AccControl, action: String): Boolean {
        return action.cmd(Action.ACTION_SET_TEXT)
    }

    override fun runAction(
        control: AccControl,
        controlContext: ControlContext,
        nodeList: List<AccessibilityNodeInfoCompat>?,
        action: String
    ): HandleResult = handleResult {
        val key = action.arg(Action.ACTION_SET_TEXT)
        success = !key.isNullOrEmpty()

        if (success) {
            val value = action.subEnd(Action.ARG_SPLIT2)
            control._taskBean?.putMap(key, value)
        }

        controlContext.log {
            append("设置文本[$key]:$success")
        }
    }
}