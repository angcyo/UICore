package com.angcyo.download

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.view.View
import androidx.core.app.NotificationCompat
import com.angcyo.base.dslAHelper
import com.angcyo.library.L
import com.angcyo.library.app
import com.angcyo.library.component.*
import com.angcyo.library.ex.fileSizeString
import com.angcyo.library.ex.toBitmap
import com.liulishuo.okdownload.DownloadTask
import com.liulishuo.okdownload.StatusUtil
import com.liulishuo.okdownload.core.cause.EndCause

/**
 * 下载通知显示
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/03
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
class DslDownloadNotify : DslListener() {

    companion object {
        /**已经安装过的notify*/
        val _notifyList = mutableListOf<String>()

        const val ACTION_CANCEL_DOWNLOAD = "action_cancel_download:"
    }

    var _downloadUrl: String? = null
    var _notifyId: Int = 0

    /**通知logo*/
    var logo: Bitmap? = null

    /**下载完成, 自动安装apk*/
    var autoInstallApk = true

    var _cancelIntent = Intent()

    var _cancelReceiver: CancelDownloadBroadcastReceiver = CancelDownloadBroadcastReceiver()

    init {
        logo = app().packageName.appBean()!!.appIcon.toBitmap()

        onTaskStart = {
            _notify {
                _remoteView(it.filename, -1) {
                    setTextViewText(
                        R.id.lib_sub_text_view,
                        "正在准备下载..."
                    )
                }
            }
        }

        onTaskFinish = { downloadTask, cause, exception ->
            uninstall(false)

            val filename = downloadTask.filename
            _notify {
                notifyOngoing = false

                val process = if (cause == EndCause.COMPLETED) 100 else -100
                _remoteView(filename, process) {
                    setViewVisibility(R.id.lib_delete_view, View.GONE)

                    if (cause == EndCause.COMPLETED) {
                        if (filename?.toLowerCase()?.endsWith("apk") == true) {
                            setTextViewText(R.id.lib_sub_text_view, "下载完成, 点击安装!")

                            //安装APK Intent
                            DslIntent.getInstallAppIntent(downloadTask.file)?.run {
                                notifyContentIntent = DslNotify.pendingActivity(app(), this)

                                if (autoInstallApk) {
                                    //立即安装
                                    app().dslAHelper {
                                        start(this@run)
                                    }
                                }
                            }
                        } else {
                            setTextViewText(
                                R.id.lib_sub_text_view,
                                "下载完成:${downloadTask.file?.absolutePath}"
                            )
                        }
                    } else if (cause == EndCause.CANCELED) {
                        setTextViewText(R.id.lib_sub_text_view, "下载已取消!")
                    } else {
                        setTextViewText(R.id.lib_sub_text_view, "下载失败:${exception?.message}")
                    }
                }
            }
        }
    }

    fun _notify(action: DslNotify.() -> Unit) {
        dslNotify {
            channelName = "文件下载"
            notifyId = _notifyId
            notifyOngoing = true
            notifyDefaults = NotificationCompat.DEFAULT_VIBRATE
            notifyPriority = NotificationCompat.PRIORITY_HIGH
            action()
        }
    }

    fun DslNotify._remoteView(
        name: String? = null,
        process: Int = -1,
        ico: Int = -1,
        action: DslRemoteView.() -> Unit = {}
    ) {
        notifyCustomContentView =
            dslRemoteView {
                layoutId = R.layout.layout_download_notify
                if (name != null) {
                    setTextViewText(R.id.lib_text_view, name)
                }
                if (ico > 0) {
                    setImageViewResource(R.id.lib_image_view, ico)
                } else {
                    logo?.run {
                        setImageViewBitmap(R.id.lib_image_view, this)
                    }
                }
                if (process != -100) {
                    setProgressBar(R.id.lib_progress_bar, process)
                }
                //delete
                setViewVisibility(
                    R.id.lib_delete_view,
                    if (process != 100 && process != -100) View.VISIBLE else View.GONE
                )
                //click
                if (process != 100) {
                    setClickPending(
                        R.id.lib_delete_view,
                        DslNotify.pendingBroadcast(app(), _cancelIntent)
                    )
                }
                action()
            }
    }

    /**安装*/
    fun install(url: String) {
        if (_notifyList.contains(url)) {
            L.w("已经监听了:$url")
            return
        }
        uninstall()
        _notifyList.add(url)

        _downloadUrl = url
        _notifyId = url.hashCode()//System.currentTimeMillis().toInt()
        url.listener(this)

        try {
            val action = "$ACTION_CANCEL_DOWNLOAD$url"
            _cancelIntent.action = action
            app().registerReceiver(_cancelReceiver, IntentFilter().apply {
                addAction(action)
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun uninstall(cancelNotify: Boolean = true) {
        _notifyList.remove(_downloadUrl)

        if (_notifyId > 0 && cancelNotify) {
            DslNotify.cancelNotify(app(), _notifyId)
        }

        _downloadUrl?.removeListener(this)

        try {
            app().unregisterReceiver(_cancelReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun taskProgress(
        task: DownloadTask,
        totalLength: Long,
        totalOffset: Long,
        increaseBytes: Long,
        speed: Long
    ) {
        super.taskProgress(task, totalLength, totalOffset, increaseBytes, speed)
        if (task.taskStatus() == StatusUtil.Status.RUNNING) {
            val percent = (totalOffset * 100 / totalLength).toInt()
            _notify {
                notifyDefaults = NotificationCompat.DEFAULT_LIGHTS
                notifyPriority = NotificationCompat.PRIORITY_LOW
                _remoteView(task.filename, percent) {
                    setTextViewText(R.id.lib_sub_text_view, "速度:${speed.fileSizeString()}/s")
                }
            }
        }
    }

    /**取消下载的广播*/
    inner class CancelDownloadBroadcastReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent?) {
            _downloadUrl?.let {
                if (intent?.action == "$ACTION_CANCEL_DOWNLOAD$it") {
                    DslDownload.cancel(it)
                }
            }
        }
    }
}

fun String.downloadNotify(action: (DslDownloadNotify.() -> Unit)? = null) {
    dslDownloadNotify(this, action)
}

fun dslDownloadNotify(url: String?, action: (DslDownloadNotify.() -> Unit)? = null) {
    if (url.isNullOrBlank()) {
        L.w("url is null or blank.")
        return
    }
    DslDownloadNotify().apply {
        action?.invoke(this)
        install(url)
    }
}