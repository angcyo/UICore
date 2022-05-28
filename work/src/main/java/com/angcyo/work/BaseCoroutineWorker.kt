package com.angcyo.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters

/**
 * https://developer.android.google.cn/topic/libraries/architecture/workmanager/how-to/define-work#coroutineworker
 *
 * https://developer.android.google.cn/topic/libraries/architecture/workmanager/advanced/coroutineworker
 *
 * [com.angcyo.work.BaseWorker]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/05/28
 */
abstract class BaseCoroutineWorker(context: Context, workerParameters: WorkerParameters) :
    CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        /*withContext(Dispatchers.IO) {
            val data = downloadSynchronously("https://www.google.com")
            saveData(data)
            return Result.success()
        }*/
        return Result.success()
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return super.getForegroundInfo()
    }
}