package com.liulishuo.okdownload.core.dispatcher

import android.os.Handler
import android.util.ArrayMap
import com.angcyo.download.FDownloadListener
import com.angcyo.library.L
import com.liulishuo.okdownload.DownloadTask
import com.liulishuo.okdownload.core.breakpoint.BreakpointInfo
import com.liulishuo.okdownload.core.cause.EndCause
import com.liulishuo.okdownload.core.cause.ResumeFailedCause
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 进度转发器
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/26
 */
internal class UnifiedTransmitListener(val handler: Handler) :
    CallbackDispatcher.DefaultTransmitListener(handler) {

    companion object {
        val _listenerMap: ArrayMap<String, CopyOnWriteArrayList<FDownloadListener>> = ArrayMap()
    }

    override fun connectTrialEnd(
        task: DownloadTask,
        responseCode: Int,
        headerFields: MutableMap<String, MutableList<String>>
    ) {
        super.connectTrialEnd(task, responseCode, headerFields)
        _eachTaskListener(task) {
            it.connectTrialEnd(task, responseCode, headerFields)
        }
    }

    override fun inspectTaskStart(task: DownloadTask?) {
        super.inspectTaskStart(task)
    }

    override fun fetchEnd(task: DownloadTask, blockIndex: Int, contentLength: Long) {
        super.fetchEnd(task, blockIndex, contentLength)
        _eachTaskListener(task) {
            it.fetchEnd(task, blockIndex, contentLength)
        }
    }

    override fun downloadFromBeginning(
        task: DownloadTask,
        info: BreakpointInfo,
        cause: ResumeFailedCause
    ) {
        super.downloadFromBeginning(task, info, cause)
        _eachTaskListener(task) {
            it.downloadFromBeginning(task, info, cause)
        }
    }

    override fun connectTrialStart(
        task: DownloadTask,
        headerFields: MutableMap<String, MutableList<String>>
    ) {
        super.connectTrialStart(task, headerFields)
        _eachTaskListener(task) {
            it.connectTrialStart(task, headerFields)
        }
    }

    override fun inspectDownloadFromBreakpoint(task: DownloadTask, info: BreakpointInfo) {
        super.inspectDownloadFromBreakpoint(task, info)
    }

    override fun downloadFromBreakpoint(task: DownloadTask, info: BreakpointInfo) {
        super.downloadFromBreakpoint(task, info)
        _eachTaskListener(task) {
            it.downloadFromBreakpoint(task, info)
        }
    }

    override fun fetchStart(task: DownloadTask, blockIndex: Int, contentLength: Long) {
        super.fetchStart(task, blockIndex, contentLength)
        _eachTaskListener(task) {
            it.fetchStart(task, blockIndex, contentLength)
        }
    }

    override fun connectEnd(
        task: DownloadTask,
        blockIndex: Int,
        responseCode: Int,
        requestHeaderFields: MutableMap<String, MutableList<String>>
    ) {
        super.connectEnd(task, blockIndex, responseCode, requestHeaderFields)
        _eachTaskListener(task) {
            it.connectEnd(task, blockIndex, responseCode, requestHeaderFields)
        }
    }

    override fun inspectDownloadFromBeginning(
        task: DownloadTask,
        info: BreakpointInfo,
        cause: ResumeFailedCause
    ) {
        super.inspectDownloadFromBeginning(task, info, cause)
    }

    override fun connectStart(
        task: DownloadTask,
        blockIndex: Int,
        requestHeaderFields: MutableMap<String, MutableList<String>>
    ) {
        super.connectStart(task, blockIndex, requestHeaderFields)
        _eachTaskListener(task) {
            it.connectStart(task, blockIndex, requestHeaderFields)
        }
    }

    override fun inspectTaskEnd(task: DownloadTask?, cause: EndCause?, realCause: Exception?) {
        super.inspectTaskEnd(task, cause, realCause)
    }

    override fun taskStart(task: DownloadTask) {
        super.taskStart(task)
        _eachTaskListener(task) {
            it.taskStart(task)
        }
    }

    override fun taskEnd(task: DownloadTask, cause: EndCause, realCause: Exception?) {
        super.taskEnd(task, cause, realCause)
        _eachTaskListener(task) {
            it.taskEnd(task, cause, realCause)
        }
        if (cause == EndCause.COMPLETED) {
            _clearListener(task)
        }
    }

    override fun fetchProgress(task: DownloadTask, blockIndex: Int, increaseBytes: Long) {
        super.fetchProgress(task, blockIndex, increaseBytes)
        _eachTaskListener(task) {
            it.fetchProgress(task, blockIndex, increaseBytes)
        }
    }

    /**获取[DownloadTask]对应的有效[Listener]*/
    fun _eachTaskListener(task: DownloadTask, action: (FDownloadListener) -> Unit) {
        _listenerMap[task.url]?.forEach {
            if (task.isAutoCallbackToUIThread) {
                handler.post { action(it) }
            } else {
                action(it)
            }
        }
    }

    /**[DownloadTask]下载结束之后, 清理[Listener]*/
    fun _clearListener(task: DownloadTask) {
        L.w("清理回调:${task.url}")
        _listenerMap[task.url]?.apply {
            val list = ArrayList(this)
            list.forEach {
                if (it.removeOnCompleted) {
                    remove(it)
                }
            }
        }
    }
}