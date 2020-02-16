package com.angcyo.library.component.work

import java.util.concurrent.Executor

/**
 * Interface for executing common tasks in WorkManager.
 */
interface TaskExecutor {
    /**
     * @param runnable [Runnable] to post to the main thread
     */
    fun postToMainThread(runnable: Runnable)

    /**
     * @return The [Executor] for main thread task processing
     */
    val mainThreadExecutor: Executor

    /**
     * @param runnable [Runnable] to execute on a background thread pool
     */
    fun executeOnBackgroundThread(runnable: Runnable)

    /**
     * @return The [SerialExecutor] for background task processing
     */
    val backgroundExecutor: SerialExecutor
}