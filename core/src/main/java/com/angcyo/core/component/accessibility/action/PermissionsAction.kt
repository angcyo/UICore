package com.angcyo.core.component.accessibility.action

import android.view.accessibility.AccessibilityEvent
import com.angcyo.core.component.accessibility.*

/**
 * 关闭系统权限对话框, 其他rom未适配
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/06/25
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
open class PermissionsAction : BaseAccessibilityAction() {
    override fun doActionWidth(
        action: BaseAccessibilityAction,
        service: BaseAccessibilityService,
        event: AccessibilityEvent?
    ): Boolean {
        if (isPermissionsUI(service, event)) {
            return handlePermissionsAction(action, service, event)
        }
        return super.doActionWidth(action, service, event)
    }

    open fun isPermissionsUI(
        service: BaseAccessibilityService,
        event: AccessibilityEvent?
    ): Boolean {
        val haveNode = service.haveNode("要允许抖音短视频", event)

        val haveEvent = if (event != null) {
            event.isClassNameContains("permission") ||
                    event.isClassNameContains("packageinstaller") ||
                    event.haveText("要允许抖音短视频")
        } else {
            false
        }

        return haveNode || haveEvent
    }

    open fun handlePermissionsAction(
        action: BaseAccessibilityAction,
        service: BaseAccessibilityService,
        event: AccessibilityEvent?
    ): Boolean {
        return service.clickByText("允许", event)
    }
}