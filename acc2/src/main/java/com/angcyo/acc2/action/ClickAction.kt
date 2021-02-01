package com.angcyo.acc2.action

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.control.AccControl
import com.angcyo.acc2.control.log
import com.angcyo.acc2.parse.HandleResult
import com.angcyo.acc2.parse.toLog
import com.angcyo.library.ex.click
import com.angcyo.library.ex.getClickParent
import com.angcyo.library.ex.isDebug

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/02/01
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class ClickAction : BaseAction() {

    override fun interceptAction(control: AccControl, action: String): Boolean {
        return action.startsWith(Action.ACTION_CLICK)
    }

    override fun runAction(
        control: AccControl,
        nodeList: List<AccessibilityNodeInfoCompat>?,
        action: String
    ): HandleResult = handleResult {
        //触发节点自带的click
        nodeList?.forEach { node ->
            val result = node.getClickParent()?.click() == true
            success = success || result
            if (result) {
                addNode(node)
            }
            control.log("点击节点[${node.toLog(isDebug())}]:$result")
        }
    }
}