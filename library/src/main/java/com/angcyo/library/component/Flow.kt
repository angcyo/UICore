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

    /**追加flow*/
    fun flow(action: FlowAction): Flow {
        actionList.add(action)
        return this
    }

    /**开始执行*/
    fun start(): Flow {
        _startIndex = 0
        _start()
        return this
    }

    private var _startIndex = 0

    private fun _start() {
        val action = actionList.getOrNull(_startIndex)
        action?.apply {
            _startAction(action)
        }
    }

    private fun _next() {
        _startIndex++
        _start()
    }

    private fun _startAction(action: FlowAction) {
        action.invoke {
            //执行结束后的回调
            if (it != null) {
                //异常, 中断处理
                L.w("Flow 被中断:[${_startIndex}/${actionList.size}]")
            } else {
                _next()
            }
        }
    }
}

typealias FlowChain = (error: Throwable?) -> Unit
typealias FlowAction = (flowChain: FlowChain) -> Unit

/**Dsl
 * 需要调用 [com.angcyo.library.component.Flow.start]开始flow*/
fun flow(action: FlowAction): Flow {
    val flow = Flow()
    return flow.flow(action)
}