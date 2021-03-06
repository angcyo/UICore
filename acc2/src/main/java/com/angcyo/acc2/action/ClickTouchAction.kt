package com.angcyo.acc2.action

import android.graphics.PointF
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.control.AccControl
import com.angcyo.acc2.control.log
import com.angcyo.acc2.parse.HandleResult
import com.angcyo.acc2.parse.arg
import com.angcyo.library.ex.bounds
import com.angcyo.library.ex.sync

/**
 * 在节点区域执行手势[touch]操作
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/09/21
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
open class ClickTouchAction : BaseTouchAction() {

    override fun interceptAction(control: AccControl, action: String): Boolean {
        return action.cmd(Action.ACTION_CLICK2) || action.cmd(Action.ACTION_CLICK3)
    }

    override fun runAction(
        control: AccControl,
        nodeList: List<AccessibilityNodeInfoCompat>?,
        action: String
    ): HandleResult = handleResult {
        clickTouch(
            control,
            nodeList,
            this,
            action,
            action.arg(Action.ACTION_CLICK3) ?: action.arg(Action.ACTION_CLICK2)
        )
    }

    fun clickTouch(
        control: AccControl,
        nodeList: List<AccessibilityNodeInfoCompat>?,
        handleResult: HandleResult,
        action: String,
        pointArg: String?
    ) {
        val async = action.contains(Action.ASYNC)

        handleResult.apply {
            //触发节点区域的手势操作
            val click = action.startsWith(Action.ACTION_CLICK3)

            var x = 0f
            var y = 0f

            nodeList?.forEach {
                val bound = it.bounds()

                var specifyPoint: PointF? = null
                if (!pointArg.isNullOrEmpty()) {
                    control.accSchedule.accParse.parsePoint(pointArg, bound).let {
                        specifyPoint = it[0]
                    }
                }

                if (specifyPoint == null) {
                    x = bound.centerX().toFloat()
                    y = bound.centerY().toFloat()
                } else {
                    x = specifyPoint!!.x + bound.left
                    y = specifyPoint!!.y + bound.top
                }

                success = if (click) {
                    //点击节点区域
                    if (async) {
                        sync<Boolean> { _, atomicReference ->
                            atomicReference.set(click(control, x, y))
                        } == true
                    } else {
                        click(control, x, y)
                    }
                } else {
                    //双击节点区域
                    if (async) {
                        sync<Boolean> { _, atomicReference ->
                            atomicReference.set(double(control, x, y))
                        } == true
                    } else {
                        double(control, x, y)
                    }
                } || success
            }
            if (click) {
                control.log("点击节点区域[${x},${y}]:$success")
            } else {
                control.log("双击节点区域[${x},${y}]:$success")
            }
        }
    }
}