package com.angcyo.acc2.dynamic

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.action.handleResult
import com.angcyo.acc2.control.AccControl
import com.angcyo.acc2.control.ControlContext
import com.angcyo.acc2.parse.HandleResult

/**
 * 动态处理[com.angcyo.acc2.action.ClassAction]
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/12/16
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
interface IHandleActionDynamic {

    fun runAction(control: AccControl, nodeList: List<AccessibilityNodeInfoCompat>?, action: String): HandleResult {
        return handleResult { }
    }

    fun runAction(control: AccControl,
                  controlContext: ControlContext,
                  nodeList: List<AccessibilityNodeInfoCompat>?,
                  action: String): HandleResult {
        return runAction(control, nodeList, action)
    }

}