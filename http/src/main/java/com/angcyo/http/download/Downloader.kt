package com.angcyo.http.download

import com.angcyo.library.L
import com.angcyo.library.ex.getFileAttachmentName
import com.angcyo.library.libCacheFile

/**
 * [OkHttp]下载器
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/07/09
 */

/**简单的OkHttp下载文件, 覆盖原文件
 * 不支持断点下载, 不存数据库
 * */
fun String.download(
    savePath: String? = null,
    config: DownloadTask.() -> Unit = {},
    action: (task: DownloadTask, error: Throwable?) -> Unit
): DownloadTask {
    val name = getFileAttachmentName()
    val path = savePath ?: libCacheFile(name!!).absolutePath
    val task = DownloadTask(this, path, object : DownloadListener {
        override fun onDownloadSuccess(task: DownloadTask) {
            action(task, null)
        }

        override fun onDownloading(task: DownloadTask, progress: Int) {
            L.v("下载:${task.url} -> ${task.savePath} 进度:$progress%")
            action(task, null)
        }

        override fun onDownloadFailed(task: DownloadTask, error: Throwable) {
            action(task, error)
        }
    })
    task.config()
    task.download()//开始下载
    return task
}