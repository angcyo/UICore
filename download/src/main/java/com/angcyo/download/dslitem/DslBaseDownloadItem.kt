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
    var itemDownloadStart: (itemHolder: DslViewHolder?, task: DownloadTask) -> Unit =
        { itemHolder, task ->
            onDownloadStart(itemHolder, task)
        }

    /**下载结束的回调*/
    var itemDownloadFinish: (
        itemHolder: DslViewHolder?,
        task: DownloadTask,
        cause: EndCause,
        error: Exception?
    ) -> Unit = { itemHolder, task, cause, error ->
        onDownloadFinish(itemHolder, task, cause, error)
    }

    /**下载进度*/
    var itemDownloadProgress: (DownloadTask, progress: Int, speed: Long) -> Unit =
        { downloadTask, progress, speed -> onDownloadProgress(downloadTask, progress, speed) }

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
                itemDownloadStart(itemHolder, it)
            }
            onTaskFinish = { downloadTask, cause, exception ->
                itemDownloadFinish(itemHolder, downloadTask, cause, exception)
                if (cause == EndCause.COMPLETED && !_isItemViewDetached) {
                    callback(downloadTask.file!!.absolutePath)
                }
            }
            onTaskProgress = { downloadTask, progress, speed ->
                itemDownloadProgress(downloadTask, progress, speed)
            }
        }
    }

    //<editor-fold desc="任务回调">

    open fun onDownloadStart(itemHolder: DslViewHolder?, task: DownloadTask) {
    }

    open fun onDownloadFinish(
        itemHolder: DslViewHolder?,
        task: DownloadTask,
        cause: EndCause,
        error: Exception?
    ) {
    }

    open fun onDownloadProgress(task: DownloadTask, progress: Int, speed: Long) {
    }

    //</editor-fold desc="任务回调">

    //<editor-fold desc="获取任务信息">

    //</editor-fold desc="获取任务信息">
}