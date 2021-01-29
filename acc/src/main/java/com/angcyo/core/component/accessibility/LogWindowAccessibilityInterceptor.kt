package com.angcyo.core.component.accessibility

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import com.angcyo.acc.findNodeInfoList
import com.angcyo.core.BuildConfig
import com.angcyo.core.component.accessibility.AccessibilityHelper.logFolderName
import com.angcyo.core.component.accessibility.AccessibilityHelper.tempRect
import com.angcyo.core.component.file.DslFileHelper
import com.angcyo.core.component.file.wrapData
import com.angcyo.http.rx.doBack
import com.angcyo.library.L
import com.angcyo.library._screenHeight
import com.angcyo.library._screenWidth
import com.angcyo.library.ex.*
import com.angcyo.library.toastQQ
import java.lang.ref.WeakReference

/**
 * 窗口改变日志输出
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/06/25
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
open class LogWindowAccessibilityInterceptor : BaseAccessibilityInterceptor() {

    companion object {
        const val LOG_WINDOW_NAME = "window.log"
        const val LOG_CONTENT_NAME = "content.log"
        const val LOG_OTHER_NAME = "other.log"
        const val LOG_INTERVAL_NAME = "interval.log"


        //最后一次未处理时的节点数据
        var _lastNoHandleInfo: WeakReference<String>? = null

        /**获取所有[AccessibilityWindowInfo]的信息*/
        fun logWindow(
            allWindow: Boolean = true,
            service: AccessibilityService? = BaseAccessibilityService.lastService,
            builder: StringBuilder = StringBuilder(),
            showToast: Boolean = false
        ): String {
            if (service != null) {
                val toastStringBuilder = StringBuilder()
                val rootNodeInfo: AccessibilityNodeInfo? = service.findNodeInfoList().mainNode()
                service.windows.forEachIndexed { index, accessibilityWindowInfo ->
                    builder.appendln("$index->$accessibilityWindowInfo")

                    if (index > 0) {
                        toastStringBuilder.appendln()
                    }

                    toastStringBuilder.append("$index->title=${accessibilityWindowInfo.title}\n")

                    accessibilityWindowInfo.root?.apply {
                        getBoundsInScreen(tempRect)

                        val type = StringBuilder("")
                        if (this == rootNodeInfo) {
                            type.append("root ")
                        }
                        if (this == service.rootInActiveWindow) {
                            type.append("active ")
                        }

                        if (rootNodeInfo != null && this == rootNodeInfo) {
                            builder.append("[${type} ${_screenWidth}x${_screenHeight} ${tempRect.width()}x${tempRect.height()}]")
                            logNodeInfo(
                                outBuilder = builder,
                                refWidth = tempRect.width(),
                                refHeight = tempRect.height()
                            )
                            toastStringBuilder.append("[${type}] ")
                        } else if (allWindow) {
                            logNodeInfo(
                                outBuilder = builder,
                                refWidth = tempRect.width(),
                                refHeight = tempRect.height()
                            )
                        }
                        toastStringBuilder.append("${this.packageName}\n")
                        toastStringBuilder.append("$tempRect\n")
                    }
                }

                if (showToast) {
                    toastQQ(toastStringBuilder) {
                        toastAnimation = 0
                        duration = Toast.LENGTH_LONG
                    }
                }
            }
            return builder.toString().apply {
                if (this.length > 100) {
                    _lastNoHandleInfo = WeakReference(this)
                }
                L.w("log size:${this.toByteArray().size.toLong().fileSizeString()}")
            }
        }
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

    override fun onInterval() {
        super.onInterval()

        doBack {
            lastService?.let { service ->
                //确定日志输出文件
                val logFileName = logFileName()

                logFileName?.let {
                    val windowBuilder = StringBuilder()

                    service.windows.forEach {
                        windowBuilder.appendln(it.toString())
                        it.root?.apply {
                            windowBuilder.appendln(wrap().toString())
                        }
                    }

                    val log = windowBuilder.toString()
                    DslFileHelper.write(logFolderName, logFileName, log.wrapData())

                    allWindowLog(log)
                }
            }
        }
    }

    override fun handleFilterNode(
        service: BaseAccessibilityService,
        nodeList: List<AccessibilityNodeInfo>
    ) {
        //super.handleFilterNode(service, nodeList)
        if (enable) {

            doBack {
                val builder = StringBuilder()
                val windowBuilder = StringBuilder()

                logBeforeBuild(builder)
                logBeforeBuild(windowBuilder)

                //确定日志输出文件
                val logFileName = logFileName(lastEvent)

                lastEvent?.apply { builder.appendln(this.toString()) }

                logFileName?.let {
                    //需要输出对应的log
                    val rootNodeInfo: AccessibilityNodeInfo? =
                        service.findNodeInfoList(filterPackageNameList).mainNode()

                    service.windows.forEachIndexed { index, accessibilityWindowInfo ->
                        builder.appendln("$index->$accessibilityWindowInfo")
                        windowBuilder.appendln(accessibilityWindowInfo.toString())
                        accessibilityWindowInfo.root?.apply {
                            getBoundsInScreen(tempRect)
                            if (rootNodeInfo != null && this == rootNodeInfo) {
                                builder.append("[root ${_screenWidth}x${_screenHeight} ${tempRect.width()}x${tempRect.height()}]")

                                logNodeInfo(
                                    outBuilder = builder,
                                    refWidth = tempRect.width(),
                                    refHeight = tempRect.height()
                                )

                                windowBuilder.append("[root]")
                                windowBuilder.appendln(wrap().toString())

                                rootNodeLog(builder.toString())
                            } else {
                                windowBuilder.appendln(wrap().toString())
                                if (logAllWindow) {
                                    logNodeInfo(
                                        outBuilder = builder,
                                        refWidth = tempRect.width(),
                                        refHeight = tempRect.height()
                                    )
                                } else {
                                    builder.appendln(wrap().toString())
                                }
                            }

                        }
                    }

                    logAfterBuild(builder)
                    logAfterBuild(windowBuilder)

                    allWindowLog(windowBuilder.toString())

                    val log = builder.toString()

                    DslFileHelper.write(logFolderName, logFileName, log.wrapData())
                }
            }
        }
    }

    fun logFileName(event: AccessibilityEvent? = null): String? {
        return logFileName ?: if (logInterval) {
            LOG_INTERVAL_NAME
        } else if (event != null) {
            val windowStateChanged = event.isWindowStateChanged()
            val windowContentChanged = event.isWindowContentChanged()

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
            null
        }
    }

    /**拦截处理*/
    open fun allWindowLog(log: String) {

    }

    /**拦截处理*/
    open fun rootNodeLog(log: String) {

    }
}