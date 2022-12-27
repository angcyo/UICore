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