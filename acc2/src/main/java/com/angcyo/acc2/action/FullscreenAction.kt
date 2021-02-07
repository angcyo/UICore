package com.angcyo.acc2.action

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.control.AccControl
import com.angcyo.acc2.control.log
import com.angcyo.acc2.parse.HandleResult
import com.angcyo.library.ex.subEnd

/**
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/10/20
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class FullscreenAction : BaseAction() {

    var fullscreenAction: (Boolean) -> Boolean = { false }

    override fun interceptAction(control: AccControl, action: String): Boolean {
        return action.startsWith(Action.ACTION_FULLSCREEN)
    }

    override fun runAction(
        control: AccControl,
        nodeList: List<AccessibilityNodeInfoCompat>?,
        action: String
    ): HandleResult = handleResult {
        val arg = action.subEnd(Action.ARG_SPLIT)
        val fullscreen = if (arg.isNullOrEmpty()) {
            control._taskBean?.fullscreen ?: true
        } else {
            arg == "true"
        }
        success = fullscreenAction(fullscreen)
        control.log("设置浮窗全屏显示模式[${arg}]:$success")
    }
}