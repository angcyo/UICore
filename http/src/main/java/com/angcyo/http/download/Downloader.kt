package com.angcyo.http.download

import com.angcyo.library.L
import com.angcyo.library.ex.getFileAttachmentName
import com.angcyo.library.libCacheFile
import com.angcyo.library.utils.fileNameUUID

/**
 * [OkHttp]下载器
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/07/09
 */

/**简单的OkHttp下载文件, 覆盖原文件
 * 不支持断点下载, 不存数据库
 *
 * ```
 * bitmapUrl.download(fileNameUUID(".png")) { task, error2 ->
 *     if (error2 == null) {
 *         //下载成功
 *         if (task.isFinish) {
 *             val bitmapPath = task.savePath
 *             action(bitmapPath)
 *         }
 *     } else {
 *         toastQQ(error2.message)
 *     }
 * }
 * ```
 *
 * ```
 * entity.fileUrl!!.download(absolutePath) { task, error ->
 *     if (task.isFinish || error != null) {
 *         action(file, error)
 *     }
 * }
 * ```
 *
 * [savePath] 保存的路径, 如果以/开头, 就是路径, 否则就是文件名
 * */
fun String.download(
    savePath: String? = null,
    config: DownloadTask.() -> Unit = {},
    overwrite: Boolean = false,
    action: (task: DownloadTask, error: Throwable?) -> Unit
): DownloadTask {
    val name = getFileAttachmentName()
    val path = if (savePath?.startsWith("/") == true) {
        savePath
    } else {
        libCacheFile(savePath ?: name ?: fileNameUUID()).absolutePath
    }
    val task = DownloadTask(this, path, object : DownloadListener {
        override fun onDownloadSuccess(task: DownloadTask) {
            action(task, null)
        }

        override fun onDownloading(task: DownloadTask, progress: Int) {
            L.v("下载进度:$progress%\n${task.url} -> ${task.savePath}")
            action(task, null)
        }

        override fun onDownloadFailed(task: DownloadTask, error: Throwable) {
            action(task, error)
        }
    }, overwrite)
    task.config()
    task.download()//开始下载
    return task
}