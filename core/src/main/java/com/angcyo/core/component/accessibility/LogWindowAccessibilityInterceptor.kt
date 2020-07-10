package com.angcyo.core.component.accessibility

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
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

    companion object {
        const val LOG_WINDOW_NAME = "window.log"
        const val LOG_CONTENT_NAME = "content.log"
        const val LOG_OTHER_NAME = "other.log"
        const val LOG_INTERVAL_NAME = "interval.log"
    }

    var enable: Boolean = true

    var logWindow: Boolean = false
    var logContent: Boolean = false
    var logOther: Boolean = false
    var logInterval: Boolean = true

    //是否要打印所有window的日志, 否则只打印root
    var logAllWindow: Boolean = BuildConfig.DEBUG

    /**如果不为空, 表示强制指定log输出文件, 否则智能设置*/
    var logFileName: String? = null

    //日志打印之前
    var logBeforeBuild: StringBuilder.() -> Unit = {}

    //日志打印之后
    var logAfterBuild: StringBuilder.() -> Unit = {}

    init {
        if (logInterval) {
            ignoreInterceptor = true
            enableInterval = true
            //避免log输出, 限制5秒一次
            intervalDelay = 5_000
        }
    }

    override fun onServiceConnected(service: BaseAccessibilityService) {
        startAction()
        super.onServiceConnected(service)
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

                logBeforeBuild(builder)

                val logFileName = logFileName ?: if (event != null) {

                    val windowStateChanged = event.isWindowStateChanged()
                    val windowContentChanged = event.isWindowContentChanged()

                    builder.appendln(event.toString())

                    if (windowStateChanged && logWindow) {
                        LOG_WINDOW_NAME
                    } else if (windowContentChanged && logContent) {
                        LOG_CONTENT_NAME
                    } else if (logOther) {
                        LOG_OTHER_NAME
                    } else {
                        null
                    }
                } else {
                    //间隔回调
                    if (logInterval) {
                        LOG_INTERVAL_NAME
                    } else {
                        null
                    }
                }

                logFileName?.let {
                    //需要输出对应的log
                    val rootNodeInfo: AccessibilityNodeInfo? =
                        service.findNodeInfo(filterPackageNameList).firstOrNull()

                    service.windows.forEach {
                        builder.appendln(it.toString())
                        it.root?.apply {
                            if (rootNodeInfo != null && this == rootNodeInfo) {
                                builder.append("[root]")
                                logNodeInfo(outBuilder = builder)
                            } else if (logAllWindow) {
                                logNodeInfo(outBuilder = builder)
                            } else {
                                builder.appendln(wrap().toString())
                            }
                        }
                    }

                    logAfterBuild(builder)

                    val log = builder.toString()

                    DslFileHelper.write(logFolderName, logFileName, log.wrapData())
                }
            }
        }
    }
}