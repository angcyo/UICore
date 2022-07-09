package com.angcyo.http.download

import androidx.annotation.WorkerThread

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/07/09
 */
@WorkerThread
interface DownloadListener {

    /** 下载成功 */
    @WorkerThread
    fun onDownloadSuccess(task: DownloadTask) {
    }

    /** [progress]  下载进度[0~100] */
    @WorkerThread
    fun onDownloading(task: DownloadTask, progress: Int) {
    }

    /** 下载失败 */
    @WorkerThread
    fun onDownloadFailed(task: DownloadTask, error: Throwable) {
    }
}