package com.angcyo.core.component.accessibility.action

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
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
        event: AccessibilityEvent?,
        nodeList: List<AccessibilityNodeInfo>
    ): Boolean {
        if (isPermissionsUI(service, event)) {
            return handlePermissionsAction(action, service, event)
        }
        return super.doActionWidth(action, service, event, nodeList)
    }

    open fun isPermissionsUI(
        service: BaseAccessibilityService,
        event: AccessibilityEvent?
    ): Boolean {
        var result = false

        service.findNode {
            if (it.haveText("要允许") || it.haveText("安装.+未知应用")) {
                result = true
            }
            result
        }

        return result
    }

    open fun handlePermissionsAction(
        action: BaseAccessibilityAction,
        service: BaseAccessibilityService,
        event: AccessibilityEvent?
    ): Boolean {
        return service.rootNodeInfo()?.findNode {
            if (it.haveText("允许") || it.haveText("取消")) {
                1
            } else {
                -1
            }
        }?.clickAll() ?: false
    }
}