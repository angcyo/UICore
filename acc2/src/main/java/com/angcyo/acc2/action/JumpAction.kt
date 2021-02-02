package com.angcyo.acc2.action

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.control.AccControl
import com.angcyo.acc2.control.log
import com.angcyo.acc2.parse.HandleResult
import com.angcyo.acc2.parse.arg
import com.angcyo.library.utils.getLongNumList

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/02/02
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class JumpAction : BaseAction() {

    override fun interceptAction(control: AccControl, action: String): Boolean {
        return action.startsWith(Action.ACTION_JUMP)
    }

    override fun runAction(
        control: AccControl,
        nodeList: List<AccessibilityNodeInfoCompat>?,
        action: String
    ): HandleResult = handleResult {

        var jumpId = -1L

        if (action.arg(Action.ACTION_JUMP) == Action.RELY) {
            //跳过到依赖的action
            jumpId = jumpActionList(control, this, control.accSchedule.relyList())
        } else {
            val actionIdList = action.getLongNumList(true)
            if (actionIdList == null) {
                //下一个
                control.accSchedule.apply {
                    next()
                    val nextActionBean = nextActionBean()
                    success = nextActionBean != null
                    jumpId = nextActionBean?.actionId ?: jumpId
                }
            } else {
                //指定id
                jumpId = jumpActionList(control, this, actionIdList)
            }
        }

        if (success) {
            control.accSchedule.jumpCountIncrement(control.accSchedule._scheduleActionBean?.actionId)
        }

        control.log("跳转至[${jumpId}]:${success}")
    }

    fun jumpActionList(
        control: AccControl,
        result: HandleResult,
        actionIdList: List<Long>?
    ): Long {
        var jumpId = -1L
        if (actionIdList != null) {
            for (actionId in actionIdList) {
                val findAction = control.findAction(actionId)
                if (findAction != null) {
                    jumpId = findAction.actionId
                    findAction.enable = true
                    control.accSchedule.nextScheduleAction(findAction)
                    result.success = true
                    break
                }
            }
        }
        return jumpId
    }
}