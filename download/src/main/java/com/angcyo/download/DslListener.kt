package com.angcyo.download

import com.liulishuo.okdownload.DownloadTask
import com.liulishuo.okdownload.core.cause.EndCause

/**
 * https://github.com/lingochamp/okdownload/wiki/Download-Listener
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/26
 */
open class DslListener : FDownloadListener() {

    var onTaskStart: (DownloadTask) -> Unit = {}
    var onTaskProgress: (DownloadTask, progress: Int, speed: Long) -> Unit = { _, _, _ -> }
    var onTaskFinish: (DownloadTask, cause: EndCause, Exception?) -> Unit = { _, _, _ -> }

    override fun taskStart(task: DownloadTask) {
        super.taskStart(task)
        onTaskStart(task)
    }

    override fun taskEnd(task: DownloadTask, cause: EndCause, realCause: Exception?) {
        super.taskEnd(task, cause, realCause)
        onTaskFinish(task, cause, realCause)
    }

    override fun taskProgress(
        task: DownloadTask,
        totalLength: Long,
        totalOffset: Long,
        increaseBytes: Long,
        speed: Long
    ) {
        super.taskProgress(task, totalLength, totalOffset, increaseBytes, speed)
        val percent = (totalOffset * 100 / totalLength).toInt()
        onTaskProgress(task, percent, speed)
    }
}