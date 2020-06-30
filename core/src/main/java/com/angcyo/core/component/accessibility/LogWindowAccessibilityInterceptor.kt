package com.angcyo.core.component.accessibility

import android.view.accessibility.AccessibilityEvent
import com.angcyo.core.BuildConfig
import com.angcyo.core.component.accessibility.AccessibilityHelper.logFolderName
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
        enableInterval = true
    }

    override fun checkDoAction(service: BaseAccessibilityService, event: AccessibilityEvent?) {
        //super.checkDoAction(service, event)
    }

    override fun onAccessibilityEvent(
        service: BaseAccessibilityService,
        event: AccessibilityEvent?
    ) {
        super.onAccessibilityEvent(service, event)
        if (enable) {
            val builder = StringBuilder()

            val logFileName = if (event != null) {

                val windowStateChanged = event.isWindowStateChanged()
                val windowContentChanged = event.isWindowContentChanged()

                builder.appendln(event.toString())

                if (windowStateChanged || windowContentChanged) {
                    if (windowStateChanged) "window.log" else "content.log"
                } else {
                    "other.log"
                }
            } else {
                //间隔回调
                "interval.log"
            }

            val rootNodeInfo = service.rootNodeInfo(event)

            service.windows.forEach {
                builder.appendln(it.toString())
                it.root?.apply {
                    if (rootNodeInfo != null && this == rootNodeInfo) {
                        builder.append("[root]")
                    }
                    logNodeInfo(outBuilder = builder)
                }
            }

            rootNodeInfo?.logNodeInfo(outBuilder = builder)

            val log = builder.toString()

            DslFileHelper.write(logFolderName, logFileName, log.wrapData())
        }
    }
}