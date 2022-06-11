package com.angcyo.acc2.action

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.control.AccControl
import com.angcyo.acc2.control.log
import com.angcyo.acc2.parse.HandleResult
import com.angcyo.acc2.parse.toLog
import com.angcyo.library.ex.*
import com.angcyo.library.toastWX
import com.angcyo.library.utils.getFloatNumList

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/02/01
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class ClickAction : ClickTouchAction() {

    override fun interceptAction(control: AccControl, action: String): Boolean {
        return action.cmd(Action.ACTION_CLICK)
    }

    fun targetNode(
        node: AccessibilityNodeInfoCompat,
        action: String
    ): AccessibilityNodeInfoCompat? {
        return if (action.subEnd(Action.ARG_SPLIT)?.contains(Action.NOT_PARENT) == true) {
            node
        } else {
            node.getClickParent()
        }
    }

    override fun runAction(
        control: AccControl,
        nodeList: List<AccessibilityNodeInfoCompat>?,
        action: String
    ): HandleResult = handleResult {

        if (action.contains("debug")) {
            if (isDebugType()) {
                success = true
                control.log("[$action]点击节点:debug".apply {
                    toastWX(this)
                })
                return@handleResult
            }
        }

        val pointList = action.getFloatNumList()
        if (pointList.size() >= 2) {
            clickTouch(
                control,
                nodeList,
                this,
                Action.ACTION_CLICK3,
                action.subEnd(Action.ARG_SPLIT)
            )
        } else {
            //触发节点自带的click
            val arg = action.subEnd(Action.ARG_SPLIT)

            nodeList?.forEach { node ->
                var result = false
                if (arg.isNullOrEmpty()) {
                    result = targetNode(node, action)?.click() == true
                } else {
                    control.accSchedule.accParse.findParse.getStateParentNode(listOf(arg), node)
                        .apply {
                            result = if (first) {
                                targetNode(node, action)?.click() ?: false || result
                            } else {
                                //携带了状态约束参数, 并且没有匹配到状态. 则直接返回成功.
                                control.log("[$arg]状态不满足, 跳过点击节点.")
                                true
                            }
                        }
                }

                success = success || result
                if (result) {
                    addNode(node)
                }
                control.log("点击节点:$result ↓\n${node.toLog(isShowDebug())}")
            }
        }
    }
}