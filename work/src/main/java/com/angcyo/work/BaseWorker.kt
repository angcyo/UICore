package com.angcyo.work

import android.content.Context
import androidx.work.ForegroundInfo
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.common.util.concurrent.ListenableFuture

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/05/28
 */
abstract class BaseWorker(context: Context, workerParameters: WorkerParameters) :
    Worker(context, workerParameters) {

    /**
     * Result.success()：工作成功完成。
     * Result.failure()：工作失败。
     * Result.retry()：工作失败，应根据其重试政策在其他时间尝试。
     * */
    override fun doWork(): Result {
        // Do the work here--in this case, upload the images.
        //uploadImages() //任务操作

        //inputData.getString()

        /*val firstUpdate = workDataOf(Progress to 0)
        val lastUpdate = workDataOf(Progress to 100)
        setProgress(firstUpdate)
        delay(delayDuration)
        setProgress(lastUpdate)*/

        // Indicate whether the work finished successfully with the Result
        return Result.success()
    }

    /**
     * 在 Android 12 之前，工作器中的 getForegroundInfoAsync() 和 getForegroundInfo() 方法可让 WorkManager 在您调用 setExpedited() 时显示通知。
     * 如果您想要请求任务作为加急作业运行，则所有的 ListenableWorker 都必须实现 getForegroundInfo 方法。
     * */
    override fun getForegroundInfoAsync(): ListenableFuture<ForegroundInfo> {
        return super.getForegroundInfoAsync()
    }
}