package com.angcyo.library.component

import android.os.Handler
import android.os.Looper
import com.angcyo.library.R
import com.angcyo.library.app
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

    override fun execute(command: Runnable) {
        if (!handler.post(command)) {
            throw RejectedExecutionException("$handler is shutting down")
        }
    }
}

/**延迟处理*/
fun _delay(
    delayMillis: Long = app().resources.getInteger(R.integer.lib_animation_delay).toLong(),
    action: () -> Unit
) {
    MainExecutor.handler.postDelayed(action, delayMillis)
}