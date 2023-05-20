package com.angcyo.download.version

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.angcyo.activity.BaseAppCompatActivity
import com.angcyo.base.dslAHelper
import com.angcyo.download.DslDownload
import com.angcyo.download.R
import com.angcyo.download.cancelDownload
import com.angcyo.download.download
import com.angcyo.download.getTaskStatus
import com.angcyo.download.version.VersionUpdateActivity.Companion.isUpdateIgnore
import com.angcyo.getData
import com.angcyo.library.L
import com.angcyo.library.app
import com.angcyo.library.component.VersionMatcher
import com.angcyo.library.ex.installApk
import com.angcyo.library.ex.isShowDebug
import com.angcyo.library.ex.openUrl
import com.angcyo.library.ex.toMarketDetails
import com.angcyo.library.getAppVersionCode
import com.angcyo.library.utils.Device
import com.angcyo.putData
import com.angcyo.widget.bar
import com.liulishuo.okdownload.StatusUtil
import com.liulishuo.okdownload.core.cause.EndCause
import com.liulishuo.okdownload.core.exception.ServerCanceledException
import java.net.HttpURLConnection

/**
 * 版本更新提示 Activity
 * [com.angcyo.dialog.R.style.LibDialogActivity]
 *
 * [com.angcyo.dialog.activity.DialogActivity]
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/04/08
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */

open class VersionUpdateActivity : BaseAppCompatActivity() {

    companion object {
        /**忽略更新提示, 在显示过一次版本提示后忽略*/
        var isUpdateIgnore = false
    }

    var updateBean: VersionUpdateBean? = null

    init {
        activityLayoutId = R.layout.lib_activity_version_layout
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //全屏显示, 否则...窗口只有一点点
        window.setLayout(-1, -1)
    }

    override fun onDestroy() {
        super.onDestroy()
        updateBean?.let {
            if (!it.versionForce) {
                //非强制更新的版本, 忽略本次内存提示
                isUpdateIgnore = true
            }
        }
    }

    override fun onHandleIntent(intent: Intent, fromNewIntent: Boolean) {
        super.onHandleIntent(intent, fromNewIntent)

        val bean: VersionUpdateBean? = getData()
        updateBean = bean
        if (bean == null) {
            finish()
            return
        }

        _vh.tv(R.id.version_name_view)?.text = "${bean.versionName ?: ""}  " //加2个空格, 防止倾斜之后显示不全
        _vh.tv(R.id.version_des_tip_view)?.text = bean.versionDesTip ?: "更新内容"
        _vh.tv(R.id.version_des_view)?.text = bean.versionDes
        _vh.visible(R.id.lib_cancel_view, !bean.versionForce)
        val versionUrl = bean.versionUrl
        _vh.visible(R.id.lib_button, !versionUrl.isNullOrBlank())
        val startButton = _vh.tv(R.id.lib_button)

        if (bean.link) {
            startButton?.text = "前往下载..."
        } else {
            //no op
        }

        //关闭
        _vh.click(R.id.lib_cancel_view) {
            finish()
        }

        //下载实现
        fun downloadUrl() {
            versionUrl?.download {
                onTaskStart = {
                    startButton?.text = "下载中..."
                }

                onTaskProgress = { _, progress, _ ->
                    _vh.bar(R.id.lib_progress_bar)?.setProgress(progress)
                }

                onTaskFinish = { downloadTask, cause, error ->
                    when (cause) {
                        EndCause.COMPLETED -> {
                            startButton?.text = "立即安装"

                            installApk(app(), downloadTask.file)
                        }

                        EndCause.ERROR -> {
                            if (error is ServerCanceledException &&
                                error.responseCode == HttpURLConnection.HTTP_OK
                            ) {
                                downloadTask.info?.resetInfo()
                            }
                            startButton?.text = "下载失败, 点击重试"
                        }

                        else -> {
                            startButton?.text = "点击重新开始"
                        }
                    }
                }
            }
        }

        //下载
        _vh.click(R.id.lib_button) {
            if (bean.toMarketDetails) {
                it.context.toMarketDetails()//跳转到应用市场
            } else {
                versionUrl ?: return@click
                if (bean.link) {
                    openUrl(versionUrl)
                } else {
                    val taskStatus = versionUrl.getTaskStatus()
                    if (taskStatus == StatusUtil.Status.RUNNING) {
                        //下载中
                        versionUrl.cancelDownload()
                    } else if (taskStatus == StatusUtil.Status.COMPLETED) {
                        //下载已完成
                        val task = DslDownload.findTask(versionUrl)
                        if (task?.file?.exists() == true) {
                            startButton?.text = "立即安装"
                            installApk(app(), task.file)
                        } else {
                            downloadUrl()
                        }
                    } else {
                        downloadUrl()
                    }
                }
            }
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0)
    }

    override fun onBackPressed() {
        if (updateBean?.versionForce == true) {
            return
        }
        super.onBackPressed()
    }
}

/**版本更新界面配置
 * 返回值表示是否有新版本
 * [debug] 调试模式下, 强制弹出界面*/
fun Context.versionUpdate(
    updateBean: VersionUpdateBean?,
    force: Boolean = false,
    debug: Boolean = false
): Boolean {
    if (updateBean == null) {
        return false
    }

    //---禁用检查---

    if (debug ||
        updateBean.forbiddenVersionList?.contains(getAppVersionCode()) == true ||
        VersionMatcher.matches(getAppVersionCode(), updateBean.forbiddenVersionRange, false, true)
    ) {
        //当前版本被禁止使用
        dslAHelper {
            start(VersionForbiddenActivity::class.java) {
                putData(updateBean)
            }
        }
    }

    //---更新检查---

    if (updateBean.debug) {
        if (!isShowDebug()) {
            //非调试模式下, 跳过更新处理
            return false
        }
    }
    if (isShowDebug()) {
        //debug包, 允许取消
        updateBean.versionForce = false
    }
    if (debug) {
        dslAHelper {
            start(VersionUpdateActivity::class.java) {
                putData(updateBean)
            }
        }
        return true
    }
    if (force) {
        isUpdateIgnore = false
    } else if (isUpdateIgnore) {
        return true
    }
    val appVersionCode = getAppVersionCode()
    if (updateBean.versionCode > appVersionCode) {
        //需要更新

        var update: Boolean = if (updateBean.packageList.isNullOrEmpty()) {
            true
        } else {
            updateBean.packageList?.contains(packageName) == true
        }

        if (!updateBean.deviceList.isNullOrEmpty()) {
            update = updateBean.deviceList?.contains(Device.androidId) == true
        }

        if (update) {
            //匹配
            if (updateBean.versionType >= 0 || isShowDebug()) {
                //type匹配
                dslAHelper {
                    start(VersionUpdateActivity::class.java) {
                        putData(updateBean)
                    }
                }
                return true
            }
        }
    } else {
        L.i("不需要版本更新,当前:${appVersionCode} 最新:${updateBean.versionCode}")
    }
    return false
}

/**[com.angcyo.download.version.VersionUpdateActivity.versionUpdate]*/
fun Context.dslVersionUpdate(action: VersionUpdateBean.() -> Unit) {
    val updateBean = VersionUpdateBean().apply(action)
    versionUpdate(updateBean)
}