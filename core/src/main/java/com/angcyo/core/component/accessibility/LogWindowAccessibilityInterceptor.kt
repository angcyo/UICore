package com.angcyo.core.component.accessibility

import android.view.accessibility.AccessibilityEvent
import com.angcyo.core.BuildConfig
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

    init {
        if (BuildConfig.DEBUG) {
            filterPackageNameList.add("com.ss.android.ugc.aweme")
        }
    }

    override fun onAccessibilityEvent(
        service: BaseAccessibilityService,
        event: AccessibilityEvent
    ) {
        super.onAccessibilityEvent(service, event)
        if (enable) {

            val windowStateChanged = event.isWindowStateChanged()
            val windowContentChanged = event.isWindowContentChanged()

            val builder = StringBuilder()
            builder.appendln(event.toString())

            service.windows.forEach {
                builder.appendln(it.toString())
                it.root?.apply {
                    builder.appendln(wrap().toString())
                }
            }

            service.rootNodeInfo(event)?.logNodeInfo(outBuilder = builder)

            val log = builder.toString()

            val logFileName: String = if (windowStateChanged || windowContentChanged) {
                if (windowStateChanged) "window.log" else "content.log"
            } else {
                "other.log"
            }

            DslFileHelper.write("accessibility", logFileName, log.wrapData())
        }
    }
}