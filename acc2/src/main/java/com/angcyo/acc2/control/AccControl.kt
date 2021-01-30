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
        const val CONTROL_STATE_NORMAL = 0
        const val CONTROL_STATE_RUNNING = 1
        const val CONTROL_STATE_FINISH = 2
        const val CONTROL_STATE_ERROR = 3
        const val CONTROL_STATE_PAUSE = 9
        const val CONTROL_STATE_STOP = 10
    }

    /**控制器的状态*/
    @Volatile
    var _controlState: Int = CONTROL_STATE_NORMAL

    val isControlStart: Boolean
        get() = _controlState == CONTROL_STATE_RUNNING || _controlState == CONTROL_STATE_PAUSE

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
                return false
            }
        }
        if (BaseAccService.lastService == null) {
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
        accPrint.log("控制器启动[${_taskBean?.title}]")
        accPrint.next(_taskBean?.title, _taskBean?.des, 0)
        while (isControlStart) {
            if (_controlState == CONTROL_STATE_RUNNING) {
                //run
                accSchedule.scheduleNext()
            } else {
                //wait
                sleep()
            }
        }
        accPrint.log("控制器结束[${_controlState.toControlStateStr()}]:$finishReason")
    }

    //</editor-fold desc="线程">
}

//<editor-fold desc="扩展">

fun ActionBean.actionLog() = "Action[${title}](${actionId})"

fun CheckBean.checkLog() = "Check[${title}](${checkId})"

fun Number.toControlStateStr() = when (this) {
    CONTROL_STATE_NORMAL -> "STATE_NORMAL"
    CONTROL_STATE_RUNNING -> "STATE_RUNNING"
    CONTROL_STATE_FINISH -> "STATE_FINISH"
    CONTROL_STATE_ERROR -> "STATE_ERROR"
    CONTROL_STATE_PAUSE -> "STATE_PAUSE"
    CONTROL_STATE_STOP -> "STATE_STOP"
    else -> "STATE_UNKNOWN"
}

//</editor-fold desc="扩展">

