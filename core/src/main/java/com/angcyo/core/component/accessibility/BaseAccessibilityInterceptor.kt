package com.angcyo.core.component.accessibility

import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import com.angcyo.library.ex.nowTime

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2018/01/26 08:57
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
abstract class BaseAccessibilityInterceptor {

    /**需要收到那个程序的事件, 匹配方式为 `包含`, 匹配方式为 `全等`*/
    val filterPackageNameList = ArrayList<String>()

    /**当到达目标之后的回调*/
    var onJumpToTarget: (() -> Unit)? = null

    val handler = Handler(Looper.getMainLooper())

    /**等待延迟的任务*/
    var delayRunnable: Runnable? = null

    val filterEventList = ArrayList<FilterEven>()

    var lastAccService: BaseAccessibilityService? = null
    var lastEvent: AccessibilityEvent? = null

    /**过滤包名后的事件*/
    open fun onAccessibilityEvent(accService: BaseAccessibilityService, event: AccessibilityEvent) {
        lastAccService = accService
        lastEvent = AccessibilityEvent.obtain(event)

        filterEvent(accService, event)
    }

    open fun filterEvent(accService: BaseAccessibilityService, event: AccessibilityEvent) {
        var filter = false
        for (bean in filterEventList) {
            if (bean.eventType == event.eventType && bean.className == event.className) {
                val nowTime = nowTime()
                if (nowTime - bean.lastHandlerTime <= bean.delayTime) {
                    filter = true
                } else {
                    bean.lastHandlerTime = nowTime
                }
                break
            }
        }
        if (!filter) {
            onFilterAccessibilityEvent(accService, event)
        }
    }

    /**并发事件过滤后的回调*/
    open fun onFilterAccessibilityEvent(
        accService: BaseAccessibilityService,
        event: AccessibilityEvent
    ) {

    }

    open fun onDestroy() {

    }

    /**切换到了非过滤包名的程序*/
    open fun onLeavePackageName(
        accService: BaseAccessibilityService,
        event: AccessibilityEvent,
        toPackageName: String
    ) {
        //L.i("离开 $filterPackageName -> $toPackageName")
    }

    /**每次延迟, 取消之前的任务*/
    open fun delay(delay: Long, action: () -> Unit) {
        delayRunnable?.let {
            handler.removeCallbacks(it)
        }
        delayRunnable = Runnable {
            action.invoke()
        }
        handler.postDelayed(delayRunnable!!, delay)
    }

    /**相同事件, 过滤短时间内的并发回调*/
    data class FilterEven(
        val eventType: Int,
        val className: String,
        val delayTime: Int /*毫秒*/,
        var lastHandlerTime: Long = 0L /*用来标识最后处理的时间*/
    )
}