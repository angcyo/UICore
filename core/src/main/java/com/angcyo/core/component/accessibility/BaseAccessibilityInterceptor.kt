package com.angcyo.core.component.accessibility

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.CallSuper
import com.angcyo.core.component.accessibility.BaseAccessibilityInterceptor.Companion.defaultIntervalDelay
import com.angcyo.core.component.accessibility.action.ActionException
import com.angcyo.core.component.accessibility.action.PermissionsAction
import com.angcyo.library.L
import com.angcyo.library.component.dslNotify
import com.angcyo.library.component.low
import com.angcyo.library.component.single
import com.angcyo.library.ex.nowTime
import com.angcyo.library.ex.openApp
import com.angcyo.library.ex.simpleHash
import com.angcyo.library.ex.toElapsedTime
import com.angcyo.library.utils.Device

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2018/01/26 08:57
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
abstract class BaseAccessibilityInterceptor : Runnable {

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
                Device.PERFORMANCE_HIGH -> 1_500
                Device.PERFORMANCE_MEDIUM -> 2_500
                Device.PERFORMANCE_LOW -> 5_000
                else -> 8_000
            }
        }
    }

    /**需要收到那个程序的事件, 匹配方式为 `包含`, 匹配方式为 `全等`*/
    val filterPackageNameList = ArrayList<String>()

    /**忽略[RAccessibilityService]事件处理*/
    var ignoreInterceptor: Boolean = false

    val handler = Handler(Looper.getMainLooper())

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

    //初始化的时间间隔, 用于恢复[intervalDelay]
    var initialIntervalDelay: Long = -1
        set(value) {
            field = value
            intervalDelay = value
        }

    /**间隔回调周期, 根据手机性能自动调整*/
    var intervalDelay: Long = -1

    init {
        initialIntervalDelay = defaultIntervalDelay
    }

    //</editor-fold desc="间隔">

    //<editor-fold desc="AccessibilityService 回调">

    /**无障碍服务连接后
     * [com.angcyo.core.component.accessibility.RAccessibilityService.onServiceConnected]*/
    open fun onServiceConnected(service: BaseAccessibilityService) {
        lastService = service
    }

    /** [com.angcyo.core.component.accessibility.RAccessibilityService.onAccessibilityEvent]*/
    open fun onAccessibilityEvent(service: BaseAccessibilityService, event: AccessibilityEvent) {
        //防止服务已经开启后, 再追加的拦截器
        lastService = service
        lastEvent = event //AccessibilityEvent.obtain(event)

        if (ignoreInterceptor) {
            if (event.isWindowStateChanged()) {
                //处理窗口切换事件
                handleAccessibility(service, ignoreInterceptor)
            }
        } else {
            handleAccessibility(service, ignoreInterceptor)
        }
    }

    //</editor-fold desc="AccessibilityService 回调">

    //<editor-fold desc="周期回调">

    /**[ignore] 是否要忽略此次处理. 忽略处理之后, 仅会检查是否离开了当前界面
     * [onAccessibilityEvent] | [onInterval] ->[handleAccessibility]->[handleFilterNode]->[onDoActionStart]->[onDoAction]->[onDoActionFinish]
     * */
    open fun handleAccessibility(service: BaseAccessibilityService, ignore: Boolean) {
        val findNodeInfoList = service.findNodeInfoList()

        //当前页面主要的程序
        val mainPackageName = findNodeInfoList.mainNode()?.packageName
        //L.e("main:$mainPackageName")

        if (filterPackageNameList.isEmpty()) {
            //所有包名都需要
            handleFilterNode(service, findNodeInfoList)
        } else {
            //需要处理的报名列表
            val needNodeList = findNodeInfoList.filter(filterPackageNameList)
            val needMainPackageName = needNodeList.mainNode()?.packageName

            if (needNodeList.isEmpty()) {
                //当前主界面, 不在处理的列表中
                checkLeave(service, mainPackageName, needNodeList)
            } else {
                if (ignore) {
                    //忽略事件
                    checkLeave(service, needMainPackageName, needNodeList)
                } else {
                    //需要的所有节点
                    _lastLeavePackageName = needMainPackageName //todo 是否需要直接赋值?
                    handleFilterNode(service, needNodeList)
                }
            }
        }
    }

    //最后一次离开前的程序
    var _lastLeavePackageName: CharSequence? = null

    /**检查是否离开了界面*/
    open fun checkLeave(
        service: BaseAccessibilityService,
        mainPackageName: CharSequence?,
        nodeList: List<AccessibilityNodeInfo>
    ): Boolean {
        return if (_lastLeavePackageName != mainPackageName) {
            val old = _lastLeavePackageName
            _lastLeavePackageName = mainPackageName
            onLeavePackageName(service, old, mainPackageName, nodeList)
            true
        } else {
            false
        }
    }

    /**处理需要的节点[AccessibilityNodeInfo]列表*/
    open fun handleFilterNode(
        service: BaseAccessibilityService,
        nodeList: List<AccessibilityNodeInfo>
    ) {
        if (actionList.isEmpty() && actionIndex < 0) {
            //no op
            L.w("${this.simpleHash()} no action need do. status to [ACTION_STATUS_FINISH].")
            actionStatus = ACTION_STATUS_FINISH
            onDoActionFinish()
        } else if (actionStatus.isActionCanStart()) {
            if (actionIndex >= actionList.size) {
                actionStatus = ACTION_STATUS_FINISH
                onDoActionFinish()
            } else {
                if (actionIndex < 0) {
                    //开始执行第一步
                    actionIndex = 0
                } else {
                    if (actionIndex == 0 || actionStatus == ACTION_STATUS_INIT) {
                        onDoActionStart()
                    }
                    actionStatus = ACTION_STATUS_ING
                    actionList.getOrNull(actionIndex)?.let {
                        onDoAction(it, service, nodeList)
                    }
                }
            }
        } else {
            //no op
        }
    }

    /**销毁, 释放对象
     * [com.angcyo.core.component.accessibility.RAccessibilityService.onDestroy]*/
    open fun onDestroy() {
        L.w("销毁:[$actionIndex/${actionList.size}] 耗时:${(nowTime() - _actionStartTime).toElapsedTime()}")
        actionIndex = -1
        actionStatus = ACTION_STATUS_DESTROY
        lastService = null
        lastEvent = null
        stopInterval()
    }

    /**切换到了非过滤包名的程序*/
    open fun onLeavePackageName(
        service: BaseAccessibilityService,
        fromPackageName: CharSequence?,
        toPackageName: CharSequence?,
        nodeList: List<AccessibilityNodeInfo>
    ) {
        L.e("${this.simpleHash()} 离开从:${fromPackageName}->$toPackageName")
        //L.i("离开 $filterPackageName -> $toPackageName")
        PermissionsAction().apply {
            doActionWidth(this, service, lastEvent, nodeList)
        }
    }

    /**开始间隔回调*/
    open fun startInterval(delay: Long): Boolean {
        if (delay <= 0) {
            //不合法
            L.w("间隔时长不合法!")
            return false
        }
        stopInterval()
        onIntervalStart(delay)
        return true
    }

    override fun run() {
        try {
            onInterval()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (enableInterval && actionStatus == ACTION_STATUS_ING) {
            startInterval(intervalDelay)
        } else {
            onIntervalEnd()
        }
    }

    /**周期回调开始*/
    open fun onIntervalStart(delay: Long) {
        //延迟[delay] 执行下一次.
        handler.postDelayed(this, delay)
    }

    /**周期回调结束*/
    open fun onIntervalEnd() {

    }

    /**间隔周期回调 */
    open fun onInterval() {
        //L.v(this@BaseAccessibilityInterceptor.simpleHash(), " $it")
        val service = lastService
        if (service == null) {
            if (isActionInterceptorStart()) {
                L.w("${this.simpleHash()} service is null.")
            }
        } else {
            handleAccessibility(service, false)
        }
    }

    /**停止间隔回调*/
    open fun stopInterval() {
        handler.removeCallbacks(this)
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
            onDoActionStart()
        }
    }

    /**记录开始的时间*/
    var _actionStartTime = -1L

    /**开始执行[Action]*/
    open fun onDoActionStart() {
        _actionStartTime = nowTime()
    }

    /**所有[Action]执行完成
     * [action] 执行的[Action]
     * [error] 异常, 会中断调用链
     * */
    @CallSuper
    open fun onDoActionFinish(
        action: BaseAccessibilityAction? = null,
        error: ActionException? = null
    ) {
        L.w("${action?.simpleHash()} [${action?.actionTitle}] 执行结束:${actionStatus.toActionStatusStr()} ${error ?: ""} 耗时:${(nowTime() - _actionStartTime).toElapsedTime()}")
        if (actionStatus == ACTION_STATUS_ERROR) {
            //出现异常
        } else if (actionStatus == ACTION_STATUS_FINISH) {
            //流程结束
        }
        onInterceptorFinish?.invoke(action, error)
        onInterceptorFinish = null
        if (autoUninstall) {
            //注意调用顺序
            uninstall()
        }
    }

    //执行当前的[action]
    open fun onDoAction(
        action: BaseAccessibilityAction,
        service: BaseAccessibilityService,
        nodeList: List<AccessibilityNodeInfo>
    ) {
        if (!action.isActionStart()) {
            L.v("${action.simpleHash()} [${action.actionTitle}] 开始.")
            action.onActionStart(this)
            action.actionFinish = {
                //action执行完成
                L.v("${action.simpleHash()} [${action.actionTitle}] ${if (it == null) "完成" else it.message}:${actionIndex}/${actionList.size}")
                if (it != null) {
                    actionStatus = ACTION_STATUS_ERROR
                    onDoActionFinish(action, it)
                } else {
                    actionNext(service)
                }
            }
        }
        if (action.checkEvent(service, lastEvent, nodeList)) {
            //需要事件处理
            action.doAction(service, lastEvent, nodeList)
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
                handle = handle || it.doActionWidth(action, service, lastEvent, nodeList)
            }
            if (!handle) {
                //未被处理
                action.onCheckEventOut(service, lastEvent, nodeList)
                onNoOtherActionHandle(action, service, lastEvent, nodeList)
            }
        }
    }

    /**下一个[Action]*/
    fun actionNext(service: BaseAccessibilityService) {
        actionIndex++

        if (enableInterval) {
            //no op, 等待下一个周期回调
        } else {
            handler.post {
                handleAccessibility(service, false)
            }
        }
    }

    /**未被[actionOtherList]处理*/
    open fun onNoOtherActionHandle(
        action: BaseAccessibilityAction,
        service: BaseAccessibilityService,
        event: AccessibilityEvent?,
        nodeList: List<AccessibilityNodeInfo>
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
fun BaseAccessibilityInterceptor.openApp(
    packageName: String? = lastService?.packageName,
    flags: Int = Intent.FLAG_ACTIVITY_SINGLE_TOP
) {
    lastService?.openApp(packageName, flags = flags)
}