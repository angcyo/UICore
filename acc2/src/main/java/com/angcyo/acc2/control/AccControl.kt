package com.angcyo.acc2.control

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.bean.*
import com.angcyo.acc2.control.AccControl.Companion.CONTROL_STATE_ERROR
import com.angcyo.acc2.control.AccControl.Companion.CONTROL_STATE_FINISH
import com.angcyo.acc2.control.AccControl.Companion.CONTROL_STATE_NORMAL
import com.angcyo.acc2.control.AccControl.Companion.CONTROL_STATE_PAUSE
import com.angcyo.acc2.control.AccControl.Companion.CONTROL_STATE_RUNNING
import com.angcyo.acc2.control.AccControl.Companion.CONTROL_STATE_STOP
import com.angcyo.acc2.core.BaseAccService
import com.angcyo.acc2.core.ControlException
import com.angcyo.acc2.core.ControlInterruptException
import com.angcyo.acc2.dynamic.IHandleDynamic
import com.angcyo.acc2.dynamic.IInputProvider
import com.angcyo.acc2.dynamic.ITaskDynamic
import com.angcyo.library.*
import com.angcyo.library.ex.*
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ThreadFactory

/**
 * 无障碍控制器, 包含[AccSchedule]核心调度器对象
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/01/30
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class AccControl : Runnable {

    companion object {

        const val ACC_MAIN_THREAD = "AccMainThread"

        //开始
        const val CONTROL_STATE_NORMAL = 0
        const val CONTROL_STATE_RUNNING = 1
        const val CONTROL_STATE_PAUSE = 3

        //结束
        const val CONTROL_STATE_FINISH = 10
        const val CONTROL_STATE_ERROR = 11
        const val CONTROL_STATE_STOP = 12

        fun <T> newInstance(clsName: String, cls: Class<T>): T? {
            try {
                val c = Class.forName(clsName)
                if (cls.isAssignableFrom(c)) {
                    return c.newInstance() as T
                }
            } catch (e: Exception) {
                L.w("无法实例化:$clsName")
            }
            return null
        }

        /**
         * [ITaskDynamic]
         * [IInputProvider]
         * */
        fun initTaskDynamic(control: AccControl?, taskBean: TaskBean) {
            //ITaskDynamic
            val listenerObjList = mutableListOf<ITaskDynamic>()
            taskBean.listenerClsList?.forEach {
                newInstance(it, ITaskDynamic::class.java)?.let { obj ->
                    listenerObjList.add(obj)
                }
            }

            if (listenerObjList.isNotEmpty()) {
                taskBean._listenerObjList = listenerObjList
                listenerObjList.forEach { obj ->
                    control?.controlListenerList?.forEach {
                        it.onCreateDynamicObj(obj)
                    }
                    listenerObjList.forEach {
                        it.onCreateDynamicObj(obj)
                    }
                }
            }

            //IInputProvider
            val inputProviderList = mutableListOf<IInputProvider>()
            taskBean.inputProviderClsList?.forEach {
                newInstance(it, IInputProvider::class.java)?.let { obj ->
                    inputProviderList.add(obj)
                }
            }

            if (inputProviderList.isNotEmpty()) {
                taskBean._inputProviderObjList = inputProviderList

                inputProviderList.forEach { obj ->
                    control?.controlListenerList?.forEach {
                        it.onCreateDynamicObj(obj)
                    }
                    listenerObjList.forEach {
                        it.onCreateDynamicObj(obj)
                    }
                }
            }
        }

        /**一次性创建所有[com.angcyo.acc2.dynamic.IHandleDynamic]*/
        fun initAllHandleCls(control: AccControl?, taskBean: TaskBean) {
            taskBean.apply {
                actionList?.forEach { actionBean ->
                    actionBean.check?.handle?.forEach { handleBean ->
                        initHandleDynamic(control, handleBean)
                    }
                }
            }
        }

        /**[IHandleDynamic]*/
        fun initHandleDynamic(control: AccControl?, handleBean: HandleBean) {
            val clsList = handleBean.handleClsList
            if (clsList.isNullOrEmpty()) {
                handleBean._handleObjList = null
            } else {
                if (handleBean._handleObjList == null || handleBean._handleObjList.size() != clsList.size()) {
                    val ojbList = mutableListOf<IHandleDynamic>()
                    clsList.forEach {
                        newInstance(it, IHandleDynamic::class.java)?.let { obj ->
                            ojbList.add(obj)
                        }
                    }

                    if (ojbList.isNotEmpty()) {
                        handleBean._handleObjList = ojbList

                        ojbList.forEach { obj ->
                            control?.controlListenerList?.forEach {
                                it.onCreateDynamicObj(obj)
                            }
                            control?._taskBean?._listenerObjList?.forEach {
                                it.onCreateDynamicObj(obj)
                            }
                        }
                    }
                }
            }
        }
    }

    /**控制器的状态*/
    @Volatile
    var _controlState: Int = CONTROL_STATE_NORMAL

    //<editor-fold desc="组件">

    /**监听器*/
    val controlListenerList = mutableListOf<ControlListener>()

    /**调度器*/
    val accSchedule = AccSchedule(this)

    /**日志输出*/
    var accPrint = AccPrint()

    fun addControlListener(listener: ControlListener) {
        if (!controlListenerList.contains(listener)) {
            controlListenerList.add(listener)
        }
    }

    fun removeControlListener(listener: ControlListener) {
        if (controlListenerList.contains(listener)) {
            controlListenerList.remove(listener)
        }
    }

    //</editor-fold desc="组件">

    //<editor-fold desc="启动">

    var _taskBean: TaskBean? = null

    /**完成的原因*/
    var finishReason: String? = null

    /**异常的信息*/
    var lastError: Throwable? = null

    /**启动一个任务*/
    fun start(taskBean: TaskBean, force: Boolean): Boolean {
        if (isControlStart) {
            if (force) {
                stop("被迫停止")
            } else {
                L.w("[${_taskBean?.title}]已在运行.")
                return false
            }
        }
        _taskBean = taskBean
        if (accService() == null) {
            error("无障碍服务未连接")
            return false
        }
        finishReason = null
        lastError = null

        accSchedule.startSchedule()
        _startThread()
        updateControlState(CONTROL_STATE_RUNNING)

        _taskBean?.let {
            controlListenerList.forEach {
                it.onControlStart(this, taskBean)
            }
            taskBean._listenerObjList?.forEach {
                it.onControlStart(this, taskBean)
            }
        }
        return true
    }

    /**停止任务*/
    fun stop(reason: String? = "主动停止") {
        _end(reason, CONTROL_STATE_STOP)
    }

    @Deprecated("废弃")
    fun error(reason: String?) {
        error(ControlException(reason ?: "任务异常"))
    }

    /**异常任务*/
    fun error(error: Throwable) {
        _end(error.message, CONTROL_STATE_ERROR, error)
    }

    /**完成任务*/
    fun finish(reason: String? = "任务完成") {
        _end(reason, CONTROL_STATE_FINISH)
    }

    fun _end(reason: String?, state: Int, error: Throwable? = null) {
        if (isControlEnd || _controlState == state) {
            L.i("控制器已经[${state.toControlStateStr()}]")
            return
        }
        L.i("$reason[${state.toControlStateStr()}]")
        finishReason = reason
        lastError = error
        accSchedule.endSchedule()
        _stopThread()
        updateControlState(state)

        _taskBean?.let { taskBean ->
            controlListenerList.forEach {
                it.onControlEnd(this, taskBean, state, reason)
            }
            taskBean._listenerObjList?.forEach {
                it.onControlEnd(this, taskBean, state, reason)
            }
        }
        //_taskBean = null
    }

    //</editor-fold desc="启动">

    //<editor-fold desc="操作">

    /**无障碍服务*/
    fun accService() = BaseAccService.lastService

    /**更新控制器状态
     * @return 更新是否成功*/
    fun updateControlState(newState: Int): Boolean {
        if (_controlState == newState) {
            return false
        }

        val old = _controlState
        _controlState = newState

        //form
        if (newState >= CONTROL_STATE_FINISH) {
            accSchedule.accParse.formParse.parseTaskForm(this, newState)
        }

        controlListenerList.forEach {
            it.onControlStateChanged(this, old, newState)
        }

        _taskBean?._listenerObjList?.forEach {
            it.onControlStateChanged(this, old, newState)
        }

        //状态改变
        log(buildString {
            append("控制器状态改变:${old.toControlStateStr()} -> $controlStateStr :${accSchedule.durationStr()}")
            if (newState == CONTROL_STATE_PAUSE) {
                appendLine()
                //app info
                append(controlEndToLog())
            }
        })

        return true
    }

    /**暂停*/
    fun pause() {
        if (isControlStart) {
            updateControlState(CONTROL_STATE_PAUSE)
        }
    }

    /**恢复运行*/
    fun resume(restart: Boolean) {
        if (_controlState == CONTROL_STATE_PAUSE) {
            updateControlState(CONTROL_STATE_RUNNING)
        } else {
            if (isControlStart) {
                //正在运行中...
            } else {
                if (restart) {
                    _taskBean?.let {
                        start(it, false)
                    }
                }
            }
        }
    }

    fun findAction(actionId: Long?): ActionBean? =
        if (actionId == null) null else _taskBean?.actionList?.find { it.actionId == actionId }

    /**是否是控制器的主线程*/
    fun isControlMainThread() = threadName().startsWith(ACC_MAIN_THREAD)

    /**事件通知[com.angcyo.acc2.action.EventAction] */
    fun onActionEvent(
        control: AccControl,
        controlContext: ControlContext,
        nodeList: List<AccessibilityNodeInfoCompat>?,
        action: String,
        event: String
    ) {
        controlListenerList.forEach {
            try {
                it.onActionEvent(control, controlContext, nodeList, action, event)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        _taskBean?._listenerObjList?.forEach {
            try {
                it.onActionEvent(control, controlContext, nodeList, action, event)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    //</editor-fold desc="操作">

    //<editor-fold desc="线程">

    val pool = Executors.newFixedThreadPool(2, ThreadFactory {
        Thread(it, "${ACC_MAIN_THREAD}_${this.simpleHash()}")
    })

    /**主流程控制的线程*/
    var _controlThread: Future<*>? = null

    /**循环调度的线程*/
    var _intervalThread: Future<*>? = null

    fun _startThread() {
        _stopThread()
        _controlThread = pool.submit(this)
        _intervalThread = pool.submit {
            while (isControlStart) {
                try {
                    if (_controlState == CONTROL_STATE_RUNNING) {
                        //run
                        accSchedule.intervalSchedule()
                    } else {
                        //wait
                        sleep()
                    }
                } catch (e: ControlInterruptException) {
                    e.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun _stopThread() {
        _controlThread?.cancel(true)
        _controlThread = null
        _intervalThread?.cancel(true)
        _intervalThread = null
    }

    /**子线程内调度*/
    override fun run() {
        log(controlStartToLog())
        //next(_taskBean?.title, _taskBean?.des, 0)

        controlListenerList.forEach {
            it.onControlThreadStart(this)
        }

        _taskBean?._listenerObjList?.forEach {
            it.onControlThreadStart(this)
        }

        while (isControlStart) {
            try {
                if (_controlState == CONTROL_STATE_RUNNING) {
                    //run

                    controlListenerList.forEach {
                        it.onControlThreadSchedule(this)
                    }

                    _taskBean?._listenerObjList?.forEach {
                        it.onControlThreadSchedule(this)
                    }

                    accSchedule.scheduleNext()
                } else {
                    //wait
                    sleep()
                }
            } catch (e: ControlInterruptException) {
                e.printStackTrace()
                error(e.message)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        //end...

        controlListenerList.forEach {
            it.onControlThreadEnd(this)
        }

        _taskBean?._listenerObjList?.forEach {
            it.onControlThreadEnd(this)
        }

        log(controlEndToLog())
    }

    /**控制器的日志*/
    fun controlLog(): String = buildString {
        append(controlStartToLog())
        append(controlEndToLog())
    }

    fun controlStartToLog(): String = buildString {
        if (_controlState == CONTROL_STATE_NORMAL) {
            append("控制器就绪")
        } else if (!isControlEnd && !isControlPause) {
            append("run控制器启动")
        }
        append(_taskBean?.title.des2())
        append(_taskBean?.actionList?.size()?.toString().des2())
        appendLine()
        appendLine("enable:${_taskBean?.enableAction}")
        appendLine("disable:${_taskBean?.disableAction}")
        appendLine("random:${_taskBean?.randomEnableAction}")
    }

    fun controlEndToLog(): String = buildString {

        if (isControlEnd) {
            append(controlStartToLog())
            append("run控制器结束")
            append("[${_controlState.toControlStateStr()}]:$finishReason ")
            appendLine(accSchedule.durationStr())
        }

        accSchedule.packageTrackList.let { list ->
            if (list.isNotEmpty()) {
                append("track:")
                appendLine(list)
            }
        }

        accSchedule.inputTextList.let { list ->
            if (list.isNotEmpty()) {
                append("input:")
                appendLine(list)
            }
        }

        _taskBean?.textMap?.let { map ->
            if (map.isNotEmpty()) {
                append("text:")
                appendLine(map)
            }
        }

        _taskBean?.textListMap?.let { map ->
            if (map.isNotEmpty()) {
                append("textList:")
                appendLine(map)
            }
        }

        /*_taskBean?.map?.let { map ->
            if (map.isNotEmpty()) {
                append("map:")
                appendLine(map)
            }
        }*/

        //app info
        append("${app().getAppName()} ${app().packageName} ${getAppVersionName()} ${getAppVersionCode()}")
    }

    //</editor-fold desc="线程">
}

//<editor-fold desc="扩展">

/**控制器已经开始运行了*/
val AccControl.isControlStart: Boolean
    get() = _controlState.isControlStart

val Int.isControlStart: Boolean
    get() = this == CONTROL_STATE_RUNNING || this == CONTROL_STATE_PAUSE

val AccControl.isControlEnd: Boolean
    get() = _controlState.isControlEnd

val Int.isControlEnd: Boolean
    get() = this >= CONTROL_STATE_FINISH

/**控制器暂停中*/
val AccControl.isControlPause: Boolean
    get() = _controlState.isControlPause

val Int.isControlPause: Boolean
    get() = this == CONTROL_STATE_PAUSE

val AccControl.isControlRunning: Boolean
    get() = _controlState.isControlRunning

val Int.isControlRunning: Boolean
    get() = this == CONTROL_STATE_RUNNING

val AccControl.controlStateStr: String
    get() = _controlState.toControlStateStr()

fun ActionBean.actionLog() = "Action[${title ?: ""}${(des ?: summary).des()}](${actionId})"

fun CheckBean.checkLog() = "Check[${title ?: ""}${des.des()}](${checkId})"

fun FindBean.findLog() = "Find[${textList ?: stateList ?: clsList ?: rectList}]"

fun Number.toControlStateStr() = when (this) {
    CONTROL_STATE_NORMAL -> "STATE_NORMAL"
    CONTROL_STATE_RUNNING -> "STATE_RUNNING"
    CONTROL_STATE_FINISH -> "STATE_FINISH"
    CONTROL_STATE_ERROR -> "STATE_ERROR"
    CONTROL_STATE_PAUSE -> "STATE_PAUSE"
    CONTROL_STATE_STOP -> "STATE_STOP"
    else -> "STATE_UNKNOWN"
}

/**日志输出
 * [isPrimaryAction] 是否是主线任务, 如果不是, 日志会缩进*/
fun AccControl.log(log: String?, isPrimaryAction: Boolean = true) {
    log(log, if (isPrimaryAction) 0 else 2)
}

/**[indent]缩进的数量*/
fun AccControl.log(log: String?, indent: Int) {
    if (indent <= 0) {
        accPrint.log(log)
    } else {
        accPrint.log("${newLineIndent(indent)}$log")
    }
}

/**日志输出*/
fun AccControl.next(actionBean: ActionBean, time: Long) {
    accPrint.next(actionBean, time)
}

//</editor-fold desc="扩展">

//<editor-fold desc="Dynamic扩展">

/**查找[com.angcyo.acc2.dynamic.IHandleDynamic]对象*/
fun <T> AccControl.findHandleObj(cls: Class<T>): T? {
    val actionList = _taskBean?.actionList ?: return null
    for (action in actionList) {
        for (handleBean in action.check?.handle ?: emptyList()) {
            for (obj in handleBean._handleObjList ?: emptyList()) {
                if (cls.isAssignableFrom(obj.javaClass)) {
                    return obj as T
                }
            }
        }
    }
    return null
}

//</editor-fold desc="Dynamic扩展">


