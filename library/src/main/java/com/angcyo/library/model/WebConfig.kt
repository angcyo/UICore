package com.angcyo.library.model

import android.net.Uri
import android.os.Parcelable
import androidx.fragment.app.Fragment
import kotlinx.parcelize.Parcelize

/**
 * 启动[TbsWebActivity]附带的参数信息
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/01
 */

@Parcelize
data class WebConfig(
    /**需要打开的网页, 或者本地文件路径, 也可以通过Intent.setData()设置*/
    var uri: Uri? = null,
    /**需要直接加载的数据
     * [com.angcyo.tbs.core.inner.TbsWebView.loadDataWithBaseURL2]*/
    var data: String? = null,
    var mimeType: String? = null, /*当无法从uri中获取mimeType时, 使用此mimeType*/
    /**
     * [com.angcyo.web.WebFragment]
     * [com.angcyo.tbs.core.TbsWebFragment]*/
    var targetClass: Class<out Fragment>? = null,
    /**指定界面的标题*/
    var title: CharSequence? = null,
    /**是否自动显示标题栏左边的关闭icon*/
    var showCloseButton: Boolean = true,
    /**是否需要显示标题栏右边的菜单icon*/
    var showRightMenu: Boolean = true,
    /**是否需要loading提示, 包括顶部和中间*/
    var showLoading: Boolean = true,
    var enableTitleBarHideBehavior: Boolean = true,
) : Parcelable {
    companion object {
        const val KEY_CONFIG = "key_config"

        const val DEBUG_TBS_URL = "https://debugtbs.qq.com"
    }
}