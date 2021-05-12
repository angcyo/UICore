package com.angcyo.acc2.action

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.control.AccControl
import com.angcyo.acc2.control.log
import com.angcyo.acc2.parse.HandleResult
import com.angcyo.library.ex.takeScreenshot

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/05/12
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class ScreenshotAction : BaseAction() {

    override fun interceptAction(control: AccControl, action: String): Boolean {
        return action.cmd(Action.ACTION_SCREENSHOT)
    }

    override fun runAction(
        control: AccControl,
        nodeList: List<AccessibilityNodeInfoCompat>?,
        action: String
    ): HandleResult = handleResult {
        success = control.accService()?.takeScreenshot() == true
        control.log("截屏:$success")
    }

}