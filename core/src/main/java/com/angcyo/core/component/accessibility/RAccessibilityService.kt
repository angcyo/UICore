package com.angcyo.core.component.accessibility

import android.content.Intent
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
open class RAccessibilityService : BaseAccessibilityService() {
    companion object {

        val accessibilityInterceptorList =
            CopyOnWriteArrayList<BaseAccessibilityInterceptor>()

        /**最后一次窗口变化[TYPE_WINDOW_STATE_CHANGED]的程序包名*/
        val lastPackageNameList: MutableList<Pair<CharSequence, CharSequence>> = mutableListOf()

        /**添加拦截器*/
        fun addInterceptor(interceptor: BaseAccessibilityInterceptor) {
            if (!accessibilityInterceptorList.contains(interceptor)) {
                accessibilityInterceptorList.add(interceptor)
            }

            if (interceptor.lastService == null) {
                interceptor.restart()
                lastService?.apply {
                    interceptor.onServiceConnected(this)
                }
            }
        }

        /**移除拦截器*/
        fun removeInterceptor(interceptor: BaseAccessibilityInterceptor, reason: String? = null) {
            if (accessibilityInterceptorList.contains(interceptor)) {
                accessibilityInterceptorList.remove(interceptor)
                interceptor.onDestroy(reason)
            }
        }

        /**清空拦截器*/
        fun clearInterceptor(reason: String? = null) {
            accessibilityInterceptorList.forEach {
                it.onDestroy(reason)
            }
            accessibilityInterceptorList.clear()
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        super.onAccessibilityEvent(event)
        event?.let {

            when (event.eventType) {
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                    //当被监听的程序窗口状态变化时回调, 通常打开程序时会回调
                    onWindowStateChanged(event)
                }
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                    //当窗口上有内容发生变化的时候回调
                    onWindowContentChanged(event)
                }
            }

            for (i in accessibilityInterceptorList.lastIndex downTo 0) {
                //反向调用, 防止调用者在内部执行了Remove操作, 导致后续的拦截器无法执行
                if (accessibilityInterceptorList.size > i) {
                    try {
                        val interceptor = accessibilityInterceptorList[i]
                        interceptor.onAccessibilityEvent(this, event)
                    } catch (e: Exception) {
                        L.e(e)
                        AccessibilityHelper.log(e.toString())
                    }
                }
            }
        }
    }

    /**打开了新窗口*/
    open fun onWindowStateChanged(event: AccessibilityEvent) {
        val last = lastPackageNameList.lastOrNull()

        if (lastPackageNameList.size >= 10) {
            for (i in 5 downTo 0) {
                lastPackageNameList.removeAt(0)
            }
        }

        val packageName = event.packageName
        val className = event.className
        lastPackageNameList.add(packageName to className)

        if (last?.first != packageName) {
            "切换:${last?.second}[${last?.first}]->$className[$packageName]".apply {
                AccessibilityHelper.log(this)
                //L.i(this)
            }
        }
    }

    /**窗口中, 有内容发生了变化*/
    open fun onWindowContentChanged(event: AccessibilityEvent) {

    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        accessibilityInterceptorList.forEach {
            it.onServiceConnected(this)
        }
    }

    /**服务断开 优于 onDestroy 执行*/

    override fun onUnbind(intent: Intent?): Boolean {
        L.e("onUnbind -> $intent")
        isServiceConnected = false
        lastPackageNameList.clear()
        //clearInterceptor()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()

        for (i in accessibilityInterceptorList.size - 1 downTo 0) {
            //反向调用, 防止调用者在内部执行了Remove操作, 导致后续的拦截器无法执行
            if (accessibilityInterceptorList.size > i) {
                val interceptor = accessibilityInterceptorList[i]
                try {
                    interceptor.onDestroy("无障碍服务被销毁")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}