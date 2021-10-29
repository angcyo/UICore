package com.angcyo.acc2.action

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.bean.ActionBean
import com.angcyo.acc2.control.AccControl
import com.angcyo.acc2.control.actionLog
import com.angcyo.acc2.control.log
import com.angcyo.acc2.parse.HandleResult
import com.angcyo.library.utils.getLongNumList

/**
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/09/21
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class DisableAction : BaseAction() {

    override fun interceptAction(control: AccControl, action: String): Boolean {
        return action.cmd(Action.ACTION_DISABLE)
    }

    override fun runAction(
        control: AccControl,
        nodeList: List<AccessibilityNodeInfoCompat>?,
        action: String
    ): HandleResult = handleResult {

        val actionIdList = mutableListOf<Long>()
        actionIdList.addAll(action.getLongNumList(true) ?: emptyList())
        if (action.contains(Action.RELY)) {
            actionIdList.addAll(control.accSchedule.relyList() ?: emptyList())
        }
        if (action.contains(Action.CURRENT)) {
            control.accSchedule._runActionBean?.actionId?.let {
                actionIdList.add(it)
            }
        }
        val actionList = getActionList(control, actionIdList)

        control.accSchedule.disableActionIdList.addAll(actionIdList)
        control.accSchedule.enableActionIdList.removeAll(actionIdList)

        success = actionList.isNotEmpty()

        control.log(buildString {
            actionList.forEachIndexed { index, actionBean ->
                actionBean.enable = false
                append("禁用[${actionBean.actionLog()}]")
                if (index != actionList.lastIndex) {
                    appendLine()
                }
            }
        })
    }

    fun getActionList(control: AccControl, actionIdList: List<Long>?): List<ActionBean> {
        val result = mutableListOf<ActionBean>()
        if (actionIdList != null) {
            for (actionId in actionIdList) {
                val findAction = control.findAction(actionId)
                if (findAction != null) {
                    result.add(findAction)
                }
            }
        }
        return result
    }
}