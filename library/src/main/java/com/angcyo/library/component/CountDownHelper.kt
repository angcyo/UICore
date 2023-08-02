package com.angcyo.library.component

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.angcyo.library.ex.nowTime

/**
 * 倒计时
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/12/08
 * Copyright (c) 2020 angcyo. All rights reserved.
 */

/**回调, 还剩 多少毫秒[millisecond]*/
typealias OnCountDownTick = (millisecond: Long) -> Unit

class CountDownHelper : Runnable {

    /**自动绑定声明周期*/
    var lifecycleOwner: LifecycleOwner? = null

    private val lifecycleObserver = LifecycleEventObserver { source, event ->
        if (event == Lifecycle.Event.ON_DESTROY) {
            //销毁之后, 自动移除
            stopCountDown()
        }
    }

    //---

    val handler = Handler(Looper.getMainLooper())

    /**回调*/
    var onTick: OnCountDownTick? = null

    //是否已开始
    var _isStart: Boolean = false

    //开始的时间戳, 毫秒
    var _startTime: Long = 0

    //结束的时间戳, 毫秒
    var _endTime: Long = 0

    //当前时间戳
    var _currentTime: Long = 0

    //步长, 也就是回调频率
    var _step: Long = 1000

    /**开始倒计时
     * [second] 多少秒倒计时*/
    fun startCountDown(second: Long, step: Long = 1000, tick: OnCountDownTick) {
        if (_isStart) {
            return
        }
        lifecycleOwner?.lifecycle?.addObserver(lifecycleObserver)

        _isStart = true
        _startTime = System.currentTimeMillis()
        _currentTime = _startTime
        _endTime = _startTime + second * 1000
        _step = step
        onTick = tick

        _notify()
    }

    /**停止倒计时*/
    fun stopCountDown(notify: Boolean = false) {
        lifecycleOwner?.lifecycle?.removeObserver(lifecycleObserver)
        if (_isStart) {
            handler.removeCallbacks(this)
            if (notify) {
                run()
            }
        }
        _isStart = false
    }

    override fun run() {
        _currentTime = nowTime()
        _notify()
    }

    fun _notify() {
        val surplusTime = _endTime - _currentTime

        if (surplusTime <= 0) {
            //结束
            onTick?.invoke(0)
        } else {
            onTick?.invoke(surplusTime)

            handler.postDelayed(this, _step)
        }
    }
}

/**开始一个倒计时
 * [second] 多少秒倒计时, 秒
 * [step] 步长毫秒*/
fun startCountDown(second: Long, step: Long = 1000, tick: OnCountDownTick): CountDownHelper {
    val countDownHelper = CountDownHelper()
    countDownHelper.startCountDown(second, step, tick)
    return countDownHelper
}

/**[startCountDown]*/
fun LifecycleOwner.startCountDown(
    second: Long,
    step: Long = 1000,
    tick: OnCountDownTick
): CountDownHelper {
    val countDownHelper = CountDownHelper()
    countDownHelper.lifecycleOwner = this
    countDownHelper.startCountDown(second, step, tick)
    return countDownHelper
}