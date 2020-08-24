package com.angcyo.download

import com.angcyo.library.L
import com.angcyo.library.ex.fileSizeString
import com.liulishuo.okdownload.DownloadListener
import com.liulishuo.okdownload.DownloadTask
import com.liulishuo.okdownload.StatusUtil
import com.liulishuo.okdownload.core.breakpoint.BreakpointInfo
import com.liulishuo.okdownload.core.cause.EndCause
import com.liulishuo.okdownload.core.cause.ResumeFailedCause

/**
 * https://github.com/lingochamp/okdownload/wiki/Download-Listener
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/26
 */
open class FDownloadListener : DownloadListener {

    /**任务完成, 是否移除监听. 任务失败不会移除!*/
    var removeOnCompleted = true

    override fun connectTrialEnd(
        task: DownloadTask,
        responseCode: Int,
        responseHeaderFields: MutableMap<String, MutableList<String>>
    ) {
        L.d("this...")
    }

    override fun fetchEnd(task: DownloadTask, blockIndex: Int, contentLength: Long) {
        L.d("this...")
    }

    override fun downloadFromBeginning(
        task: DownloadTask,
        info: BreakpointInfo,
        cause: ResumeFailedCause
    ) {
        L.d("this...")
    }

    override fun connectTrialStart(
        task: DownloadTask,
        requestHeaderFields: MutableMap<String, MutableList<String>>
    ) {
        L.d("this...")
    }

    override fun downloadFromBreakpoint(task: DownloadTask, info: BreakpointInfo) {
        L.d("this...")
    }

    override fun fetchStart(task: DownloadTask, blockIndex: Int, contentLength: Long) {
        L.d("this...")
    }

    override fun connectEnd(
        task: DownloadTask,
        blockIndex: Int,
        responseCode: Int,
        responseHeaderFields: MutableMap<String, MutableList<String>>
    ) {
        L.d("this...")
    }

    override fun connectStart(
        task: DownloadTask,
        blockIndex: Int,
        requestHeaderFields: MutableMap<String, MutableList<String>>
    ) {
        L.d("this...")
    }

    /**常用回调*/
    override fun taskStart(task: DownloadTask) {
        L.d("下载:${task.id} ${task.url}->${task.file?.absolutePath}")
    }

    /**常用回调*/
    override fun taskEnd(task: DownloadTask, cause: EndCause, realCause: Exception?) {
        val info = StatusUtil.getCurrentInfo(task)
        L.d("this...$cause $realCause ${info?.totalLength?.fileSizeString()}")
    }

    override fun fetchProgress(task: DownloadTask, blockIndex: Int, increaseBytes: Long) {
        //8
        val info = StatusUtil.getCurrentInfo(task)
        if (info != null) {
            val totalLength = info.totalLength
            val totalOffset = info.totalOffset

            taskProgress(
                task,
                totalLength,
                totalOffset,
                increaseBytes,
                (increaseBytes * 1f / task.minIntervalMillisCallbackProcess * 1000L).toLong()
            )
        }
    }

    /**常用回调*/
    open fun taskProgress(
        task: DownloadTask,
        totalLength: Long,
        totalOffset: Long,
        increaseBytes: Long,
        speed: Long
    ) {
        val percent = (totalOffset * 100 / totalLength).toInt()
        //计算每秒多少
        val sp = "${speed.fileSizeString()}/s"
        val builder = StringBuilder()
        builder.append("\n下载进度:")
        builder.append(task.url)
        builder.append("\n总大小:")
        builder.append(totalLength.fileSizeString())
        builder.append(" 已下载:")
        builder.append(totalOffset.fileSizeString())
        builder.append(" ")
        builder.append(percent)
        builder.append("%")
        builder.append(" 新增:")
        builder.append(increaseBytes.fileSizeString())
        builder.append(" ")
        builder.append(sp)
        L.v(builder.toString())
    }
}