package com.angcyo.core.component.accessibility

import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import androidx.annotation.CallSuper
import com.angcyo.library.L

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

    val handler = Handler(Looper.getMainLooper())

    /**等待延迟的任务*/
    var delayRunnable: Runnable? = null

    var lastService: BaseAccessibilityService? = null
    var lastEvent: AccessibilityEvent? = null

    /**需要执行的动作集合*/
    val actionList: MutableList<BaseAccessibilityAction> = mutableListOf()

    /**当当前的[BaseAccessibilityAction]不需要处理[Event]时, 才会执行的[BaseAccessibilityAction]*/
    val actionOtherList: MutableList<BaseAccessibilityAction> = mutableListOf()

    /**当前执行到动作的索引*/
    var actionIndex: Int = -1

    //<editor-fold desc="周期回调">

    /**无障碍服务连接后*/
    open fun onServiceConnected(service: BaseAccessibilityService) {

    }

    /**过滤包名后的事件*/
    open fun onAccessibilityEvent(service: BaseAccessibilityService, event: AccessibilityEvent) {
        lastService = service
        lastEvent = event //AccessibilityEvent.obtain(event)

        //filterEvent(service, event)

        if (actionList.isEmpty()) {
            if (actionIndex != -1) {
                onActionFinish()
            }
        } else {
            checkDoAction()
        }
    }

    //</editor-fold desc="周期回调">

    //<editor-fold desc="action">

    /**所有Action执行完成*/
    @CallSuper
    open fun onActionFinish() {
        actionIndex = -1
    }

    open fun checkDoAction() {
        if (actionIndex == actionList.size) {
            onActionFinish()
        } else {
            if (actionIndex < 0) {
                actionIndex = 0
            }
            onDoAction(actionList[actionIndex])
        }
    }

    open fun onDoAction(action: BaseAccessibilityAction) {
        val service = lastService
        val event = lastEvent

        if (service != null && event != null) {
            if (action.checkEvent(service, event)) {
                //需要事件
                action.actionFinish = {
                    //action执行完成
                    actionIndex++
                    if (actionIndex == actionList.size) {
                        onActionFinish()
                    }
                }
                action.doAction(service, event)
                action.actionFinish = null
            } else {
                //不需要处理
                L.i("[$actionIndex]无Action能处理! 包名:${event.packageName} 类名:${event.className} type:${event.eventTypeStr()} type2:${event.contentChangeTypesStr()}")
                var handle = false
                actionOtherList.forEach {
                    handle = handle || it.doActionWidth(action, service, event)
                }
                if (!handle) {
                    //未被处理
                    onNoOtherActionHandle(service, event)
                }
            }
        }
    }

    /**未被[actionOtherList]处理*/
    open fun onNoOtherActionHandle(service: BaseAccessibilityService, event: AccessibilityEvent) {

    }

    //</editor-fold desc="action">


//    open fun filterEvent(service: BaseAccessibilityService, event: AccessibilityEvent) {
//        var filter = false
//        for (bean in filterEventList) {
//            if (bean.eventType == event.eventType && bean.className == event.className) {
//                val nowTime = nowTime()
//                if (nowTime - bean.lastHandlerTime <= bean.delayTime) {
//                    filter = true
//                } else {
//                    bean.lastHandlerTime = nowTime
//                }
//                break
//            }
//        }
//        if (!filter) {
//            onFilterAccessibilityEvent(service, event)
//        }
//    }

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

//    /**相同事件, 过滤短时间内的并发回调*/
//    data class FilterEven(
//        val eventType: Int,
//        val className: String,
//        val delayTime: Int /*毫秒*/,
//        var lastHandlerTime: Long = 0L /*用来标识最后处理的时间*/
//    )
}

/**安装拦截器*/
fun BaseAccessibilityInterceptor.install() {
    RAccessibilityService.addInterceptor(this)
}

fun BaseAccessibilityInterceptor.uninstall() {
    RAccessibilityService.removeInterceptor(this)
}