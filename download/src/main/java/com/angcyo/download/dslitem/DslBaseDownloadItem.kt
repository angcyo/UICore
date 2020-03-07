package com.angcyo.download.dslitem

import com.angcyo.download.dslDownload
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.widget.DslViewHolder
import com.liulishuo.okdownload.DownloadTask
import com.liulishuo.okdownload.core.cause.EndCause

/**
 * 需要下载的item基类
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/23
 */

abstract class DslBaseDownloadItem : DslAdapterItem() {

    /**下载开始的回调*/
    var onItemDownloadStart: (itemHolder: DslViewHolder?, task: DownloadTask) -> Unit = { _, _ -> }

    /**下载结束的回调*/
    var onItemDownloadFinish: (
        itemHolder: DslViewHolder?,
        task: DownloadTask,
        cause: EndCause,
        error: Exception?
    ) -> Unit = { _, _, _, _ ->

    }

    /**下载进度*/
    var onItemDownloadProgress: (DownloadTask, progress: Int, speed: Long) -> Unit = { _, _, _ -> }

    //当前任务
    var _downTask: DownloadTask? = null

    //是否被Detached过, 用于下载成功后判断是否需要继续之前的操作
    var _isItemViewDetached = false

    override fun onItemViewDetachedToWindow(itemHolder: DslViewHolder, itemPosition: Int) {
        super.onItemViewDetachedToWindow(itemHolder, itemPosition)
        _isItemViewDetached = true
        _downTask?.cancel()
    }

    /**下载*/
    open fun download(
        itemHolder: DslViewHolder?,
        url: String?,
        callback: (path: String) -> Unit = {}
    ) {
        _isItemViewDetached = false
        _downTask?.cancel()
        _downTask = dslDownload(url) {
            onTaskStart = {
                onDownloadStart(itemHolder, it)
            }
            onTaskFinish = { downloadTask, cause, exception ->
                onDownloadFinish(itemHolder, downloadTask, cause, exception)
                if (cause == EndCause.COMPLETED && !_isItemViewDetached) {
                    callback(downloadTask.file!!.absolutePath)
                }
            }
            onTaskProgress = { downloadTask, progress, speed ->
                onItemDownloadProgress(downloadTask, progress, speed)
            }
        }
    }

    open fun onDownloadStart(itemHolder: DslViewHolder?, task: DownloadTask) {
        onItemDownloadStart(itemHolder, task)
    }

    open fun onDownloadFinish(
        itemHolder: DslViewHolder?,
        task: DownloadTask,
        cause: EndCause,
        error: Exception?
    ) {
        onItemDownloadFinish(itemHolder, task, cause, error)
    }
}