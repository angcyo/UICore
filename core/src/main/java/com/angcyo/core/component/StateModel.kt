package com.angcyo.core.component

import com.angcyo.core.lifecycle.LifecycleViewModel
import com.angcyo.core.vmApp
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 状态保持, 维护模型, 比如登录状态.
 *
 * 可以用于等待某个状态改变之后, 触发回调.
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/01/17
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */

/**回调监听*/
typealias StateAction = (data: StateData, throwable: Throwable?) -> Unit

class StateModel : LifecycleViewModel() {

    /**所有状态列表*/
    val stateList = CopyOnWriteArrayList<StateData>()

    /**所有回调列表*/
    val actionList = CopyOnWriteArrayList<PendingStateData>()

    /**更新一个状态*/
    fun updateState(type: String, value: Any?) {
        val find = stateList.find { it.type == type }
        if (find == null) {
            stateList.add(StateData(type, value))
            _notifyStateValueChanged(type)
        } else {
            if (find.value != value) {
                find.value = value
                _notifyStateValueChanged(type)
            }
        }
    }

    /**等待指定类型的状态改变*/
    fun waitState(type: String, forever: Boolean = false, action: StateAction) {
        val pendingStateData = PendingStateData(type, null, true, forever, action)
        actionList.add(pendingStateData)
        _notifyStateValueChanged(type, pendingStateData)
    }

    /**等待指定类型的状态值改变成指定的值*/
    fun waitState(type: String, value: Any?, forever: Boolean = false, action: StateAction) {
        val pendingStateData = PendingStateData(type, value, false, forever, action)
        actionList.add(pendingStateData)
        _notifyStateValueChanged(type, pendingStateData)
    }

    //通知
    fun _notifyStateValueChanged(type: String) {
        actionList.forEach {
            if (it.type == type) {
                _notifyStateValueChanged(type, it)
            }
        }
    }

    fun _notifyStateValueChanged(type: String, pendingStateData: PendingStateData) {
        val stateData = stateList.find { it.type == type } ?: return

        if (pendingStateData.waitAllValue || pendingStateData.value == stateData.value) {
            //需要监听的值改变了, 或者是目标值, 则通知回调
            pendingStateData.action.invoke(stateData, null)
            if (!pendingStateData.forever) {
                actionList.remove(pendingStateData)
            }
        }
    }

    override fun release(data: Any?) {
        super.release(data)
        actionList.forEach {
            it.action.invoke(StateData(it.type, it.value), CancelStateException("release"))
        }
        actionList.clear()
        stateList.clear()
    }
}

/**状态被取消的异常*/
class CancelStateException(message: String) : RuntimeException(message)

/**想要监听的状态*/
data class PendingStateData(
    val type: String,
    val value: Any?,
    /**只要有值改变, 就需要通知*/
    val waitAllValue: Boolean,
    /**一直监听*/
    val forever: Boolean,
    val action: StateAction
)

/**被监听的状态*/
data class StateData(
    /**状态类型*/
    val type: String,
    /**状态的值*/
    var value: Any?
)

/**等待一个[state]状态改变*/
fun waitState(state: String, value: Any? = null, forever: Boolean = false, action: StateAction) {
    vmApp<StateModel>().waitState(state, value, forever, action)
}

/**一直等待一个[state]状态改变, 注意内存泄漏*/
fun waitStateForever(state: String, value: Any? = null, action: StateAction) {
    waitState(state, value, true, action)
}

/**更新一个状态*/
fun updateState(state: String, value: Any?) {
    vmApp<StateModel>().updateState(state, value)
}
