package com.angcyo.core.component.accessibility.action.a

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.core.component.accessibility.BaseAccessibilityService
import com.angcyo.core.component.accessibility.action.AutoParseAction
import com.angcyo.core.component.accessibility.action.HandleResult
import com.angcyo.core.component.accessibility.action.arg
import com.angcyo.core.component.accessibility.parse.ConstraintBean

/**
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/09/21
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class SleepAction : BaseAction() {

    init {
        handleAction = ConstraintBean.ACTION_SLEEP
    }

    override fun runAction(
        autoParseAction: AutoParseAction,
        service: BaseAccessibilityService,
        constraintBean: ConstraintBean,
        handleNodeList: List<AccessibilityNodeInfoCompat>,
        handleResult: HandleResult
    ): Boolean {
        //指定下一次周期循环的间隔
        var value = false
        val arg = arg

        val delay = autoParseAction.getInterceptorIntervalDelay(autoParseAction.actionInterval)

        val interval: Long = when {
            arg.isNullOrEmpty() -> delay
            arg.startsWith("+") -> delay + autoParseAction.getInterceptorIntervalDelay(
                arg.arg(1, "+")
            )
            arg.startsWith("-") -> delay - autoParseAction.getInterceptorIntervalDelay(
                arg.arg(1, "-")
            )
            else -> autoParseAction.getInterceptorIntervalDelay(arg)
        }

        autoParseAction.accessibilityInterceptor?.let {
            it.intervalDelay = interval//autoParseAction.getInterceptorIntervalDelay(interval)
            value = true
            autoParseAction.handleActionLog("指定下一个周期在[${it.intervalDelay}ms]:$value")
        }

        return value
    }
}