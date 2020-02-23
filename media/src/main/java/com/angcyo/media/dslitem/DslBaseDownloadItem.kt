package com.angcyo.media.dslitem

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
    var _downTask: DownloadTask? = null

    var isItemViewDetached = false

    init {
        itemViewDetachedToWindow = { itemHolder, itemPosition ->
            onItemViewDetachedToWindow(itemHolder, itemPosition)
        }
        itemViewRecycled = { itemHolder, itemPosition ->
            onItemViewRecycled(itemHolder, itemPosition)
        }
    }

    open fun onItemViewDetachedToWindow(itemHolder: DslViewHolder, itemPosition: Int) {
        isItemViewDetached = true
        _downTask?.cancel()
    }

    open fun onItemViewRecycled(itemHolder: DslViewHolder, itemPosition: Int) {

    }

    /**下载*/
    open fun download(itemHolder: DslViewHolder?, url: String?, callback: (path: String) -> Unit) {
        isItemViewDetached = false
        _downTask?.cancel()
        _downTask = dslDownload(url) {
            onTaskStart = {
                onDownloadStart(itemHolder, it)
            }
            onTaskFinish = { downloadTask, cause, exception ->
                onDownloadFinish(itemHolder, downloadTask, cause, exception)
                if (cause == EndCause.COMPLETED && !isItemViewDetached) {
                    callback(downloadTask.file!!.absolutePath)
                }
            }
        }
    }

    open fun onDownloadStart(itemHolder: DslViewHolder?, task: DownloadTask) {

    }

    open fun onDownloadFinish(
        itemHolder: DslViewHolder?,
        task: DownloadTask,
        cause: EndCause,
        error: Exception?
    ) {

    }
}