package com.angcyo.core.component.accessibility

import android.view.accessibility.AccessibilityEvent
import com.angcyo.core.BuildConfig

/**
 * 提供日志输出
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/06/25
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class LogAccessibilityInterceptor : BaseAccessibilityInterceptor() {

    var enable: Boolean = BuildConfig.DEBUG

    override fun checkDoAction(service: BaseAccessibilityService, event: AccessibilityEvent?) {
        //super.checkDoAction(service, event)
    }

    override fun onAccessibilityEvent(
        service: BaseAccessibilityService,
        event: AccessibilityEvent?
    ) {
        super.onAccessibilityEvent(service, event)

        if (enable) {
            event?.let {
                AccessibilityHelper.log(it.toString())
            }
        }
    }
}