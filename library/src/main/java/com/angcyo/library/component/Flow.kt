package com.angcyo.library.component

import com.angcyo.library.L

/**
 * 流程同步调用
 *
 * flow1
 *     flow2
 *          flow3
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/11/09
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class Flow {

    val actionList = mutableListOf<FlowAction>()

    /**异常的回调处理*/
    var onErrorAction: (Throwable) -> Unit = {}

    /**结束的回调处理*/
    var onEndAction: (Throwable?) -> Unit = {}

    /**异常后, 是否中断flow*/
    var errorInterruptFlow: Boolean = true

    /**最后一次的错误信息*/
    var lastError: Throwable? = null

    /**追加flow*/
    fun flow(action: FlowAction): Flow {
        actionList.add(action)
        return this
    }

    /**开始执行*/
    fun start(onEnd: (Throwable?) -> Unit = {}): Flow {
        _startIndex = 0
        onEndAction = onEnd
        _start()
        return this
    }

    private var _startIndex = 0

    private fun _start() {
        val action = actionList.getOrNull(_startIndex)
        if (action == null) {
            onEndAction(null)
        } else {
            _startAction(action)
        }
    }

    private fun _next() {
        _startIndex++
        _start()
    }

    private fun _startAction(action: FlowAction) {
        action.invoke(this) {
            //执行结束后的回调
            if (it != null) {
                lastError = it
                //异常, 中断处理
                onErrorAction(it)
                if (errorInterruptFlow) {
                    L.w("Flow 被中断:[${_startIndex}/${actionList.size}]")
                    onEndAction(it)
                } else {
                    _next()
                }
            } else {
                _next()
            }
        }
    }
}

/**调用此方法, 触发下一个*/
typealias FlowChain = (error: Throwable?) -> Unit

/**配置调用请求*/
typealias FlowAction = Flow.(flowChain: FlowChain) -> Unit

/**Dsl
 * 需要调用 [com.angcyo.library.component.Flow.start]开始flow*/
fun flow(action: FlowAction): Flow {
    val flow = Flow()
    return flow.flow(action)
}