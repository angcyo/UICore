package com.angcyo.acc2.action

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.control.AccControl
import com.angcyo.acc2.control.log
import com.angcyo.acc2.parse.HandleResult
import com.angcyo.acc2.parse.arg
import com.angcyo.library.ex.size
import com.angcyo.library.ex.sync

/**
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/09/21
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class FlingAction : BaseTouchAction() {

    override fun interceptAction(control: AccControl, action: String): Boolean {
        return action.startsWith(Action.ACTION_FLING)
    }

    override fun runAction(
        control: AccControl,
        nodeList: List<AccessibilityNodeInfoCompat>?,
        action: String
    ): HandleResult = handleResult {
        val arg = action.arg(Action.ACTION_FLING)
        val async = action.contains(Action.ASYNC)

        val pointList = control.accSchedule.accParse.parsePoint(arg)
        if (pointList.size() >= 2) {
            val p1 = pointList[0]
            val p2 = pointList[1]
            success = if (async) {
                sync<Boolean> { _, atomicReference ->
                    atomicReference.set(
                        fling(
                            control,
                            p1.x,
                            p1.y,
                            p2.x,
                            p2.y
                        )
                    )
                } == true
            } else {
                fling(control, p1.x, p1.y, p2.x, p2.y/*, startTime = 16 * 2, duration = 16 * 2*/)
            }
            control.log("fling[$p1]->[$p2]:$success")
        }
    }
}