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
 * @date 2020/10/20
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class FullscreenAction : BaseAction() {

    init {
        handleAction = ConstraintBean.ACTION_FULLSCREEN
    }

    override fun runAction(
        autoParseAction: AutoParseAction,
        service: BaseAccessibilityService,
        constraintBean: ConstraintBean,
        handleNodeList: List<AccessibilityNodeInfoCompat>,
        handleResult: HandleResult
    ): Boolean {

        val fullscreen = if (arg.isNullOrEmpty()) {
            val interceptor = autoParseAction.accessibilityInterceptor
            if (interceptor is AutoParseInterceptor) {
                interceptor.taskBean.fullscreen
            } else {
                true
            }
        } else {
            arg == "true"
        }

        AccessibilityWindow.fullscreenLayer = fullscreen
        autoParseAction.handleActionLog("设置浮窗全屏显示模式[$fullscreen]:true")
        return true
    }
}