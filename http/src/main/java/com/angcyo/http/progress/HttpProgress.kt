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

/**相同下载地址, 不同下载内容*/
fun String.newProgressUrl(key: String) = ProgressManager.newUrl(this, key)

/**Http下载进度*/
fun String.downloadProgress(
    removeOnEnd: Boolean = true,
    key: String? = null,
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
    if (key == null) {
        ProgressManager.getInstance().addResponseListener(this, listener)
    } else {
        ProgressManager.getInstance().addDiffResponseListenerOnSameUrl(this, key, listener)
    }
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

/**Http 上传进度
 * [key] 通常上传接口都是一样的, 但是上传的文件路径不一样*/
fun String.uploadProgress(
    removeOnEnd: Boolean = true,
    key: String? = null,
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
    if (key == null) {
        ProgressManager.getInstance().addRequestListener(this, listener)
    } else {
        ProgressManager.getInstance().addDiffRequestListenerOnSameUrl(this, key, listener)
    }
    return listener
}

/**移除上传进度监听*/
fun String.removeUploadProgress(key: String? = null) {
    if (key == null) {
        ProgressManager.getInstance().removeRequestListener(this)
    } else {
        ProgressManager.getInstance().removeRequestListener(this, key)
    }
}

/**移除上传进度监听*/
fun ProgressListener.removeUploadProgress() {
    ProgressManager.getInstance().removeRequestListener(this)
}

