package com.angcyo.core.component.accessibility.action.a

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.core.component.accessibility.AutoParseInterceptor
import com.angcyo.core.component.accessibility.BaseAccessibilityService
import com.angcyo.core.component.accessibility.action.AutoParseAction
import com.angcyo.core.component.accessibility.action.HandleResult
import com.angcyo.core.component.accessibility.parse.ConstraintBean
import com.angcyo.core.component.accessibility.uninstall

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/11/02
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */

class DestroyAction : BaseAction() {

    init {
        handleAction = ConstraintBean.ACTION_DESTROY
    }

    override fun runAction(
        autoParseAction: AutoParseAction,
        service: BaseAccessibilityService,
        constraintBean: ConstraintBean,
        handleNodeList: List<AccessibilityNodeInfoCompat>,
        handleResult: HandleResult
    ): Boolean {
        val interceptor = autoParseAction.accessibilityInterceptor
        return if (interceptor == null) {
            autoParseAction.handleActionLog("销毁拦截器[interceptor is null]:false")
            false
        } else {
            if (interceptor is AutoParseInterceptor) {
                autoParseAction.handleActionLog("销毁拦截器[${interceptor.taskBean.name}]:true")
            } else {
                autoParseAction.handleActionLog("销毁拦截器[${autoParseAction.actionBean?.title}]:true")
            }
            interceptor.uninstall("ACTION_DESTROY")
            true
        }
    }
}