package com.angcyo.acc2.action

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.control.AccControl
import com.angcyo.acc2.control.log
import com.angcyo.acc2.parse.HandleResult
import com.angcyo.library.ex.size
import com.angcyo.library.ex.subEnd

/**
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/09/21
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class MoveAction : BaseTouchAction() {

    override fun interceptAction(control: AccControl, action: String): Boolean {
        return action.startsWith(Action.ACTION_MOVE)
    }

    override fun runAction(
        control: AccControl,
        nodeList: List<AccessibilityNodeInfoCompat>?,
        action: String
    ): HandleResult = handleResult {
        val arg = action.subEnd(Action.ARG_SPLIT)
        val pointList = control.accSchedule.accParse.parsePoint(arg)
        if (pointList.size() >= 2) {
            val p1 = pointList[0]
            val p2 = pointList[1]
            success = move(control, p1.x, p1.y, p2.x, p2.y)
            control.log("move[$p1]->[$p2]:$success")
        }
    }
}