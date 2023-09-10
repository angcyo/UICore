package com.angcyo.library.component

import android.os.Handler
import android.os.Looper
import com.angcyo.library.R
import com.angcyo.library.app
import com.angcyo.library.component.MainExecutor._lastRunnable
import com.angcyo.library.ex.Action
import java.lang.ref.WeakReference
import java.util.concurrent.Executor
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 主线程调度器
 *
 * [com.angcyo.library.component.ThreadExecutor]
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/14
 */

object MainExecutor : Executor {
    val handler = Handler(Looper.getMainLooper())

    var _lastRunnable: WeakReference<MainRunnable?>? = null

    /**直接执行[command]*/
    override fun execute(command: Runnable) {
        if (!handler.post(command)) {
            throw RejectedExecutionException("$handler is shutting down")
        }
    }

    /**延迟执行[command]*/
    fun delay(command: Runnable, delayMillis: Long) {
        if (!handler.postDelayed(command, delayMillis)) {
            throw RejectedExecutionException("$handler is shutting down")
        }
    }

    fun delay(delayMillis: Long, command: Runnable) {
        delay(command, delayMillis)
    }

    /**移除执行[command]*/
    fun remove(command: Runnable) {
        handler.removeCallbacks(command)
    }
}

class MainRunnable(var action: Action? = null) : Runnable {
    var cancel: AtomicBoolean = AtomicBoolean(false)
    override fun run() {
        if (cancel.get()) {
            //被取消
        } else {
            action?.invoke()
        }
        cancel.set(true)
        action = null
    }
}

/**在主线程中执行一次[Runnable], 抖动处理
 * [runnable] 需要确保是唯一的对象*/
fun onMainOnce(
    delayMillis: Long = app().resources.getInteger(R.integer.lib_animation_delay).toLong(),
    runnable: Runnable
) {
    MainExecutor.remove(runnable)
    MainExecutor.delay(runnable, delayMillis)
}

fun _removeMainRunnable(runnable: Runnable?) {
    runnable ?: return
    MainExecutor.remove(runnable)
}

fun _runMainRunnable(runnable: Runnable) {
    MainExecutor.execute(runnable)
}

fun _runMainRunnableDelay(
    delayMillis: Long = app().resources.getInteger(R.integer.lib_animation_delay).toLong(),
    runnable: Runnable
) {
    MainExecutor.delay(runnable, delayMillis)
}

fun onMainDelay(
    delayMillis: Long = app().resources.getInteger(R.integer.lib_animation_delay).toLong(),
    runnable: Runnable
) {
    _runMainRunnableDelay(delayMillis, runnable)
}

/**延迟处理*/
fun _delay(
    delayMillis: Long = app().resources.getInteger(R.integer.lib_animation_delay).toLong(),
    action: Action
): Action {
    val run = MainRunnable(action)
    MainExecutor.handler.postDelayed(run, delayMillis)
    _lastRunnable = WeakReference(run)
    return action
}

/**移除最后一个[Action]*/
fun _delayCancel() {
    _lastRunnable?.get()?.also {
        it.cancel.set(true)
        it.action = null
        MainExecutor.handler.removeCallbacks(it)
    }
    _lastRunnable?.clear()
    _lastRunnable = null
}