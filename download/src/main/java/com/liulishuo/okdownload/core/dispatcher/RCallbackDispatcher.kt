package com.liulishuo.okdownload.core.dispatcher

import android.os.Handler
import android.os.Looper
import com.liulishuo.okdownload.DownloadListener
import com.liulishuo.okdownload.DownloadTask

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/26
 */

class RCallbackDispatcher(handler: Handler = Handler(Looper.getMainLooper())) :
    CallbackDispatcher(handler, UnifiedTransmitListener(handler)) {

    override fun isFetchProcessMoment(task: DownloadTask?): Boolean {
        return super.isFetchProcessMoment(task)
    }

    override fun endTasksWithCanceled(canceledCollection: MutableCollection<DownloadTask>) {
        super.endTasksWithCanceled(canceledCollection)
    }

    override fun endTasksWithError(
        errorCollection: MutableCollection<DownloadTask>,
        realCause: Exception
    ) {
        super.endTasksWithError(errorCollection, realCause)
    }

    override fun endTasks(
        completedTaskCollection: MutableCollection<DownloadTask>,
        sameTaskConflictCollection: MutableCollection<DownloadTask>,
        fileBusyCollection: MutableCollection<DownloadTask>
    ) {
        super.endTasks(completedTaskCollection, sameTaskConflictCollection, fileBusyCollection)
    }

    override fun dispatch(): DownloadListener {
        return super.dispatch()
    }
}