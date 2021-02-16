package com.angcyo.acc2.control

import com.angcyo.acc2.bean.ActionBean
import com.angcyo.acc2.bean.CheckBean
import com.angcyo.acc2.bean.TaskBean
import com.angcyo.acc2.control.AccControl.Companion.CONTROL_STATE_ERROR
import com.angcyo.acc2.control.AccControl.Companion.CONTROL_STATE_FINISH
import com.angcyo.acc2.control.AccControl.Companion.CONTROL_STATE_NORMAL
import com.angcyo.acc2.control.AccControl.Companion.CONTROL_STATE_PAUSE
import com.angcyo.acc2.control.AccControl.Companion.CONTROL_STATE_RUNNING
import com.angcyo.acc2.control.AccControl.Companion.CONTROL_STATE_STOP
import com.angcyo.acc2.core.BaseAccService
import com.angcyo.library.L
import com.angcyo.library.ex.des
import com.angcyo.library.ex.simpleHash
import com.angcyo.library.ex.sleep

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/01/30
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class AccControl : Runnable {

    companion object {

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

    var finishReason: String? = null

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
        if (accService() == null) {
            error("无障碍服务未连接")
            return false
        }
        _taskBean = taskBean
        accSchedule.startSchedule()
        _startThread()
        updateControlState(CONTROL_STATE_RUNNING)

        _taskBean?.let {
            controlListenerList.forEach {
                it.onControlStart(taskBean)
            }
        }
        return true
    }

    /**停止任务*/
    fun stop(reason: String = "主动停止") {
        _end(reason, CONTROL_STATE_STOP)
    }

    /**异常任务*/
    fun error(reason: String = "任务异常") {
        _end(reason, CONTROL_STATE_ERROR)
    }

    /**完成任务*/
    fun finish(reason: String = "任务完成") {
        _end(reason, CONTROL_STATE_FINISH)
    }

    fun _end(reason: String, state: Int) {
        L.i("$reason[${state.toControlStateStr()}]")
        finishReason = reason
        accSchedule.endSchedule()
        _stopThread()
        updateControlState(state)

        _taskBean?.let { taskBean ->
            controlListenerList.forEach {
                it.onControlEnd(taskBean, state, reason)
            }
        }
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

        controlListenerList.forEach {
            it.onControlStateChanged(this, old, newState)
        }

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

    //</editor-fold desc="操作">

    //<editor-fold desc="线程">

    var _controlThread: Thread? = null

    fun _startThread() {
        _stopThread()
        _controlThread = Thread(this, this.simpleHash()).apply {
            start()
        }
    }

    fun _stopThread() {
        _controlThread?.interrupt()
        _controlThread = null
    }

    /**子线程内调度*/
    override fun run() {
        log("run控制器启动[${_taskBean?.title}]")
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
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        log("run控制器结束[${_controlState.toControlStateStr()}]:$finishReason ${accSchedule.durationStr()}\n${_taskBean?.textMap}\n${_taskBean?.textListMap}")
    }

    //</editor-fold desc="线程">
}

//<editor-fold desc="扩展">

/**控制器已经开始运行了*/
val AccControl.isControlStart: Boolean
    get() = _controlState == CONTROL_STATE_RUNNING || _controlState == CONTROL_STATE_PAUSE

val AccControl.isControlEnd: Boolean
    get() = _controlState >= CONTROL_STATE_FINISH

/**控制器暂停中*/
val AccControl.isControlPause: Boolean
    get() = _controlState == CONTROL_STATE_PAUSE

val AccControl.isControlRunning: Boolean
    get() = _controlState == CONTROL_STATE_RUNNING

val AccControl.controlStateStr: String
    get() = _controlState.toControlStateStr()

fun ActionBean.actionLog() = "Action[${title}${des.des()}](${actionId})"

fun CheckBean.checkLog() = "Check[${title}${des.des()}](${checkId})"

fun Number.toControlStateStr() = when (this) {
    CONTROL_STATE_NORMAL -> "STATE_NORMAL"
    CONTROL_STATE_RUNNING -> "STATE_RUNNING"
    CONTROL_STATE_FINISH -> "STATE_FINISH"
    CONTROL_STATE_ERROR -> "STATE_ERROR"
    CONTROL_STATE_PAUSE -> "STATE_PAUSE"
    CONTROL_STATE_STOP -> "STATE_STOP"
    else -> "STATE_UNKNOWN"
}

/**日志输出*/
fun AccControl.log(log: String?) {
    accPrint.log(log)
}

/**日志输出*/
fun AccControl.next(actionBean: ActionBean, time: Long) {
    accPrint.next(actionBean, time)
}

//</editor-fold desc="扩展">

