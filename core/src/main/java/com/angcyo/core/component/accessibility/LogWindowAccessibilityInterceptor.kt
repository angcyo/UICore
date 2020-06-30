package com.angcyo.core.component.accessibility

import android.view.accessibility.AccessibilityEvent
import com.angcyo.core.BuildConfig
import com.angcyo.core.component.accessibility.AccessibilityHelper.logFolderName
import com.angcyo.core.component.file.DslFileHelper
import com.angcyo.core.component.file.wrapData
import com.angcyo.http.rx.doBack

/**
 * 窗口改变日志输出
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/06/25
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class LogWindowAccessibilityInterceptor : BaseAccessibilityInterceptor() {

    var enable: Boolean = true

    var logWindow: Boolean = false
    var logContent: Boolean = false
    var logOther: Boolean = false
    var logInterval: Boolean = true

    //是否要打印所有window的日志, 否则只打印root
    var logAllWindow: Boolean = BuildConfig.DEBUG

    init {
        if (BuildConfig.DEBUG) {
            filterPackageNameList.add("com.ss.android.ugc.aweme")
            enableInterval = true
        }
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

            doBack {
                val builder = StringBuilder()

                val logFileName = if (event != null) {

                    val windowStateChanged = event.isWindowStateChanged()
                    val windowContentChanged = event.isWindowContentChanged()

                    builder.appendln(event.toString())

                    if (windowStateChanged && logWindow) {
                        "window.log"
                    } else if (windowContentChanged && logContent) {
                        "content.log"
                    } else if (logOther) {
                        "other.log"
                    } else {
                        null
                    }
                } else {
                    //间隔回调
                    if (logInterval) {
                        "interval.log"
                    } else {
                        null
                    }
                }

                logFileName?.let {
                    //需要输出对应的log
                    val rootNodeInfo = service.rootNodeInfo(event)

                    service.windows.forEach {
                        builder.appendln(it.toString())
                        it.root?.apply {
                            if (rootNodeInfo != null && this == rootNodeInfo) {
                                builder.append("[root]")
                                logNodeInfo(outBuilder = builder)
                            } else if (logAllWindow) {
                                logNodeInfo(outBuilder = builder)
                            }
                        }
                    }
                    val log = builder.toString()

                    DslFileHelper.write(logFolderName, logFileName, log.wrapData())
                }
            }
        }
    }
}