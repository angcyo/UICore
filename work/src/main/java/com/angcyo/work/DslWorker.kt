package com.angcyo.work

import android.content.Context
import androidx.work.*
import com.angcyo.library.app
import com.angcyo.library.ex.nowTimeString
import java.util.concurrent.TimeUnit

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/05/28
 */
object DslWorker {

    //fun getAllWorkTags(context: Context = app()) =WorkManager.getInstance(context).getWorkInfos()

}

/**取消带有特定标记的所有工作请求*/
fun String.cancelAllWorkByTag(context: Context = app()) = WorkManager.getInstance(context)
    .cancelAllWorkByTag(this) // 例如， 会，WorkManager.getWorkInfosByTag(String)

/**返回一个 WorkInfo 对象列表，该列表可用于确定当前工作状态*/
fun String.getWorkInfosByTag(context: Context = app()) =
    WorkManager.getInstance(context).getWorkInfosByTag(this)

/**一次性的任务*/
inline fun <reified T : ListenableWorker> T.oneTimeWork(
    tag: String = nowTimeString(),
    context: Context = app(),
    block: OneTimeWorkRequest.Builder.() -> Unit = {}
): Operation {
    val request = OneTimeWorkRequestBuilder<T>().apply {
        //setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)//加急处理

        /*val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .setRequiresCharging(true)
            .build()
        setConstraints(constraints) //工作约束*/

        //setInitialDelay() //延迟工作

        //setBackoffCriteria(BackoffPolicy.LINEAR, OneTimeWorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS) //重试策略

        addTag(tag) //标记, 可以用来取消

        //setInputData()

        block()
    }.build()
    return WorkManager.getInstance(context).enqueue(request)
}

/**持续性的任务
 * 注意：可以定义的最短重复间隔是 15 分钟（与 JobScheduler API 相同）。*/
inline fun <reified T : ListenableWorker> T.periodicWork(
    tag: String = nowTimeString(),
    repeatInterval: Long = PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS,
    repeatIntervalTimeUnit: TimeUnit = TimeUnit.MILLISECONDS,
    context: Context = app(),
    block: PeriodicWorkRequest.Builder.() -> Unit = {}
): Operation {
    val request = PeriodicWorkRequestBuilder<T>(repeatInterval, repeatIntervalTimeUnit).apply {
        addTag(tag)
        block()
    }.build()
    return WorkManager.getInstance(context).enqueue(request)
}