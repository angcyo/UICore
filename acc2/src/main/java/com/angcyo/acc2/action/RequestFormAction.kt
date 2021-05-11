package com.angcyo.acc2.action

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.control.AccControl
import com.angcyo.acc2.control.ControlContext
import com.angcyo.acc2.control.log
import com.angcyo.acc2.parse.HandleResult
import com.angcyo.acc2.parse.arg

/**
 * 中途请求表单命令
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/05/10
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class RequestFormAction : BaseAction() {

    companion object {
        const val TYPE_OPERATE = "operate"
        const val TYPE_HANDLE = "handle"
        const val TYPE_ACTION = "action"
        const val TYPE_TASK = "task"
    }

    override fun interceptAction(control: AccControl, action: String): Boolean {
        return action.cmd(Action.ACTION_REQUEST_FORM)
    }

    override fun runAction(
        control: AccControl,
        controlContext: ControlContext,
        nodeList: List<AccessibilityNodeInfoCompat>?,
        action: String
    ): HandleResult = handleResult {

        val arg = action.arg(Action.ACTION_REQUEST_FORM)
        var type: String? = arg

        val formBean = when {
            arg.isNullOrEmpty() -> {
                controlContext.handle?.operate?.form?.apply {
                    type = TYPE_OPERATE
                } ?: controlContext.handle?.form?.apply {
                    type = TYPE_HANDLE
                } ?: controlContext.action?.form?.apply {
                    type = TYPE_ACTION
                } ?: control._taskBean?.form?.apply {
                    type = TYPE_TASK
                }
            }
            arg == TYPE_OPERATE -> controlContext.handle?.operate?.form
            arg == TYPE_HANDLE -> controlContext.handle?.form
            arg == TYPE_ACTION -> controlContext.action?.form
            arg == TYPE_TASK -> control._taskBean?.form
            else -> null
        }

        val formParse = control.accSchedule.accParse.formParse
        success = formBean != null && formParse.formRequestListener != null

        formBean?.let {
            when (type) {
                TYPE_OPERATE -> formParse.parseOperateForm(
                    controlContext,
                    control,
                    controlContext.handle,
                    null,
                    this
                )
                TYPE_HANDLE -> formParse.parseHandleForm(
                    controlContext,
                    control,
                    controlContext.handle,
                    null,
                    this
                )
                TYPE_ACTION -> formParse.parseActionForm(
                    controlContext,
                    control,
                    controlContext.action,
                    this
                )
                TYPE_TASK -> formParse.parseTaskForm(
                    control,
                    control._controlState
                )
            }
        }

        //sleep
        if (success && formBean?.sync == true) {
            control.accPrint.sleep(-1)
        }

        control.log("请求表单Action[${arg}]:${success}↓\n${formBean}")
    }
}