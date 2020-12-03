package com.angcyo.http.progress

import com.angcyo.library.L
import me.jessyan.progressmanager.ProgressListener
import me.jessyan.progressmanager.ProgressManager
import me.jessyan.progressmanager.body.ProgressInfo

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/12/03
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */

object HttpProgress {

}

/**Http下载进度*/
fun String.downloadProgress(
    removeOnEnd: Boolean = true,
    action: (ProgressInfo?, Exception?) -> Unit
): ProgressListener {
    val url = this
    val listener = object : ProgressListener {
        override fun onProgress(progressInfo: ProgressInfo?) {
            //单位为byte/s
            //progressInfo?.speed
            //80,100
            //progressInfo?.percent
            L.d("download(${hashCode()}):$url ${progressInfo?.percent} ${progressInfo?.currentbytes}/${progressInfo?.contentLength} ${progressInfo?.speed} byte/s")
            action(progressInfo, null)
            if (removeOnEnd && progressInfo?.percent ?: 0 >= 100) {
                this.removeDownloadProgress()
            }
        }

        override fun onError(id: Long, e: Exception?) {
            action(null, e)
            if (removeOnEnd && e != null) {
                this.removeDownloadProgress()
            }
        }
    }
    ProgressManager.getInstance().addResponseListener(this, listener)
    return listener
}

/**移除下载进度监听*/
fun String.removeDownloadProgress() {
    ProgressManager.getInstance().removeResponseListener(this)
}

/**移除下载进度监听*/
fun ProgressListener.removeDownloadProgress() {
    ProgressManager.getInstance().removeResponseListener(this)
}

/**Http 上传进度*/
fun String.uploadProgress(
    removeOnEnd: Boolean = true,
    action: (ProgressInfo?, Exception?) -> Unit
): ProgressListener {
    val url = this
    val listener = object : ProgressListener {
        override fun onProgress(progressInfo: ProgressInfo?) {
            //单位为byte/s
            //progressInfo?.speed
            //80,100
            //progressInfo?.percent
            L.d("upload(${hashCode()}):$url ${progressInfo?.percent} ${progressInfo?.currentbytes}/${progressInfo?.contentLength} ${progressInfo?.speed} byte/s")
            action(progressInfo, null)
            if (removeOnEnd && progressInfo?.percent ?: 0 >= 100) {
                this.removeUploadProgress()
            }
        }

        override fun onError(id: Long, e: Exception?) {
            action(null, e)
            if (removeOnEnd && e != null) {
                this.removeUploadProgress()
            }
        }
    }
    ProgressManager.getInstance().addRequestListener(this, listener)
    return listener
}

/**移除上传进度监听*/
fun String.removeUploadProgress() {
    ProgressManager.getInstance().removeRequestListener(this)
}

/**移除上传进度监听*/
fun ProgressListener.removeUploadProgress() {
    ProgressManager.getInstance().removeRequestListener(this)
}

