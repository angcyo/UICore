package com.angcyo.library.component

import android.os.Handler
import android.os.Looper

/**
 * 防抖动
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/10/22
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
object Debounce {
    val _mainHandler: Handler by lazy {
        Handler(Looper.getMainLooper())
    }
    val _funMap = hashMapOf<Int, FuncRunnable>()

    fun reset(key: Int): FuncRunnable {
        return _funMap[key]?.run {
            _mainHandler.removeCallbacks(this)
            this
        } ?: FuncRunnable()
    }

    fun remove(key: Int) {
        _funMap[key]?.run {
            _func = null
            _hashCode = 0
            _mainHandler.removeCallbacks(this)
        }
    }

    fun addDo(wait: Long, key: Int, runnable: FuncRunnable) {
        _funMap[key] = runnable
        _mainHandler.postDelayed(runnable, wait)
    }
}

typealias Function = () -> Unit

class FuncRunnable : Runnable {
    var _func: Function? = null
    var _hashCode: Int = 0
    override fun run() {
        _func?.invoke()
        Debounce.remove(_hashCode)
    }
}

/**
 * 防抖动函数
 * https://www.lodashjs.com/docs/latest#_debouncefunc-wait0-options
 * */
fun Any._debounce(wait: Long = 300, func: Function) {
    val hashCode = this.hashCode()
    Debounce.reset(hashCode).apply {
        this._hashCode = hashCode
        this._func = func
        Debounce.addDo(wait, hashCode, this)
    }
}