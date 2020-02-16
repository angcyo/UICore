package com.angcyo.library.component.work

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlin.math.max
import kotlin.math.min

/**
 * Default Task Executor for executing common tasks in WorkManager
 */
class WorkManagerTaskExecutor(
    backgroundExecutor: Executor =
    // This value is the same as the core pool size for AsyncTask#THREAD_POOL_EXECUTOR.
        Executors.newFixedThreadPool(max(2, min(Runtime.getRuntime().availableProcessors() - 1, 4)))
) : TaskExecutor {

    // Wrap it with a serial executor so we have ordering guarantees on commands
    // being executed.
    override val backgroundExecutor: SerialExecutor = SerialExecutor(backgroundExecutor)

    private val mainThreadHandler = Handler(Looper.getMainLooper())
    override val mainThreadExecutor = Executor { command -> postToMainThread(command) }

    override fun postToMainThread(runnable: Runnable) {
        mainThreadHandler.post(runnable)
    }

    override fun executeOnBackgroundThread(runnable: Runnable) {
        backgroundExecutor.execute(runnable)
    }
}