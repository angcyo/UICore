package com.angcyo.core.component.accessibility

import android.view.accessibility.AccessibilityEvent

/**
 * 每个无障碍拦截后需要执行的动作
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/06/25
 * Copyright (c) 2020 angcyo. All rights reserved.
 */

abstract class BaseAccessibilityAction {

    /**当完成了[Action], 需要调用此方法, 触发下一个[Action]*/
    var actionFinish: ((error: Boolean) -> Unit)? = null

    /**是否需要事件[event],返回true表示需要处理*/
    open fun checkEvent(service: BaseAccessibilityService, event: AccessibilityEvent?): Boolean {
        return false
    }

    /**执行action*/
    open fun doAction(service: BaseAccessibilityService, event: AccessibilityEvent?) {

    }

    /**执行action来自其他action不需要处理, 返回true表示处理了事件*/
    open fun doActionWidth(
        action: BaseAccessibilityAction,
        service: BaseAccessibilityService,
        event: AccessibilityEvent?
    ): Boolean {
        return false
    }
}