package com.angcyo.core.component.accessibility

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.CallSuper
import com.angcyo.acc.contentChangeTypesStr
import com.angcyo.acc.eventTypeStr
import com.angcyo.acc.findNodeInfoList
import com.angcyo.acc.rootNodeInfo
import com.angcyo.core.component.accessibility.BaseAccessibilityAction.Companion.parseInterceptorIntervalDelay
import com.angcyo.core.component.accessibility.BaseAccessibilityInterceptor.Companion.defaultIntervalDelay
import com.angcyo.core.component.accessibility.action.*
import com.angcyo.core.component.accessibility.base.AccessibilityWindow
import com.angcyo.core.component.accessibility.parse.ActionBean
import com.angcyo.library.L
import com.angcyo.library.component.dslNotify
import com.angcyo.library.component.low
import com.angcyo.library.component.single
import com.angcyo.library.ex.*
import com.angcyo.library.utils.Device
import kotlin.math.max

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2018/01/26 08:57
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
abstract class BaseAccessibilityInterceptor : Runnable {

    companion object {

        const val ACTION_STATUS_INIT = 1           //初始化
        const val ACTION_STATUS_ING = 2            //进行中
        const val ACTION_STATUS_FINISH = 10         //完成
        const val ACTION_STATUS_ERROR = 11         //错误
        const val ACTION_STATUS_INTERRUPTED = 12   //中断
        const val ACTION_STATUS_DESTROY = 13       //销毁

        /**根据设备性能, 算出来的时间间隔*/
        var defaultIntervalDelay: Long = -1

        init {
//            defaultIntervalDelay = if (isDebugType()) {
//                when (Device.performanceLevel()) {
//                    Device.PERFORMANCE_HIGH -> 600
//                    Device.PERFORMANCE_MEDIUM -> 800
//                    Device.PERFORMANCE_LOW -> 1_200
//                    else -> 1_500
//                }
//            } else {
//                when (Device.performanceLevel()) {
//                    Device.PERFORMANCE_HIGH -> 800
//                    Device.PERFORMANCE_MEDIUM -> 1200
//                    Device.PERFORMANCE_LOW -> 1_500
//                    else -> 2_000
//                }
//            }

            defaultIntervalDelay = when (Device.performanceLevel()) {
                Device.PERFORMANCE_HIGH -> 800
                Device.PERFORMANCE_MEDIUM -> 1200
                Device.PERFORMANCE_LOW -> 1_500
                else -> 2_000
            }
        }

        /**当前主界面的程序包名*/
        var _lastPackageName: String? = null
    }

    /**需要收到那个程序的事件, 匹配方式为 `包含`, 匹配方式为 `全等`*/
    val filterPackageNameList = ArrayList<String>()

    /**只获取同一个包名程序的最上层window的node信息*/
    var onlyFilterTopWindow: Boolean = false
        get() {
            val action = currentAccessibilityAction
            if (action is AutoParseAction) {
                return action.actionBean?.onlyTopWindow ?: field
            }
            return field
        }

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

    /**指定了下一个需要执行的的[BaseAccessibilityAction]*/
    var _targetAction: BaseAccessibilityAction? = null

    var runActionStatus: Int = ACTION_STATUS_INIT

    /***当所有的[Action]处理结束后(成功和失败), 是否自动卸载拦截器.*/
    var autoUninstall: Boolean = true

    //<editor-fold desc="间隔">

    /**是否激活间隔回调*/
    var enableInterval: Boolean = false

    //初始化的时间间隔, 用于恢复[intervalDelay]
    var initialIntervalDelay: Long = -1
        set(value) {
            field = value
            if (intervalDelay == -1L) {
                intervalDelay = value
            }
        }

    /**间隔回调周期, 根据手机性能自动调整*/
    var intervalDelay: Long = -1

    /**[com.angcyo.core.component.accessibility.parse.ConstraintBean.ACTION_SLEEP]指令, 指定的间隔时间*/
    var sleepIntervalDelay: String? = null

    /**当无法处理[AccessibilityEvent]时*/
    var onNoOtherEventHandleAction: ((service: BaseAccessibilityService, mainNode: AccessibilityNodeInfo) -> Unit)? =
        null

    /**日志输出*/
    var interceptorLog: ILogPrint? = ILogPrint()

    /**下一个周期延迟时间的获取*/
    var onHandleIntervalDelay: (actionIndex: Int) -> Long = { actionIndex ->
        computeIntervalDelay(actionIndex)
    }

    /**当前是否在目标的程序界面内*/
    var _isInFilterPackageNameApp = false

    /**拦截器离开过滤界面的计数统计*/
    var interceptorLeaveCount: ActionCount = ActionCount().apply {
        maxCountLimit = BaseAccessibilityAction.DEFAULT_INTERCEPTOR_LEAVE_COUNT
    }

    /**[onDoAction]流程监督*/
    val _actionControl = ActionControl()

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
            if (event.isWindowStateChanged() && !enableInterval) {
                //在未开启周期循环的情况下, 处理窗口切换事件, 否则周期回调中会触发[handleAccessibility]
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
        val findNodeInfoList = service.findNodeInfoList(onlyTopWindow = onlyFilterTopWindow)

        //当前页面主要的程序
        val mainPackageName = findNodeInfoList.mainNode()?.packageName
        //L.e("main:$mainPackageName")

        mainPackageName?.it { _lastPackageName = it.str() }

        if (filterPackageNameList.isEmpty()) {
            _isInFilterPackageNameApp = true

            //track
            _trackPackage(mainPackageName)

            //所有包名都需要
            handleFilterNode(service, findNodeInfoList)
        } else {
            _isInFilterPackageNameApp = filterPackageNameList.contains(mainPackageName) ||
                    findNodeInfoList.find { filterPackageNameList.contains(it.packageName) } != null

            //需要处理包名对应节点的列表
            val needNodeList = findNodeInfoList.filter(filterPackageNameList)//过滤后的应用程序节点列表
            val needMainPackageName = needNodeList.mainNode()?.packageName

            //track
            _trackPackage(needMainPackageName ?: mainPackageName)

            if (needNodeList.isEmpty()) {
                //当前主界面, 不在处理的列表中
                checkLeave(service, mainPackageName, findNodeInfoList)
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

    //跟踪包名
    fun _trackPackage(packageName: CharSequence?) {
        //track
        if (_packageTrackList.lastOrNull() != packageName) {
            _packageTrackList.add(packageName)
        }
    }

    //记录所有切换过的包名
    val _packageTrackList: MutableList<CharSequence?> = mutableListOf()

    //最后一次离开前的程序
    var _lastLeavePackageName: CharSequence? = null

    //离开的时间
    var _lastLeaveTime: Long = 0

    /**当前正在执行的[ActionBean]*/
    val currentAccessibilityAction: BaseAccessibilityAction?
        get() = actionList.getOrNull(actionIndex)

    /**检查是否离开了界面*/
    @CallSuper
    open fun checkLeave(
        service: BaseAccessibilityService,
        mainPackageName: CharSequence?,
        nodeList: List<AccessibilityNodeInfo>
    ): Boolean {
        var handle = false
        //检查当前的action,是否需要突破当前[interceptor]的包名限制
        currentAccessibilityAction?.let { action ->
            if (action is AutoParseAction) {
                val specifyPackageNameList = action.actionBean?.check?.packageName?.split(";")
                if (specifyPackageNameList != null) {

                    if (specifyPackageNameList.isListEmpty() ||
                        (specifyPackageNameList.size == 1 &&
                                specifyPackageNameList.firstOrNull().isNullOrEmpty())
                    ) {
                        handle = true
                        stopInterval()
                        onDoAction(
                            action,
                            service,
                            service.findNodeInfoList(onlyTopWindow = onlyFilterTopWindow)
                        )
                        if (enableInterval && runActionStatus == ACTION_STATUS_ING) {
                            interceptorLog?.log("拦截器恢复,下一个周期在 ${intervalDelay}ms!")
                            startInterval(intervalDelay)
                        }
                    }
                }

                if (!handle) {
                    //离开主程序后, 未被处理的事件
                    actionOtherList.forEach {
                        if (it is AutoParseAction) {
                            if (it.actionBean?.check?.packageName?.isEmpty() == true) {
                                //空字符的包名, 才允许处理

                                val handleNodeList = if (it.actionBean?.onlyTopWindow == true) {
                                    service.findNodeInfoList(onlyTopWindow = true)
                                } else {
                                    nodeList
                                }

                                handle = it.doActionWidth(
                                    action,
                                    service,
                                    lastEvent,
                                    handleNodeList
                                ) || handle
                            }
                        }
                    }
                }

                if (!handle) {
                    interceptorLeaveCount.start()
                    val leaveCount = action.actionBean?.leaveCount ?: -1
                    if (action.actionBean?.check?.leaveOut != null &&
                        interceptorLeaveCount.count + 1 >= leaveCount
                    ) {
                        //到达阈值
                        handle = action.parseHandleAction(
                            service,
                            currentAccessibilityAction,
                            nodeList,
                            action.actionBean?.check?.leaveOut
                        )
                        if (handle) {
                            //处理了leave, 清空计数
                            interceptorLeaveCount.clear()
                        }
                    }
                }
            }
        }

        if (!handle) {
            interceptorLeaveCount.doCount()
        }

        return if (_lastLeavePackageName != mainPackageName) {
            _lastLeaveTime = nowTime()
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
        //清空计数
        interceptorLeaveCount.clear()

        if (actionList.isEmpty() && actionIndex < 0) {
            //no op
            L.w("${this.simpleHash()} no action need do. status to [ACTION_STATUS_FINISH].")
            runActionStatus = ACTION_STATUS_FINISH
            onDoActionFinish()
        } else if (runActionStatus.isActionCanStart()) {
            if (actionIndex >= actionList.size) {
                runActionStatus = ACTION_STATUS_FINISH
                onDoActionFinish()
            } else {
                if (actionIndex < 0) {
                    //开始执行第一步
                    actionIndex = 0
                } else {
                    if (actionIndex == 0 || runActionStatus == ACTION_STATUS_INIT) {
                        onDoActionStart()
                    }
                    runActionStatus = ACTION_STATUS_ING
                    currentAccessibilityAction?.let {
                        if (it is AutoParseAction) {
                            val packageName = it.actionBean?.check?.packageName
                            val specifyPackageNameList = packageName?.split(";")
                            if (packageName.isNullOrEmpty() || specifyPackageNameList.isListEmpty()) {
                                //没有指定需要强制过滤的包名, 则执行对应的action
                                onDoAction(it, service, nodeList)
                            } else {
                                val findNodeInfoList =
                                    service.findNodeInfoList(onlyTopWindow = onlyFilterTopWindow)
                                val currentPackageName = findNodeInfoList.mainNode()?.packageName

                                if (specifyPackageNameList!!.contains(currentPackageName)) {
                                    //强制指定了要处理的包名
                                    onDoAction(it, service, findNodeInfoList)
                                } else {
                                    //指定了需要强制过滤的包名, 但是当前界面不是此应用
                                    L.w("指定处理的包名:$specifyPackageNameList 当前包名:$currentPackageName")
                                }
                            }
                        } else {
                            onDoAction(it, service, nodeList)
                        }
                    }
                }
            }
        } else {
            //no op
        }
    }

    /**销毁, 释放对象
     * [com.angcyo.core.component.accessibility.RAccessibilityService.onDestroy]*/
    open fun onDestroy(reason: String?) {

        actionList.forEach {
            //释放资源
            it.release()
        }

        if (runActionStatus != ACTION_STATUS_ING) {
            L.w("销毁${this.simpleHash()} $reason")
        } else {
            L.w("销毁${this.simpleHash()}:[$actionIndex/${actionList.size}] 耗时:${(nowTime() - _actionStartTime).toElapsedTime()} $reason")
        }
        //actionIndex = -1 //不重置index, 这样可以支持回复
        runActionStatus = ACTION_STATUS_DESTROY
        lastService = null
        lastEvent = null
        interceptorLog = null
        onNoOtherEventHandleAction = null
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
        stopInterval()
        onIntervalStart(max(delay, 0))
        return true
    }

    var _tempIntervalDelay = 0L

    override fun run() {
        try {
            onInterval()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        //处理[com.angcyo.core.component.accessibility.parse.ConstraintBean.ACTION_JUMP]指令
        val action = _targetAction
        if (action != null) {
            //指定了下一个需要执行的[BaseAccessibilityAction]
            val indexOf = actionList.indexOf(action)
            if (indexOf != -1) {
                actionIndex = indexOf

                _tempIntervalDelay = nextSleepIntervalDelay(onHandleIntervalDelay(actionIndex))
            } else {
                L.w("${this.simpleHash()} 指定需要运行的action:${action.simpleHash()} 不存在.".apply {
                    interceptorLog?.log(this)
                })
            }

            //清空
            _targetAction = null
        } else {
            _tempIntervalDelay = nextSleepIntervalDelay(intervalDelay)
        }

        //下一个周期
        if (enableInterval && runActionStatus == ACTION_STATUS_ING) {
            startInterval(_tempIntervalDelay)
        } else {
            interceptorLog?.log("${this.simpleHash()} 拦截器,周期回调结束!")
            onIntervalEnd()
        }
    }

    /**周期回调开始*/
    open fun onIntervalStart(delay: Long) {
        //延迟[delay] 执行下一次.
        handler.postDelayed(this, delay)

        interceptorLog?.log(buildString {
            append(this@BaseAccessibilityInterceptor.hashCode())
            append("[$actionIndex/${actionList.size}]")
            append(" 拦截器,下一个周期在 ${delay}ms! $intervalDelay")
        })
    }

    /**周期回调结束*/
    open fun onIntervalEnd() {

    }

    /**间隔周期回调
     * [run] -> [onInterval] -> [handleAccessibility] -> [handleFilterNode] -> [onDoAction]
     * */
    open fun onInterval() {
        //interceptorLog?.log(this@BaseAccessibilityInterceptor.simpleHash(), " $it")
        val service = lastService
        if (service == null) {
            if (isActionInterceptorStart()) {
                actionError(currentAccessibilityAction, ActionException("service is null"))
                L.w("${this.simpleHash()} service is null.".apply {
                    interceptorLog?.log(this)
                })
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
        runActionStatus = ACTION_STATUS_INIT
    }

    /**在周期回调模式下, 需要手动调用此方法.开始回调*/
    open fun startAction(restart: Boolean = true) {
        if (restart) {
            restart()
        }

        if (runActionStatus != ACTION_STATUS_ING) {
            if (restart) {
                //重启
                actionIndex = 0
            } else {
                //恢复
            }
            runActionStatus = ACTION_STATUS_ING
            onDoActionStart()
            intervalDelay = onHandleIntervalDelay(actionIndex)
            startInterval(intervalDelay)
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
        L.w("${action?.simpleHash()} [${action?.actionTitle}] 执行结束:${runActionStatus.toActionStatusStr()} ${error ?: ""} 耗时:${(nowTime() - _actionStartTime).toElapsedTime()}")
        if (runActionStatus == ACTION_STATUS_ERROR) {
            //出现异常
        } else if (runActionStatus == ACTION_STATUS_INTERRUPTED) {
            //流程中止
        } else if (runActionStatus == ACTION_STATUS_FINISH) {
            //流程结束
        }
        val _finish = onInterceptorFinish
        onInterceptorFinish = null
        _finish?.invoke(action, error)
        //清空
        if (autoUninstall) {
            //注意调用顺序
            uninstall()
        }
    }

    /**开始执行[action]前回调*/
    open fun _actionStart(
        action: BaseAccessibilityAction,
        service: BaseAccessibilityService,
        nodeList: List<AccessibilityNodeInfo>
    ) {
    }

    /**[action]执行结束后回调*/
    open fun _actionFinish(
        action: BaseAccessibilityAction,
        service: BaseAccessibilityService,
        error: ActionException?,
        nodeList: List<AccessibilityNodeInfo>
    ) {
        //action执行完成
        interceptorLog?.log("finish[${actionIndex}/${actionList.size}]->${action.simpleHash()} [${action.actionTitle}] ${if (error == null) "完成" else error.message}")

        if (error == null || error is ActionInterruptedNextException) {
            //[BaseAccessibilityAction] 被中断时, 允许继续玩下执行
            actionNext(service)
        } else {
            actionError(action, error)
        }
    }

    /**
     * 执行当前的[action]
     * [checkEvent]->[doAction]->[checkOtherEvent]->[doActionWidth]->[onCheckEventOut]
     * */
    open fun onDoAction(
        action: BaseAccessibilityAction,
        service: BaseAccessibilityService,
        nodeList: List<AccessibilityNodeInfo>
    ) {
        if (!action.isActionStart()) {
            interceptorLog?.log("start[${actionIndex}/${actionList.size}]->${action.simpleHash()} [${action.actionTitle}]")
            _actionStart(action, service, nodeList)

            if (!isActionInterceptorStart()) {
                interceptorLog?.log("已结束[${actionIndex}/${actionList.size}]->${action.simpleHash()} [${action.actionTitle}]")
                return
            }

            action._actionFinish = {
                _actionFinish(action, service, it, nodeList)
            }

            action.onActionStart(this, service, nodeList)

            if (!sleepIntervalDelay.isNullOrEmpty()) {
                //如果在[onActionStart]中触发了[ACTION_SLEEP]
                return
            }
        }

        _actionControl.addMethodName(ActionControl.METHOD_checkEvent)
        //事件处理[checkEvent]->[checkOtherEvent]->[doActionWidth]->[onCheckEventOut]
        var handle = action.checkEvent(service, lastEvent, nodeList)
        if (handle) {
            //需要事件处理
            _actionControl.addMethodName(ActionControl.METHOD_doAction)
            action.doAction(service, lastEvent, nodeList)
        } else {
            _actionControl.addMethodName(ActionControl.METHOD_checkOtherEvent)
            handle = action.checkOtherEvent(service, lastEvent, nodeList)

            if (handle) {
                //内部消化, 被other处理
                action.checkEventOutCount.clear()
            } else {
                //还是未处理的事件
                _actionControl.addMethodName(ActionControl.METHOD_doActionWidth)
                for (otherAction in actionOtherList) {
                    otherAction._actionFinish = {
                        otherAction._actionFinish = null
                        if (it != null) {
                            //只处理异常
                            actionError(otherAction, it)
                        }
                    }
                    handle = otherAction.doActionWidth(action, service, lastEvent, nodeList)
                    if (handle) {
                        //有一个action处理了, 清空checkEventOutCount计数
                        action.checkEventOutCount.clear()
                        break
                    }
                }
                if (!handle) {
                    //未被处理
                    _actionControl.addMethodName(ActionControl.METHOD_onCheckEventOut)
                    action.onCheckEventOut(service, lastEvent, nodeList)
                    onNoOtherActionHandle(action, service, lastEvent, nodeList)
                }
            }
        }

        //_actionControl.addMethodName("!next!")

        //last
        nodeList.forEach {
            try {
                it.recycle()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**下一个[Action]*/
    fun actionNext(service: BaseAccessibilityService) {
        actionIndex++

        //切换间隔时长
        intervalDelay = nextSleepIntervalDelay(onHandleIntervalDelay(actionIndex))

        AccessibilityWindow.hideCountDown()

        if (enableInterval) {
            //no op, 等待下一个周期回调
        } else {
            handler.post {
                handleAccessibility(service, false)
            }
        }
    }

    /**执行异常*/
    fun actionError(action: BaseAccessibilityAction?, error: ActionException?) {
        if (error is ActionInterruptedException) {
            runActionStatus = ACTION_STATUS_INTERRUPTED
        } else if (error != null) {
            runActionStatus = ACTION_STATUS_ERROR
        }
        onDoActionFinish(action, error)
    }

    /**未被[actionOtherList]处理*/
    open fun onNoOtherActionHandle(
        action: BaseAccessibilityAction,
        service: BaseAccessibilityService,
        event: AccessibilityEvent?,
        nodeList: List<AccessibilityNodeInfo>
    ) {

        if (onNoOtherEventHandleAction != null) {
            val mainNode = nodeList.mainNode()
            L.e("无法识别的界面:$mainNode")

            mainNode?.let {
                onNoOtherEventHandleAction?.invoke(service, mainNode)
            }
        }

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
            notifyOngoing = runActionStatus.isActionStart()
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

    /**计算出, 下一次延时的时间*/
    fun computeIntervalDelay(actionIndex: Int): Long {
        var delay = -1L
        val action = actionList.getOrNull(actionIndex)
        if (action == null) {
            delay = initialIntervalDelay
        } else {

            if (action is AutoParseAction) {
                val start = action.actionBean?.start
                if (start.isNullOrEmpty()) {
                    //no op
                } else {
                    //当前的action, 指定了自身的启动延迟
                    delay = action.getInterceptorIntervalDelay(start)
                }
            }

            if (delay < 0) {
                //无效的数据

                //使用上一个action的间隔时长
                val prevAction = actionList.getOrNull(actionIndex - 1)
                delay = prevAction?.getInterceptorIntervalDelay() ?: -1
                if (delay < 0) {
                    delay = initialIntervalDelay
                }
            }

        }
        return delay
    }

    /**下一个周期时间*/
    fun nextSleepIntervalDelay(startDelay: Long): Long {
        val intervalDelay = sleepIntervalDelay
        if (intervalDelay.isNullOrEmpty()) {
            return startDelay
        }

        val delay = parseInterceptorIntervalDelay(sleepIntervalDelay, startDelay)
        val result = when {
            intervalDelay.startsWith("+") -> startDelay + parseInterceptorIntervalDelay(
                intervalDelay.arg(1, "+")
            )
            intervalDelay.startsWith("-") -> startDelay - parseInterceptorIntervalDelay(
                intervalDelay.arg(1, "-")
            )
            else -> delay
        }

        //清空
        sleepIntervalDelay = null

        return result
    }

    //</editor-fold desc="其他">
}

fun Int.isActionCanStart() =
    this == BaseAccessibilityInterceptor.ACTION_STATUS_INIT || this == BaseAccessibilityInterceptor.ACTION_STATUS_ING

fun Int.isActionInit() = this == BaseAccessibilityInterceptor.ACTION_STATUS_INIT
fun Int.isActionStart() = this == BaseAccessibilityInterceptor.ACTION_STATUS_ING
fun Int.isActionInterrupted() = this == BaseAccessibilityInterceptor.ACTION_STATUS_INTERRUPTED
fun Int.isActionFinish() = this == BaseAccessibilityInterceptor.ACTION_STATUS_FINISH
fun Int.isActionError() = this == BaseAccessibilityInterceptor.ACTION_STATUS_ERROR
fun Int.isActionDestroy() = this == BaseAccessibilityInterceptor.ACTION_STATUS_DESTROY

/**[BaseAccessibilityInterceptor]执行结束*/
fun Int.isActionEnd() = this >= BaseAccessibilityInterceptor.ACTION_STATUS_FINISH

fun Int.toActionStatusStr() = when (this) {
    BaseAccessibilityInterceptor.ACTION_STATUS_INIT -> "ACTION_STATUS_INIT"
    BaseAccessibilityInterceptor.ACTION_STATUS_ING -> "ACTION_STATUS_ING"
    BaseAccessibilityInterceptor.ACTION_STATUS_FINISH -> "ACTION_STATUS_FINISH"
    BaseAccessibilityInterceptor.ACTION_STATUS_ERROR -> "ACTION_STATUS_ERROR"
    BaseAccessibilityInterceptor.ACTION_STATUS_DESTROY -> "ACTION_STATUS_DESTROY"
    BaseAccessibilityInterceptor.ACTION_STATUS_INTERRUPTED -> "ACTION_STATUS_INTERRUPTED"
    else -> "Unknown:$this"
}

fun BaseAccessibilityInterceptor.isActionInterceptorStart() = runActionStatus.isActionStart()

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
fun BaseAccessibilityInterceptor.uninstall(reason: String? = null) {
    RAccessibilityService.removeInterceptor(this, reason)
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
    flags: Int = 0
) {
    lastService?.openApp(packageName, flags = flags)
}