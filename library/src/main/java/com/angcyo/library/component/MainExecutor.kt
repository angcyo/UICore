package com.angcyo.library.component

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor
import java.util.concurrent.RejectedExecutionException

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/14
 */

object MainExecutor : Executor {
    private val handler = Handler(Looper.getMainLooper())

    override fun execute(command: Runnable) {
        if (!handler.post(command)) {
            throw RejectedExecutionException("$handler is shutting down")
        }
    }
}