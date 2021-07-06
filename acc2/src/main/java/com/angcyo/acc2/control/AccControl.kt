package com.angcyo.acc2.control

import com.angcyo.acc2.bean.ActionBean
import com.angcyo.acc2.bean.CheckBean
import com.angcyo.acc2.bean.FindBean
import com.angcyo.acc2.bean.TaskBean
import com.angcyo.acc2.control.AccControl.Companion.CONTROL_STATE_ERROR
import com.angcyo.acc2.control.AccControl.Companion.CONTROL_STATE_FINISH
import com.angcyo.acc2.control.AccControl.Companion.CONTROL_STATE_NORMAL
import com.angcyo.acc2.control.AccControl.Companion.CONTROL_STATE_PAUSE
import com.angcyo.acc2.control.AccControl.Companion.CONTROL_STATE_RUNNING
import com.angcyo.acc2.control.AccControl.Companion.CONTROL_STATE_STOP
import com.angcyo.acc2.core.BaseAccService
import com.angcyo.acc2.core.ControlException
import com.angcyo.acc2.core.ControlInterruptException
import com.angcyo.library.*
import com.angcyo.library.ex.*

/**
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
        }
        return true
    }

    /**停止任务*/
    fun stop(reason: String? = "主动停止") {
        _end(reason, CONTROL_STATE_STOP)
    }

    /**异常任务*/
    @Deprecated("废弃")
    fun error(reason: String?) {
        error(ControlException(reason ?: "任务异常"))
    }

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

    //</editor-fold desc="操作">

    //<editor-fold desc="线程">

    var _controlThread: Thread? = null

    fun _startThread() {
        _stopThread()
        _controlThread = Thread(this, "${ACC_MAIN_THREAD}_${this.simpleHash()}").apply {
            start()
        }
    }

    fun _stopThread() {
        _controlThread?.interrupt()
        _controlThread = null
    }

    /**子线程内调度*/
    override fun run() {
        log(controlStartToLog())
        //next(_taskBean?.title, _taskBean?.des, 0)
        while (isControlStart) {
            try {
                if (_controlState == CONTROL_STATE_RUNNING) {
                    //run
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

        accSchedule.packageTrackList.apply {
            if (isNotEmpty()) {
                append("track:")
                appendLine(this)
            }
        }

        accSchedule.inputTextList.apply {
            if (isNotEmpty()) {
                append("input:")
                appendLine(this)
            }
        }

        _taskBean?.textMap?.apply {
            if (isNotEmpty()) {
                append("text:")
                appendLine(this)
            }
        }

        _taskBean?.textListMap?.apply {
            if (isNotEmpty()) {
                append("textList:")
                appendLine(this)
            }
        }

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

