package com.angcyo.library.component

import android.os.Handler
import android.os.Looper
import com.angcyo.library.R
import com.angcyo.library.app
import com.angcyo.library.ex.nowTime

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
        val result = _funMap[key]?.run {
            _mainHandler.removeCallbacks(this)
            this
        } ?: FuncRunnable()
        _funMap[key] = result
        return result
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
    var _time = nowTime()
    override fun run() {
        _func?.invoke()
        Debounce.remove(_hashCode)
    }
}

/**
 * 限流, 一定时间内, 必触发一次
 * https://juejin.cn/post/7000711447592304653
 * */
fun Any._throttle(
    wait: Long = app().resources.getInteger(R.integer.lib_animation_delay).toLong(),
    func: Function
) {
    val hashCode = this.hashCode()
    val nowTime = nowTime()
    Debounce.reset(hashCode).apply {
        this._hashCode = hashCode
        this._func = func
        if (nowTime - _time >= wait) {
            _time = nowTime
            run()
        }
    }
}

/**
 * 防抖动函数, 防抖, 只在最后一次触发.
 * https://www.lodashjs.com/docs/latest#_debouncefunc-wait0-options
 *
 * https://juejin.cn/post/7000711447592304653
 * */
fun Any._debounce(
    wait: Long = app().resources.getInteger(R.integer.lib_animation_delay).toLong(),
    func: Function
) {
    val hashCode = this.hashCode()
    Debounce.reset(hashCode).apply {
        this._hashCode = hashCode
        this._func = func
        Debounce.addDo(wait, hashCode, this)
    }
}