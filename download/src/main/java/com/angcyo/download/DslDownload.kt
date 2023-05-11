package com.angcyo.download

import android.content.Context
import android.util.ArrayMap
import com.angcyo.core.component.interceptor.LogFileInterceptor
import com.angcyo.download.DslDownload._taskIdMap
import com.angcyo.download.DslDownload.defaultDownloadFolder
import com.angcyo.library.L
import com.angcyo.library.ex.isDebugType
import com.angcyo.library.ex.isShowDebug
import com.angcyo.library.utils.FileUtils
import com.liulishuo.okdownload.DownloadTask
import com.liulishuo.okdownload.OkDownload
import com.liulishuo.okdownload.OkDownloadProvider
import com.liulishuo.okdownload.StatusUtil
import com.liulishuo.okdownload.core.Util
import com.liulishuo.okdownload.core.breakpoint.BreakpointStoreOnSQLite
import com.liulishuo.okdownload.core.cause.EndCause
import com.liulishuo.okdownload.core.connection.DownloadOkHttp3Connection
import com.liulishuo.okdownload.core.dispatcher.DownloadDispatcher
import com.liulishuo.okdownload.core.dispatcher.RCallbackDispatcher
import com.liulishuo.okdownload.core.dispatcher.UnifiedTransmitListener
import okhttp3.OkHttpClient
import java.util.concurrent.CopyOnWriteArrayList


/**
 * https://github.com/lingochamp/okdownload
 *
 * 2020-01-26
 * 1.0.7
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/26
 */

object DslDownload {

    /**默认文件夹名称*/
    var defaultDownloadFolder = "download"

    /**url和任务id对应关系*/
    val _taskIdMap = ArrayMap<String, Int>()

    /**初始化*/
    fun init(
        content: Context? = null,
        debug: Boolean = isShowDebug(),
        configOkHttp: ((OkHttpClient.Builder) -> Unit)? = null
    ) {
        val ctx = content ?: OkDownloadProvider.context
        OkDownload.Builder(ctx)
            .callbackDispatcher(RCallbackDispatcher())
            .downloadStore(BreakpointStoreOnSQLite(ctx))
            .connectionFactory(DownloadOkHttp3Connection.Factory().apply {
                setBuilder(OkHttpClient.Builder().apply {
                    addInterceptor(LogFileInterceptor().apply {
                        printLog = true
                        logRequestBody = false
                        logResponseBody = false
                    })
                    configOkHttp?.invoke(this)
                })
            })
            .build()
            .apply {
                OkDownload.setSingletonInstance(this)
            }

        //OkDownload.with().setMonitor(monitor);

        //最大任务数量, 默认5
        DownloadDispatcher.setMaxParallelRunningCount(5)

        //数据库入库频率, 默认1500
        //RemitStoreOnSQLite.setRemitToDBDelayMillis(300)

        //移除保存的断点信息
        //OkDownload.with().breakpointStore().remove(taskId)

        if (debug) {
            Util.enableConsoleLog()
        }
    }


    /**取消所有下载的任务*/
    fun cancelAll() {
        OkDownload.with().downloadDispatcher().cancelAll()
    }

    /**取消任务*/
    fun cancel(url: String?) {
        OkDownload.with().downloadDispatcher().cancel(taskId(url))
    }

    fun taskId(url: String?): Int {
        return _taskIdMap[url] ?: defaultBuilder(url)?.build()?.id ?: -1
    }

//    /**串行批量下载*/
//    fun down(listener: FDownloadListener): DownloadSerialQueue {
//        val serialQueue = DownloadSerialQueue(listener)
//
////        serialQueue.enqueue(task1)
////        serialQueue.enqueue(task2)
////
////        serialQueue.pause()
////
////        serialQueue.resume()
////
////        val workingTaskId = serialQueue.workingTaskId
////        val waitingTaskCount = serialQueue.waitingTaskCount
////
////        val discardTasks = serialQueue.shutdown()
//
//        return serialQueue
//    }

    /**获取任务状态*/
    fun getStatus(url: String?): StatusUtil.Status {
        return defaultBuilder(url)?.run {
            StatusUtil.getStatus(build())
        } ?: StatusUtil.Status.UNKNOWN
    }

    fun getStatus(task: DownloadTask): StatusUtil.Status {
        return StatusUtil.getStatus(task)
    }

    /**获取任务下载进度[0-100]*/
    fun getTaskProgress(task: DownloadTask?): Float {
        if (task == null) {
            return 0f
        }
        var percent = 0f
        val info = StatusUtil.getCurrentInfo(task)
        if (info != null) {
            val totalLength = info.totalLength
            val totalOffset = info.totalOffset

            percent = if (totalLength <= 0) {
                0f
            } else {
                totalOffset * 100f / totalLength
            }
        }
        return percent
    }

    /**查找已经存在的任务, 否则创建新任务*/
    fun findTask(url: String?): DownloadTask? {
        var result: DownloadTask? = null
        defaultBuilder(url)?.build()?.apply {
            result = OkDownload.with().downloadDispatcher().findSameTask(this) ?: this
        }
        return result
    }

    fun isCompleted(url: String?): Boolean {
        return getTaskStatus(findTask(url)) == StatusUtil.Status.COMPLETED
    }

    fun getTaskStatus(task: DownloadTask?): StatusUtil.Status {
        if (task == null) {
            return StatusUtil.Status.UNKNOWN
        }
        return getStatus(task)
    }

    /**监听下载地址*/
    fun listener(url: String?, listener: FDownloadListener) {
        if (url.isNullOrBlank()) {
            return
        }
        val arrayList = UnifiedTransmitListener._listenerMap[url]

        if (arrayList == null) {
            val list = CopyOnWriteArrayList<FDownloadListener>()
            list.add(listener)
            UnifiedTransmitListener._listenerMap[url] = list
        } else if (!arrayList.contains(listener)) {
            arrayList.add(listener)
        } else {
            L.w("$url 已存在listener:$listener")
        }
    }

    fun removeListener(listener: FDownloadListener, cancelTaskOnEmptyListener: Boolean = true) {
        UnifiedTransmitListener._listenerMap.forEach { entry ->
            if (entry.value.contains(listener)) {
                entry.value.remove(listener)

                if (cancelTaskOnEmptyListener && entry.value.isEmpty()) {
                    cancel(entry.key)
                }
            }
        }
    }

    fun removeListener(
        url: String?,
        listener: FDownloadListener,
        cancelTaskOnEmptyListener: Boolean = true
    ) {
        if (url.isNullOrBlank()) {
            return
        }
        val arrayList = UnifiedTransmitListener._listenerMap[url]

        if (arrayList != null && arrayList.contains(listener)) {
            arrayList.remove(listener)

            if (cancelTaskOnEmptyListener && arrayList.isEmpty()) {
                cancel(url)
            }
        } else {
            L.w("$url 不存在listener:$listener")
        }
    }
}

data class DownloadConfig(

    /**如果已经完成, 是否需要跳过下载*/
    var passIfAlreadyCompleted: Boolean = !isDebugType(),

    /**只在wifi下载*/
    var isWifiRequired: Boolean = false,

    /**全部配置[DownloadTask.Builder]*/
    var onConfigTask: (DownloadTask.Builder) -> Unit = {},

    var onTaskStart: (DownloadTask) -> Unit = {},

    var onTaskProgress: (DownloadTask, progress: Float, speed: Long) -> Unit = { _, _, _ -> },

    /**[cause]任务结束的情况.*/
    var onTaskFinish: (DownloadTask, cause: EndCause, Exception?) -> Unit = { _, _, _ -> }
)

/**默认任务构造器*/
fun defaultBuilder(url: String?): DownloadTask.Builder? {
    var taskBuilder: DownloadTask.Builder? = null
    val externalFolder = FileUtils.appRootExternalFolder(folder = defaultDownloadFolder)
    if (externalFolder == null) {
        L.w("存储空间不可用")
    } else if (url.isNullOrBlank()) {
        L.w("download url is null.")
    } else if (!url.startsWith("http")) {
        L.w("download url is not http.")
    } else {
        DownloadTask.Builder(url, externalFolder).apply {
            setFilename(getFileNameFromUrl(url))
            taskBuilder = this
        }
    }
    return taskBuilder
}

/**扩展[String], 开始下载*/
fun String.download(config: DownloadConfig.() -> Unit = {}): DownloadTask? {
    return dslDownload(this, config)
}

fun String?.getTaskStatus() = DslDownload.getStatus(this)

fun String?.cancelDownload() = DslDownload.cancel(this)

/**扩展[String], 监听下载事件*/
fun String.listener(listener: FDownloadListener) {
    DslDownload.listener(this, listener)
}

/**扩展[String], 移除监听下载事件*/
fun String.removeListener(listener: FDownloadListener) {
    DslDownload.removeListener(this, listener)
}

/**下载文件
 * [com.liulishuo.okdownload.DownloadTask.getFile] 下载的目标文件
 * */
fun dslDownload(url: String?, config: DownloadConfig.() -> Unit = {}): DownloadTask? {
    var task: DownloadTask? = null
    val downloadConfig = DownloadConfig()
    downloadConfig.config()
    defaultBuilder(url)?.apply {
        setConnectionCount(1)
        setAutoCallbackToUIThread(true)
        setMinIntervalMillisCallbackProcess(1_000) //24帧:60 60帧:16
        setPriority(0)
        setFilename(getFileNameFromUrl(url)) //只能设置下载的文件名, 不能设置路径
        setPassIfAlreadyCompleted(downloadConfig.passIfAlreadyCompleted)
        setWifiRequired(downloadConfig.isWifiRequired)

        downloadConfig.onConfigTask(this)

        task = build()
    }
    task?.apply {
        _taskIdMap[url] = id
//        listener(url, DslListener().apply {
//            removeOnCompleted = downloadConfig.removeOnCompleted
//            onTaskStart = downloadConfig.onTaskStart
//            onTaskProgress = downloadConfig.onTaskProgress
//            onTaskFinish = downloadConfig.onTaskFinish
//        })

        start(downloadConfig)
    }
    return task
}