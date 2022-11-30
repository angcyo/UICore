package com.angcyo.library.component

import androidx.annotation.AnyThread
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
@AnyThread
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

    /**停止*/
    fun stop(error: Throwable? = null) {
        if (!_isEnd) {
            _isEnd = true
            lastError = error
            if (error != null) {
                onErrorAction(error)
            }
            onEndAction(lastError)
        }
    }

    /**开始执行*/
    @AnyThread
    fun start(onEnd: (Throwable?) -> Unit = {}): Flow {
        _isEnd = false
        _startIndex = 0
        onEndAction = onEnd
        _start()
        return this
    }

    private var _startIndex = 0

    var _isEnd: Boolean = false

    private fun _start() {
        val action = actionList.getOrNull(_startIndex)
        if (action == null) {
            _isEnd = true
            onEndAction(null)
        } else {
            _startAction(action)
        }
    }

    @AnyThread
    private fun _next() {
        if (_isEnd) {
            return
        }
        _startIndex++
        _start()
    }

    @AnyThread
    private fun _startAction(action: FlowAction) {
        action.invoke(this) {
            //执行结束后的回调
            if (it != null) {
                lastError = it
                //异常, 中断处理
                onErrorAction(it)
                if (errorInterruptFlow) {
                    L.w("Flow 被中断:[${_startIndex}/${actionList.size}] ${lastError?.message} ↓")
                    lastError?.printStackTrace()
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
 * ```
 * flow { chain ->
 *   chain(null)
 * }.flow { chain ->
 *   chain(null)
 * }.flow { chain ->
 *   chain(null)
 * }.start {
 *  //some thing
 * }.stop(null)
 * ```
 * 需要调用 [com.angcyo.library.component.Flow.start]开始flow*/
@AnyThread
fun flow(action: FlowAction): Flow {
    val flow = Flow()
    return flow.flow(action)
}