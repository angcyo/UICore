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
        var result = false

        service.findNode {
            if (it.haveText("要允许")) {
                result = true
            }
        }

        //event.isClassNameContains("permission") ||
        //event.isClassNameContains("packageinstaller")

        return result
    }

    open fun handlePermissionsAction(
        action: BaseAccessibilityAction,
        service: BaseAccessibilityService,
        event: AccessibilityEvent?
    ): Boolean {
        return service.clickByText("允许", event)
    }
}