package com.angcyo.download.version

import android.content.Intent
import android.os.Bundle
import com.angcyo.activity.BaseAppCompatActivity
import com.angcyo.download.R
import com.angcyo.getData
import com.angcyo.library.getAppVersionName

/**
 * 版本被禁用提示 Activity
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/05/20
 */
open class VersionForbiddenActivity : BaseAppCompatActivity() {

    init {
        activityLayoutId = R.layout.lib_activity_version_forbidden_layout
    }

    var updateBean: VersionUpdateBean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //全屏显示, 否则...窗口只有一点点
        window.setLayout(-1, -1)
    }

    override fun onHandleIntent(intent: Intent, fromNewIntent: Boolean) {
        super.onHandleIntent(intent, fromNewIntent)

        val bean: VersionUpdateBean? = getData()
        updateBean = bean
        if (bean == null) {
            finish()
            return
        }

        _vh.tv(R.id.version_name_view)?.text = "V${getAppVersionName()}"
        _vh.tv(R.id.version_des_view)?.text =
            updateBean?.forbiddenReason ?: "当前版本已禁止使用, 请更新至最新版!"
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0)
    }

    override fun onBackPressed() {
        //super.onBackPressed()
    }

}