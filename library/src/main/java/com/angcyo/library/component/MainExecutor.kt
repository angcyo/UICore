package com.angcyo.library.component

import android.os.Handler
import android.os.Looper
import com.angcyo.library.R
import com.angcyo.library.app
import com.angcyo.library.component.MainExecutor._lastAction
import com.angcyo.library.ex.Action
import java.lang.ref.WeakReference
import java.util.concurrent.Executor
import java.util.concurrent.RejectedExecutionException

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/14
 */

object MainExecutor : Executor {
    val handler = Handler(Looper.getMainLooper())

    var _lastAction: WeakReference<Action?>? = null

    override fun execute(command: Runnable) {
        if (!handler.post(command)) {
            throw RejectedExecutionException("$handler is shutting down")
        }
    }
}

/**延迟处理*/
fun _delay(
    delayMillis: Long = app().resources.getInteger(R.integer.lib_animation_delay).toLong(),
    action: Action
): Action {
    MainExecutor.handler.postDelayed(action, delayMillis)
    _lastAction = WeakReference(action)
    return action
}

/**移除最后一个[Action]*/
fun _delayCancel() {
    _lastAction?.get()?.also { MainExecutor.handler.removeCallbacks(it) }
    _lastAction?.clear()
    _lastAction = null
}