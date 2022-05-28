package com.angcyo.browser

import android.app.Activity
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import com.angcyo.browser.custom.CustomTabActivityHelper
import com.angcyo.browser.custom.WebviewFallback


/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/05/28
 */
object DslBrowser {
}

/**需要安装支持自定义标签的浏览器, (可能需要设置默认应用)
 * 否则会使用默认的WebView加载*/
fun String.openCustomTab(activity: Activity) {
    val url: String = this
    val customTabsIntent = CustomTabsIntent.Builder().build()
    CustomTabActivityHelper.openCustomTab(
        activity, customTabsIntent, Uri.parse(url), WebviewFallback()
    )
}