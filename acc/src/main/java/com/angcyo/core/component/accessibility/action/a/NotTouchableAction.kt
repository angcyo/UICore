package com.angcyo.core.component.accessibility.action.a

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.core.component.accessibility.AutoParseInterceptor
import com.angcyo.core.component.accessibility.BaseAccessibilityService
import com.angcyo.core.component.accessibility.action.AutoParseAction
import com.angcyo.core.component.accessibility.action.HandleResult
import com.angcyo.core.component.accessibility.base.AccessibilityWindow
import com.angcyo.core.component.accessibility.parse.ConstraintBean

/**
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/10/26
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class NotTouchableAction : BaseAction() {

    init {
        handleAction = ConstraintBean.ACTION_NOT_TOUCHABLE
    }

    override fun runAction(
        autoParseAction: AutoParseAction,
        service: BaseAccessibilityService,
        constraintBean: ConstraintBean,
        handleNodeList: List<AccessibilityNodeInfoCompat>,
        handleResult: HandleResult
    ): Boolean {
        val notTouch = if (arg.isNullOrEmpty()) {
            val interceptor = autoParseAction.accessibilityInterceptor
            if (interceptor is AutoParseInterceptor) {
                interceptor.taskBean.notTouchable
            } else {
                true
            }
        } else {
            arg == "true"
        }

        AccessibilityWindow.notTouch = notTouch
        autoParseAction.handleActionLog("设置浮窗不接受Touch事件[$notTouch]:true")
        return true
    }
}