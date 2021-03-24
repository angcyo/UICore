package com.angcyo.acc2.action

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.control.AccControl
import com.angcyo.acc2.control.log
import com.angcyo.acc2.parse.HandleResult
import com.angcyo.library.ex.isDebugType
import com.angcyo.library.ex.size
import com.angcyo.library.ex.subEnd
import com.angcyo.library.toastWX

/**
 * 手势双击
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/09/21
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class DoubleAction : BaseTouchAction() {

    override fun interceptAction(control: AccControl, action: String): Boolean {
        return action.startsWith(Action.ACTION_DOUBLE)
    }

    override fun runAction(
        control: AccControl,
        nodeList: List<AccessibilityNodeInfoCompat>?,
        action: String
    ): HandleResult = handleResult {

        if (action.contains("debug")) {
            if (isDebugType()) {
                success = true
                control.log("[$action]双击:debug".apply {
                    toastWX(this)
                })
                return@handleResult
            }
        }

        val arg = action.subEnd(Action.ARG_SPLIT)
        val pointList = control.accSchedule.accParse.parsePoint(arg)

        val size = pointList.size()
        if (size >= 2) {
            val p1 = randomPoint(pointList)
            success = double(control, p1.x, p1.y)
            control.log("随机双击[$p1]:[$pointList]:$success")
        } else if (size >= 1) {
            val p1 = pointList[0]
            success = double(control, p1.x, p1.y)
            control.log("双击[$p1]:$success")
        }
    }
}