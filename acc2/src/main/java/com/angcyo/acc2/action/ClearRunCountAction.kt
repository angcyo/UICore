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
class ClearRunCountAction : BaseAction() {

    override fun interceptAction(control: AccControl, action: String): Boolean {
        return action.startsWith(Action.ACTION_CLEAR_RUN_COUNT)
    }

    override fun runAction(
        control: AccControl,
        nodeList: List<AccessibilityNodeInfoCompat>?,
        action: String
    ): HandleResult = handleResult {

        var _actionIdList: List<Long>? = null

        if (action.arg(Action.ACTION_CLEAR_RUN_COUNT) == Action.RELY) {
            //跳过到依赖的action
            _actionIdList = control.accSchedule.relyList()
            success = clearRunCount(control, _actionIdList)
        } else {
            val actionIdList = action.getLongNumList(true)
            if (actionIdList.isNullOrEmpty()) {
                //当前
                val actionBean = control.accSchedule._scheduleActionBean
                if (actionBean != null) {
                    _actionIdList = listOf(actionBean.actionId)
                    success = clearRunCount(control, _actionIdList)
                }
            } else {
                //指定id
                _actionIdList = actionIdList
                success = clearRunCount(control, actionIdList)
            }
        }
        control.log("清理运行次数[${_actionIdList}]:${success}")
    }

    fun clearRunCount(control: AccControl, actionIdList: List<Long>?): Boolean {
        var clearId = -1L
        if (actionIdList != null) {
            for (actionId in actionIdList) {
                val findAction = control.findAction(actionId)
                if (findAction != null) {
                    clearId = actionId
                    control.accSchedule.clearRunCount(actionId)
                }
            }
        }
        return clearId != -1L
    }
}