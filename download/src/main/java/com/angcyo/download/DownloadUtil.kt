package com.angcyo.download

import android.net.Uri
import android.os.StatFs
import android.text.TextUtils
import com.angcyo.library.app
import com.angcyo.library.ex.fileSizeString
import com.liulishuo.okdownload.DownloadTask
import com.liulishuo.okdownload.StatusUtil
import com.liulishuo.okdownload.core.cause.EndCause
import java.net.URLDecoder

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/26
 */

/**
 * 获取文件名, 在url中
 */
fun getFileNameFromUrl(url: String?): String {
    var fileName: String = System.currentTimeMillis().toString() + ".unknown"
    try {
        var url = URLDecoder.decode(url, "UTF-8")
        val nameFrom: String = getFileNameFrom(url)
        if (!TextUtils.isEmpty(nameFrom)) {
            fileName = nameFrom
        }
        val parse = Uri.parse(url)
        val parameterNames =
            parse.queryParameterNames
        if (parameterNames.isEmpty()) {
        } else {
            var param = ""
            for (s in parameterNames) {
                param = parse.getQueryParameter(s) ?: ""
                try {
                    if ( /*s.contains("name") ||*/param.contains("name=")) {
                        break
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            try {
                fileName = param.split("name=").toTypedArray()[1]
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return fileName
}

fun getFileNameFrom(url: String): String {
    var _url = url
    var result = ""
    try {
        _url = _url.split("\\?").toTypedArray()[0]
        val indexOf = _url.lastIndexOf('/')
        if (indexOf != -1) {
            result = _url.substring(indexOf + 1)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return result
}

/**剩余空间大小*/
fun availableBlocksSize(path: String = app().getExternalFilesDir("")?.absolutePath ?: "/"): Long {
    val statFs = StatFs(path)
    //statFs.totalBytes -
    return statFs.availableBytes
}

/**
 * 计算任务增量 速率
 */
fun calcTaskSpeedString(task: DownloadTask, increaseBytes: Long): String {
    return calcTaskSpeed(task, increaseBytes).fileSizeString()
}

fun calcTaskSpeed(task: DownloadTask, increaseBytes: Long): Long {
    return (increaseBytes * 1f / task.minIntervalMillisCallbackProcess * 1000).toLong()
}

fun DownloadTask?.status(): StatusUtil.Status {
    return DslDownload.getTaskStatus(this)
}

fun DownloadTask?.isCompleted(): Boolean {
    return DslDownload.getTaskStatus(this) == StatusUtil.Status.COMPLETED
}

fun DownloadTask?.isRunning(): Boolean {
    return DslDownload.getTaskStatus(this) == StatusUtil.Status.RUNNING
}

fun DownloadTask?.isStart(): Boolean {
    return DslDownload.getTaskStatus(this) == StatusUtil.Status.PENDING || isRunning()
}

fun EndCause.isSucceed() = isCompleted()
fun EndCause.isCompleted() = this == EndCause.COMPLETED
fun EndCause.isError() = this == EndCause.ERROR
fun EndCause.isCancel() = this == EndCause.CANCELED

fun String?.findDownloadTask() = DslDownload.findTask(this)
fun DownloadTask.taskStatus() = DslDownload.getStatus(this)
fun DownloadTask.taskProgress() = DslDownload.getTaskProgress(this)

/**启动任务*/
fun DownloadTask?.start(downloadConfig: DownloadConfig? = null) {
    this?.let {
        if (it.listener == null) {
            enqueue(DslListener().apply {
                onTaskStart = downloadConfig?.onTaskStart
                onTaskProgress = downloadConfig?.onTaskProgress
                onTaskFinish = downloadConfig?.onTaskFinish
            })
        } else {
            it.listener?.also { listener ->
                it.enqueue(listener)
            }
        }
    }
}