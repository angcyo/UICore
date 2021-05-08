package com.angcyo.acc2.action

import com.angcyo.acc2.control.AccControl
import com.angcyo.library.ex.subEnd

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/05/08
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
abstract class BaseClearAction : BaseAction() {

    fun getClearActionIdList(control: AccControl, action: String): List<Long> {

        val clearActionIdList = mutableListOf<Long>()
        val argList = action.subEnd(Action.ARG_SPLIT)?.split(Action.ARG_SPLIT2) ?: emptyList()

        if (argList.isNullOrEmpty()) {
            control.accSchedule._scheduleActionBean?.actionId?.let {
                clearActionIdList.add(it)
            }
        } else {
            for (arg in argList) {
                when (arg) {
                    //跳过到依赖的action
                    Action.RELY -> clearActionIdList.addAll(
                        control.accSchedule.relyList() ?: emptyList()
                    )
                    //跳过当前调度action
                    "." -> control.accSchedule._scheduleActionBean?.actionId?.let {
                        clearActionIdList.add(it)
                    }
                    //指定id
                    else -> {
                        arg.toLongOrNull()?.let {
                            clearActionIdList.add(it)
                        }
                    }
                }
            }
        }

        return clearActionIdList
    }

    fun clearRunTime(control: AccControl, actionIdList: List<Long>?): Boolean {
        var clearId = -1L
        if (actionIdList != null) {
            for (actionId in actionIdList) {
                val findAction = control.findAction(actionId)
                if (findAction != null) {
                    clearId = actionId
                    control.accSchedule.setActionRunTime(actionId, -1)
                }
            }
        }
        return clearId != -1L
    }

    fun clearJumpCount(control: AccControl, actionIdList: List<Long>?): Boolean {
        var clearId = -1L
        if (actionIdList != null) {
            for (actionId in actionIdList) {
                val findAction = control.findAction(actionId)
                if (findAction != null) {
                    clearId = actionId
                    control.accSchedule.clearJumpCount(actionId)
                }
            }
        }
        return clearId != -1L
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