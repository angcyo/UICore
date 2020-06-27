package com.angcyo.core.component.accessibility

import android.view.accessibility.AccessibilityEvent
import com.angcyo.core.component.file.DslFileHelper
import com.angcyo.core.component.file.wrapData

/**
 * 窗口改变日志输出
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/06/25
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class LogWindowAccessibilityInterceptor : BaseAccessibilityInterceptor() {

    var enable: Boolean = true

    override fun onAccessibilityEvent(
        service: BaseAccessibilityService,
        event: AccessibilityEvent
    ) {
        super.onAccessibilityEvent(service, event)
        if (enable) {
            if (event.isWindowStateChanged()) {
                val builder = StringBuilder()
                builder.appendln()

                service.windows.forEach {
                    builder.appendln(it.toString())
                }

                val log = service.rootNodeInfo(event)?.logNodeInfo(outBuilder = builder)

                DslFileHelper.write("accessibility", "window.log", log?.wrapData() ?: "null")
            }
        }
    }
}