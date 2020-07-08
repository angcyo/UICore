package com.angcyo.core.component.accessibility

import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import androidx.annotation.CallSuper
import com.angcyo.core.component.accessibility.BaseAccessibilityInterceptor.Companion.defaultIntervalDelay
import com.angcyo.core.component.accessibility.action.ActionException
import com.angcyo.core.component.accessibility.action.PermissionsAction
import com.angcyo.http.rx.BaseFlowableSubscriber
import com.angcyo.http.rx.flowableToMain
import com.angcyo.http.rx.observer
import com.angcyo.library.L
import com.angcyo.library.component.dslNotify
import com.angcyo.library.component.low
import com.angcyo.library.component.single
import com.angcyo.library.ex.className
import com.angcyo.library.ex.openApp
import com.angcyo.library.ex.simpleHash
import com.angcyo.library.utils.Device
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

        //销毁
        const val ACTION_STATUS_DESTROY = 11

        var defaultIntervalDelay: Long = -1

        init {
            defaultIntervalDelay = when (Device.performanceLevel()) {
                Device.PERFORMANCE_HIGH -> 800
                Device.PERFORMANCE_MEDIUM -> 1_200
                Device.PERFORMANCE_LOW -> 4_000
                else -> 8_000
            }
        }
    }

    /**需要收到那个程序的事件, 匹配方式为 `包含`, 匹配方式为 `全等`*/
    val filterPackageNameList = ArrayList<String>()

    /**忽略[RAccessibilityService]事件处理*/
    var ignoreInterceptor: Boolean = false

    val handler = Handler(Looper.getMainLooper())

    /**等待延迟的任务*/
    var delayRunnable: Runnable? = null

    var lastService: BaseAccessibilityService? = null
    var lastEvent: AccessibilityEvent? = null

    /**需要执行的动作集合*/
    val actionList: MutableList<BaseAccessibilityAction> = mutableListOf()

    /**当当前的[BaseAccessibilityAction]不需要处理[Event]时, 才会执行的[BaseAccessibilityAction]*/
    val actionOtherList: MutableList<BaseAccessibilityAction> = mutableListOf()

    /**当所有的[Action]处理结束后回调*/
    var onInterceptorFinish: ((action: BaseAccessibilityAction?, error: ActionException?) -> Unit)? =
        null

    /**当前执行到动作的索引*/
    var actionIndex: Int = -1

    var actionStatus: Int = ACTION_STATUS_INIT

    /***当所有的[Action]处理结束后(成功和失败), 是否自动卸载拦截器.*/
    var autoUninstall: Boolean = true

    //<editor-fold desc="间隔">

    /**是否激活间隔回调*/
    var enableInterval: Boolean = false
        set(value) {
            field = value
            if (!value) {
                stopInterval()
            }
        }

    //初始化的时间间隔, 用于恢复[intervalDelay]
    var initialIntervalDelay: Long = -1
        set(value) {
            field = value
            intervalDelay = value
        }

    /**间隔回调周期, 根据手机性能自动调整*/
    var intervalDelay: Long = -1
        set(value) {
            if (field != value) {
                field = value
                stopInterval()
                if (enableInterval) {
                    startInterval(value)
                }
            }
        }

    //观察者
    var intervalSubscriber: BaseFlowableSubscriber<Long>? = null

    init {
        initialIntervalDelay = defaultIntervalDelay
    }

    //</editor-fold desc="间隔">

    //<editor-fold desc="周期回调">

    /**无障碍服务连接后*/
    open fun onServiceConnected(service: BaseAccessibilityService) {
        lastService = service

        if (enableInterval) {
            startInterval(intervalDelay)
        }
    }

    /**是否需要拦截指定包名的数据*/
    open fun interceptorPackage(
        service: BaseAccessibilityService,
        event: AccessibilityEvent?,
        packageName: CharSequence?
    ) {
        if (packageName.isNullOrEmpty()) {
            //no op
        } else {
            if (filterPackageNameList.isEmpty() || filterPackageNameList.contains(packageName)) {
                onAccessibilityEvent(service, event)
            } else {
                onLeavePackageName(
                    service,
                    event,
                    event?.packageName ?: service.rootNodeInfo()?.packageName
                )
            }
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

    /**销毁, 释放对象*/
    open fun onDestroy() {
        actionIndex = -1
        actionStatus = ACTION_STATUS_DESTROY
        lastService = null
        lastEvent = null
        stopInterval()
    }

    /**切换到了非过滤包名的程序*/
    open fun onLeavePackageName(
        service: BaseAccessibilityService,
        event: AccessibilityEvent?,
        toPackageName: CharSequence?
    ) {
        //L.i("离开 $filterPackageName -> $toPackageName")
        PermissionsAction().apply {
            doActionWidth(this, service, event)
        }
    }

    /**开始间隔回调*/
    open fun startInterval(delay: Long): Boolean {

        if (intervalSubscriber != null) {
            //已经开启了回调
            return false
        }

        if (delay <= 0) {
            //不合法
            L.w("间隔时长不合法!")
            return false
        }

        if (enableInterval && actionStatus == ACTION_STATUS_ING) {

        } else {
            L.w("请手动调用[startAction]")
            return false
        }

        if (lastService == null) {
            //未连接到服务
            L.w("请注意, 未连接到无障碍服务!")
        }

        intervalSubscriber = BaseFlowableSubscriber<Long>().apply {

            onStart = {
                onIntervalStart()
                //L.i("onStart:${this@BaseAccessibilityInterceptor.simpleHash()} ${nowTimeString()} $intervalDelay")
            }

            onNext = {
                onInterval()
                //L.i("onNext:${this@BaseAccessibilityInterceptor.simpleHash()} ${nowTimeString()} $intervalDelay")
            }

            onObserverEnd = { data, error ->
                onIntervalEnd(data, error)
            }
        }

        Flowable.interval(delay, delay, TimeUnit.MILLISECONDS)
            .onBackpressureLatest()
            .compose(flowableToMain())
            .retry(10)
            .observer(intervalSubscriber!!)

        return true
    }

    /**周期回调开始*/
    open fun onIntervalStart() {

    }

    /**周期回调结束*/
    open fun onIntervalEnd(data: Long?, error: Throwable?) {

    }

    /**间隔周期回调*/
    open fun onInterval() {
        //L.v(this@BaseAccessibilityInterceptor.simpleHash(), " $it")
        val service = lastService
        if (service == null) {
            if (isActionInterceptorStart()) {
                L.w("${this.simpleHash()} service is null.")
            }
        } else {
            service.rootNodeInfo(null)?.let { node ->
                interceptorPackage(service, null, node.packageName)
            }
        }
    }

    /**停止间隔回调*/
    open fun stopInterval() {
        intervalSubscriber?.dispose()
        intervalSubscriber = null
    }

    //</editor-fold desc="周期回调">

    //<editor-fold desc="action">

    /**重新开始*/
    open fun restart() {
        actionIndex = -1
        actionStatus = ACTION_STATUS_INIT
    }

    /**在周期回调模式下, 需要手动调用此方法.开始回调*/
    open fun startAction(restart: Boolean = true) {
        if (restart) {
            restart()
        }

        if (actionStatus != ACTION_STATUS_ING) {
            actionStatus = ACTION_STATUS_ING
            startInterval(intervalDelay)
            onActionStart()
        }
    }

    /**开始执行[Action]*/
    open fun onActionStart() {

    }

    /**所有[Action]执行完成
     * [action] 执行的[Action]
     * [error] 异常, 会中断调用链
     * */
    @CallSuper
    open fun onActionFinish(
        action: BaseAccessibilityAction? = null,
        error: ActionException? = null
    ) {
        if (actionStatus == ACTION_STATUS_ERROR) {
            //出现异常
        } else if (actionStatus == ACTION_STATUS_FINISH) {
            //流程结束
        }
        onInterceptorFinish?.invoke(action, error)
        if (autoUninstall) {
            //注意调用顺序
            uninstall()
        }
    }

    open fun checkDoAction(service: BaseAccessibilityService, event: AccessibilityEvent?) {
        if (actionList.isEmpty() && actionIndex < 0) {
            //no op
            L.w("${this.className()} no action need do. status to [ACTION_STATUS_FINISH].")
            actionStatus = ACTION_STATUS_FINISH
            onActionFinish()
        } else if (actionStatus.isActionCanStart()) {
            if (actionIndex >= actionList.size) {
                actionStatus = ACTION_STATUS_FINISH
                onActionFinish()
            } else {
                if (actionIndex < 0) {
                    actionIndex = 0
                }
                if (actionIndex == 0 ||
                    actionStatus == ACTION_STATUS_INIT
                ) {
                    onActionStart()
                }
                actionStatus = ACTION_STATUS_ING
                actionList.getOrNull(actionIndex)?.let {
                    onDoAction(it, service, event)
                }
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
            if (!action.isActionStart()) {
                action.onActionStart(this)
            }
            if (action.checkEvent(service, event)) {
                //需要事件处理
                action.actionFinish = {
                    //action执行完成
                    if (it != null) {
                        actionStatus = ACTION_STATUS_ERROR
                        onActionFinish(action, it)
                    } else {
                        actionNext(service, event)
                    }
                }
                action.doAction(service, event)
                action.actionFinish = null

                //切换间隔时长
                val interceptorIntervalDelay = action.getInterceptorIntervalDelay()
                intervalDelay = if (interceptorIntervalDelay > 0) {
                    interceptorIntervalDelay
                } else {
                    initialIntervalDelay
                }
            } else {
                //不需要事件处理
                var handle = false
                actionOtherList.forEach {
                    handle = handle || it.doActionWidth(action, service, event)
                }
                action.onCheckEventOut(service, event)
                if (!handle) {
                    //未被处理
                    onNoOtherActionHandle(action, service, event)
                }
            }
        }
    }

    /**下一个[Action]*/
    fun actionNext(service: BaseAccessibilityService, event: AccessibilityEvent?) {
        actionIndex++

        if (enableInterval) {
            //no op, 等待下一个周期回调
        } else {
            handler.post {
                checkDoAction(service, event)
            }
        }
    }

    /**未被[actionOtherList]处理*/
    open fun onNoOtherActionHandle(
        action: BaseAccessibilityAction,
        service: BaseAccessibilityService,
        event: AccessibilityEvent?
    ) {
        if (event != null) {
            L.d("\n${this.simpleHash()} [$actionIndex] 无Action能处理! 包名:${event.packageName} 类名:${event.className} type:${event.eventTypeStr()} type2:${event.contentChangeTypesStr()}")
        } else {
            val node = service.rootNodeInfo()
            if (node != null) {
                L.d("\n${this.simpleHash()} [$actionIndex] 无Action能处理! 包名:${node.packageName} 类名:${node.className} childCount:${node.childCount} windowId:${node.windowId}")
            } else {
                L.d("${this.simpleHash()} [$actionIndex] 无Action能处理!")
            }
        }
    }

    //</editor-fold desc="action">

    //<editor-fold desc="其他">

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

    var notifyId = 0

    /**发送通知*/
    fun sendNotify(title: CharSequence? = null, content: CharSequence? = null) {
        notifyId = dslNotify {
            if (this@BaseAccessibilityInterceptor.notifyId > 0) {
                notifyId = this@BaseAccessibilityInterceptor.notifyId
            }
            notifyOngoing = actionStatus.isActionStart()
            low()
            single(title, content)
        }
    }

    /**操作符重载*/
    operator fun <T : BaseAccessibilityAction> T.invoke(
        inOther: Boolean = false,
        config: T.() -> Unit = {}
    ) {
        this.config()
        if (inOther) {
            actionOtherList.add(this)
        } else {
            actionList.add(this)
        }
    }

    operator fun <T : BaseAccessibilityAction> plus(item: T) {
        actionList.add(item)
    }

    //</editor-fold desc="其他">
}

fun Int.isActionCanStart() =
    this == BaseAccessibilityInterceptor.ACTION_STATUS_INIT || this == BaseAccessibilityInterceptor.ACTION_STATUS_ING

fun Int.isActionInit() = this == BaseAccessibilityInterceptor.ACTION_STATUS_INIT
fun Int.isActionStart() = this == BaseAccessibilityInterceptor.ACTION_STATUS_ING
fun Int.isActionFinish() = this == BaseAccessibilityInterceptor.ACTION_STATUS_FINISH
fun Int.isActionError() = this == BaseAccessibilityInterceptor.ACTION_STATUS_ERROR
fun Int.isActionDestroy() = this == BaseAccessibilityInterceptor.ACTION_STATUS_DESTROY

fun Int.toActionStatusStr() = when (this) {
    BaseAccessibilityInterceptor.ACTION_STATUS_INIT -> "ACTION_STATUS_INIT"
    BaseAccessibilityInterceptor.ACTION_STATUS_ING -> "ACTION_STATUS_ING"
    BaseAccessibilityInterceptor.ACTION_STATUS_FINISH -> "ACTION_STATUS_FINISH"
    BaseAccessibilityInterceptor.ACTION_STATUS_ERROR -> "ACTION_STATUS_ERROR"
    BaseAccessibilityInterceptor.ACTION_STATUS_DESTROY -> "ACTION_STATUS_DESTROY"
    else -> "Unknown:$this"
}

fun BaseAccessibilityInterceptor.isActionInterceptorStart() = actionStatus.isActionStart()

/**安装拦截器*/
fun BaseAccessibilityInterceptor.install(start: Boolean = false, restart: Boolean = false) {
    RAccessibilityService.addInterceptor(this)
    if (start) {
        startAction(restart)
    }
}

fun BaseAccessibilityInterceptor.run(
    delay: Long,
    start: Boolean = true,
    restart: Boolean = true
) {
    initialIntervalDelay = if (delay > 0) delay else defaultIntervalDelay
    install(start, restart)
}

/**卸载拦截器*/
fun BaseAccessibilityInterceptor.uninstall() {
    RAccessibilityService.removeInterceptor(this)
}

/**进入周期回调模式, 每隔[intervalDelay]时间回调一次*/
fun BaseAccessibilityInterceptor.intervalMode(delay: Long = intervalDelay) {
    enableInterval = false

    ignoreInterceptor = true
    initialIntervalDelay = delay
    enableInterval = true
}

/**进入普通的事件拦截模式, 当收到[AccessibilityEvent]事件时, 回调*/
fun BaseAccessibilityInterceptor.interceptorMode(vararg filterPackage: String) {
    ignoreInterceptor = false
    enableInterval = false

    filterPackage.forEach {
        filterPackageNameList.add(it)
    }
}

/**打开指定app*/
fun BaseAccessibilityInterceptor.openApp(packageName: String? = lastService?.packageName) {
    lastService?.openApp(packageName)
}