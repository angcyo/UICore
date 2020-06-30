package com.angcyo.core.component.accessibility

import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import androidx.annotation.CallSuper
import com.angcyo.http.rx.BaseFlowableSubscriber
import com.angcyo.http.rx.flowableToMain
import com.angcyo.library.L
import com.angcyo.library.ex.className
import com.angcyo.library.ex.simpleHash
import io.reactivex.Flowable
import java.util.concurrent.TimeUnit

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2018/01/26 08:57
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
abstract class BaseAccessibilityInterceptor {

    companion object {
        //初始化
        const val ACTION_STATUS_INIT = 1

        //进行中
        const val ACTION_STATUS_ING = 2

        //完成
        const val ACTION_STATUS_FINISH = 3

        //错误
        const val ACTION_STATUS_ERROR = 10
    }

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

    var actionStatus: Int = ACTION_STATUS_INIT

    //<editor-fold desc="间隔">

    /**是否激活间隔回调*/
    var enableInterval: Boolean = false
        set(value) {
            field = value
            if (value) {
                startInterval()
            } else {
                stopInterval()
            }
        }

    /**间隔回调周期*/
    var intervalDelay: Long = 2_000
        set(value) {
            field = value
            stopInterval()
            if (enableInterval) {
                startInterval()
            }
        }

    //观察者
    var intervalSubscriber: BaseFlowableSubscriber<Long>? = null

    //</editor-fold desc="间隔">

    //<editor-fold desc="周期回调">

    /**无障碍服务连接后*/
    open fun onServiceConnected(service: BaseAccessibilityService) {
        lastService = service

        if (enableInterval) {
            startInterval()
        }
    }

    /**过滤包名后的事件
     * [event] 如果是程序主动发送过来的无障碍通知, 那么就会有值. 如果是定时器触发的回调, 就没有值.
     * */
    open fun onAccessibilityEvent(service: BaseAccessibilityService, event: AccessibilityEvent?) {
        lastService = service

        if (event != null) {
            lastEvent = AccessibilityEvent.obtain(event)
        }

        checkDoAction(service, event)
    }

    open fun onDestroy() {
        intervalDelay = -1
        lastService = null
        lastEvent = null
        stopInterval()
    }

    /**切换到了非过滤包名的程序*/
    open fun onLeavePackageName(
        accService: BaseAccessibilityService,
        event: AccessibilityEvent,
        toPackageName: String
    ) {
        //L.i("离开 $filterPackageName -> $toPackageName")
    }

    /**开始间隔回调*/
    open fun startInterval() {
//        Flowable.create(intervalSubscriber, BackpressureStrategy.MISSING)
//            .compose(flowableToMain())
//            .subscribe()

        if (intervalSubscriber != null || lastService == null) {
            return
        }

        intervalSubscriber = BaseFlowableSubscriber<Long>().apply {
            onNext = {
                lastService?.let {
                    onAccessibilityEvent(it, null)
                }
            }
        }

        Flowable.interval(intervalDelay, intervalDelay, TimeUnit.MILLISECONDS)
            .onBackpressureLatest()
            .compose(flowableToMain())
            .subscribe(intervalSubscriber)
    }

    open fun stopInterval() {
        intervalSubscriber?.dispose()
        intervalSubscriber = null
    }

    //</editor-fold desc="周期回调">

    //<editor-fold desc="action">

    /**所有Action执行完成*/
    @CallSuper
    open fun onActionFinish() {

    }

    open fun checkDoAction(service: BaseAccessibilityService, event: AccessibilityEvent?) {
        if (actionList.isEmpty() && actionIndex < 0) {
            //no op
            L.w("${this.className()} no action need do.")
        } else if (actionStatus.isActionCanStart()) {
            if (actionIndex >= actionList.size) {
                actionStatus = ACTION_STATUS_FINISH
                actionIndex = -1
                onActionFinish()
            } else {
                actionStatus = ACTION_STATUS_ING
                if (actionIndex < 0) {
                    actionIndex = 0
                }
                onDoAction(actionList[actionIndex], service, event)
            }
        } else {
            //no op
        }
    }

    open fun onDoAction(
        action: BaseAccessibilityAction,
        service: BaseAccessibilityService?,
        event: AccessibilityEvent?
    ) {
        if (service != null) {
            if (action.checkEvent(service, event)) {
                //需要事件处理
                action.actionFinish = {
                    //action执行完成
                    if (it) {
                        actionStatus = ACTION_STATUS_ERROR
                    }
                    actionIndex++
                    handler.post {
                        checkDoAction(service, event)
                    }
                }
                action.doAction(service, event)
                action.actionFinish = null
            } else {
                //不需要事件处理
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
    open fun onNoOtherActionHandle(service: BaseAccessibilityService, event: AccessibilityEvent?) {
        if (event != null) {
            L.i("\n${this.simpleHash()} [$actionIndex] 无Action能处理! 包名:${event.packageName} 类名:${event.className} type:${event.eventTypeStr()} type2:${event.contentChangeTypesStr()}")
        } else {
            val node = service.rootNodeInfo()
            if (node != null) {
                L.i("\n${this.simpleHash()} [$actionIndex] 无Action能处理! 包名:${node.packageName} 类名:${node.className} childCount:${node.childCount} windowId:${node.windowId}")
            } else {
                L.i("${this.simpleHash()} [$actionIndex] 无Action能处理!")
            }
        }
    }

    //</editor-fold desc="action">

    /**每次延迟, 取消之前的任务*/
    open fun delay(delay: Long = 300, skipExist: Boolean = true, action: () -> Unit) {
        if (skipExist) {
            if (delayRunnable != null) {
                return
            }
        }

        delayRunnable?.let {
            handler.removeCallbacks(it)
        }
        delayRunnable = Runnable {
            action.invoke()
        }
        handler.postDelayed(delayRunnable!!, delay)
    }
}

fun Int.isActionCanStart() =
    this == BaseAccessibilityInterceptor.ACTION_STATUS_INIT || this == BaseAccessibilityInterceptor.ACTION_STATUS_ING

/**安装拦截器*/
fun BaseAccessibilityInterceptor.install() {
    RAccessibilityService.addInterceptor(this)
}

fun BaseAccessibilityInterceptor.uninstall() {
    RAccessibilityService.removeInterceptor(this)
}