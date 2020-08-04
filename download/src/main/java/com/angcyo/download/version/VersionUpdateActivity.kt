package com.angcyo.download.version

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.angcyo.activity.BaseAppCompatActivity
import com.angcyo.base.dslAHelper
import com.angcyo.download.R
import com.angcyo.download.download
import com.angcyo.getData
import com.angcyo.library.L
import com.angcyo.library.app
import com.angcyo.library.ex.installApk
import com.angcyo.library.ex.isDebug
import com.angcyo.library.getAppVersionCode
import com.angcyo.putData
import com.angcyo.widget.bar
import com.liulishuo.okdownload.core.cause.EndCause

/**
 * 版本更新提示Activity
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/04/08
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */

open class VersionUpdateActivity : BaseAppCompatActivity() {

    var updateBean: VersionUpdateBean? = null

    init {
        activityLayoutId = R.layout.lib_activity_version_layout
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setLayout(-1, -2)
    }

    override fun onHandleIntent(intent: Intent, fromNew: Boolean) {
        super.onHandleIntent(intent, fromNew)

        val bean: VersionUpdateBean? = getData()
        updateBean = bean
        if (bean == null) {
            finish()
            return
        }

        _vh.tv(R.id.version_name_view)?.text = bean.versionName
        _vh.tv(R.id.version_des_view)?.text = bean.versionDes
        _vh.visible(R.id.lib_cancel_view, !bean.versionForce)

        //关闭
        _vh.click(R.id.lib_cancel_view) {
            finish()
        }

        //下载
        _vh.click(R.id.lib_button) {

            bean.versionUrl?.download {
                onTaskStart = {
                    _vh.tv(R.id.lib_button)?.text = "开始下载..."
                }

                onTaskProgress = { _, progress, _ ->
                    _vh.bar(R.id.lib_progress_bar)?.setProgress(progress)
                }

                onTaskFinish = { downloadTask, cause, _ ->
                    if (cause == EndCause.COMPLETED) {
                        _vh.tv(R.id.lib_button)?.text = "立即安装"

                        installApk(app(), downloadTask.file)
                    } else {
                        _vh.tv(R.id.lib_button)?.text = "下载失败, 点击重试"
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

/**版本更新界面配置*/
fun Context.versionUpdate(updateBean: VersionUpdateBean?) {
    if (updateBean == null) {
        return
    }
    val appVersionCode = getAppVersionCode()
    if (updateBean.versionCode > appVersionCode) {
        //需要更新
        if (updateBean.versionType >= 0 ||
            (updateBean.versionType < 0 && isDebug())
        ) {
            //type匹配
            dslAHelper {
                start(VersionUpdateActivity::class.java) {
                    putData(updateBean)
                }
            }
        }
    } else {
        L.i("不需要版本更新,当前:${appVersionCode} 最新:${updateBean.versionCode}")
    }
}

fun Context.dslVersionUpdate(action: VersionUpdateBean.() -> Unit) {
    val updateBean = VersionUpdateBean().apply(action)
    versionUpdate(updateBean)
}