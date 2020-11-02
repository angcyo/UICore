package com.angcyo.core.component.accessibility.action.a

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.core.component.accessibility.BaseAccessibilityService
import com.angcyo.core.component.accessibility.action.AutoParseAction
import com.angcyo.core.component.accessibility.action.HandleResult
import com.angcyo.core.component.accessibility.action.arg
import com.angcyo.core.component.accessibility.parse.ConstraintBean
import com.angcyo.core.component.accessibility.parse.FormBean

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/11/02
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */

class TaskAction : BaseAction() {

    var onStartTask: ((autoParseAction: AutoParseAction, taskFormBean: FormBean, loop: String?, count: Long) -> Boolean)? =
        null

    init {
        handleAction = ConstraintBean.ACTION_TASK
    }

    override fun runAction(
        autoParseAction: AutoParseAction,
        service: BaseAccessibilityService,
        constraintBean: ConstraintBean,
        handleNodeList: List<AccessibilityNodeInfoCompat>,
        handleResult: HandleResult
    ): Boolean {
        val taskFormBean = autoParseAction.actionBean?.task
        return if (taskFormBean != null) {
            if (!taskFormBean.url.isNullOrEmpty()) {
                val loop = arg?.arg(0)
                val count = arg?.arg(1)?.toLongOrNull() ?: -1

                val function = onStartTask
                if (function == null) {
                    autoParseAction.handleActionLog("忽略任务[onStartTask is null]:true")
                    true
                } else {
                    if (function.invoke(autoParseAction, taskFormBean, loop, count)) {
                        autoParseAction.handleActionLog("启动任务[${taskFormBean.url}][$arg]:true")
                        true
                    } else {
                        autoParseAction.handleActionLog("启动任务失败:false")
                        false
                    }
                }
            } else {
                autoParseAction.handleActionLog("启动任务失败,未指定[Form.url]:false")
                false
            }
        } else {
            autoParseAction.handleActionLog("启动任务失败,未指定[Form]:false")
            false
        }
    }
}