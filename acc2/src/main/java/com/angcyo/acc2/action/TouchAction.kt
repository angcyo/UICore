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
class TouchAction : BaseTouchAction() {

    override fun interceptAction(control: AccControl, action: String): Boolean {
        return action.startsWith(Action.ACTION_TOUCH)
    }

    override fun runAction(
        control: AccControl,
        nodeList: List<AccessibilityNodeInfoCompat>?,
        action: String
    ): HandleResult = handleResult {
        val arg = action.subEnd(Action.ARG_SPLIT)
        val pointList = control.accSchedule.accParse.parsePoint(arg)

        val size = pointList.size()
        if (size >= 2) {
            val p1 = randomPoint(pointList)
            success = click(control, p1.x, p1.y)
            control.log("随机touch[$p1]:[$pointList]:$success")
        } else if (size >= 1) {
            val p1 = pointList[0]
            success = click(control, p1.x, p1.y)
            control.log("touch[$p1]:$success")
        }
    }
}