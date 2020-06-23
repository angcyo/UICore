package com.angcyo.core.component.accessibility

import android.content.Intent
import android.os.Build
import android.text.TextUtils
import android.view.accessibility.AccessibilityEvent
import com.angcyo.library.L
import java.util.concurrent.CopyOnWriteArrayList

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/06/23
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class RAccessibilityService : BaseAccessibilityService() {
    companion object {

        private val accessibilityInterceptorList =
            CopyOnWriteArrayList<BaseAccessibilityInterceptor>()

        var logNodeInfo = false

        /**最后一次窗口变化[TYPE_WINDOW_STATE_CHANGED]的程序包名*/
        var lastPackageName = ""

        /**添加拦截器*/
        fun addInterceptor(interceptor: BaseAccessibilityInterceptor) {
            if (!accessibilityInterceptorList.contains(interceptor)) {
                accessibilityInterceptorList.add(interceptor)
            }
        }

        /**移除拦截器*/
        fun removeInterceptor(interceptor: BaseAccessibilityInterceptor) {
            if (accessibilityInterceptorList.contains(interceptor)) {
                accessibilityInterceptorList.remove(interceptor)
            }
        }

        fun clearInterceptor() {
            accessibilityInterceptorList.clear()
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        super.onAccessibilityEvent(event)
        event?.let {
            var ignoreLog = false

            if (event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
                if ("com.android.systemui" == event.packageName) {
                    ignoreLog = true
                }
            }

            if (!ignoreLog && logNodeInfo) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    L.d("事件通知: size:${windows.size} $windows $event")
                } else {
                    L.d("事件通知: $event")
                }

                try {
                    if (event.source == null) {
                        L.e("event.source 为空")

                        if (rootInActiveWindow == null) {
                            L.e("rootInActiveWindow 为空")
                        } else {
                            if (logNodeInfo) {
                                rootInActiveWindow?.getRootNodeInfo()?.logNodeInfo()
                            }
                        }
                    } else {
                        if (logNodeInfo) {
                            event.source?.getRootNodeInfo()?.logNodeInfo()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            try {
                checkLastPackageName(event)
            } catch (e: Exception) {
                L.e("异常:${e.message}\n$rootInActiveWindow\n$event")
            }

            when (event.eventType) {
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                    //当被监听的程序窗口状态变化时回调, 通常打开程序时会回调

//                val view = View(applicationContext)
//                view.layoutParams = ViewGroup.LayoutParams(100, 100)
//                view.setBackgroundColor(Color.RED)
//                //event.source.getChild(0).getChild(0).getChild(0).addChild(view)
////                val nodeFromPath = nodeFromPath(event.source, "0_0_2_1_0")
////                Rx.base({
////                    Thread.sleep(2000)
////                }) {
////                    clickNode(nodeFromPath)
////                }
//                L.e("call: onAccessibilityEvent -> ${findListView(event.source)}")
                    //logNodeInfo(getRootNodeInfo(event.source))
                    onWindowStateChanged(event)
                }
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                    //当窗口上有内容发生变化的时候回调
                    onWindowContentChanged(event)
                }
            }

            rootInActiveWindow?.packageName?.let { packageName ->
                for (i in accessibilityInterceptorList.size - 1 downTo 0) {
                    //反向调用, 防止调用者在内部执行了Remove操作, 导致后续的拦截器无法执行
                    if (accessibilityInterceptorList.size > i) {
                        val interceptor = accessibilityInterceptorList[i]
                        try {
                            when {
                                interceptor.filterPackageNameList.isEmpty() -> {
                                    interceptor.onAccessibilityEvent(this, event)
                                }
                                interceptor.filterPackageNameList.contains(packageName) -> {
                                    interceptor.onAccessibilityEvent(this, event)
                                }
                                else -> {
                                    interceptor.onLeavePackageName(
                                        this,
                                        event,
                                        "${event.packageName}"
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    open fun checkLastPackageName(event: AccessibilityEvent) {
//        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
//            if (event.packageName == "com.android.systemui") {
//                event.className.startsWith("android.widget")
//                return
//            }
//        } else if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
//            return
//        }

        //this.rootInActiveWindow.packageName event.packageName 这2个包名 不一定会是相同的

//        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
//            || TextUtils.isEmpty(event.packageName)
//            || TextUtils.isEmpty(event.className)
//        ) {
//            return
//        }

        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return
        }

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            return
        }

        if (rootInActiveWindow == null) {
            return
        }

        if (TextUtils.isEmpty(rootInActiveWindow!!.packageName)) {
            return
        }

        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return
        }

        lastPackageName = "${rootInActiveWindow!!.packageName}"

//        if (event.packageName?.contains("inputmethod") == true || event.className?.contains("SoftInputWindow") == true) {
//            //搜狗输入法 com.sohu.inputmethod.sogou android.inputmethodservice.SoftInputWindow
//        } else {
//        }

        L.i(
            "\n切换到:${AccessibilityEvent.eventTypeToString(event.eventType)}" +
                    "\n主:${rootInActiveWindow!!.packageName}" +
                    "\n副:${event.packageName}" +
                    "\n类:${event.className} ${event.action}"
        )
    }

    /**打开了新窗口*/
    open fun onWindowStateChanged(event: AccessibilityEvent) {

    }

    /**窗口中, 有内容发生了变化*/
    open fun onWindowContentChanged(event: AccessibilityEvent) {

    }

    override fun onServiceConnected() {
        super.onServiceConnected()
    }

    /**服务断开 优于 onDestroy 执行*/

    override fun onUnbind(intent: Intent?): Boolean {
        L.e("onUnbind -> $intent")
        isServiceConnected = false
        lastPackageName = ""
        clearInterceptor()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()

        for (i in accessibilityInterceptorList.size - 1 downTo 0) {
            //反向调用, 防止调用者在内部执行了Remove操作, 导致后续的拦截器无法执行
            if (accessibilityInterceptorList.size > i) {
                val interceptor = accessibilityInterceptorList[i]
                try {
                    interceptor.onDestroy()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}