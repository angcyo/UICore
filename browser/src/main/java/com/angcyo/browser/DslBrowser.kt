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
fun String.openCustomTab(activity: Activity, block: CustomTabsIntent.Builder.() -> Unit = {}) {
    val url: String = this
    val customTabsIntent = CustomTabsIntent.Builder().apply {
        //自定义ui

        /*val defaultColors = CustomTabColorSchemeParams.Builder()
            .setToolbarColor(_color(R.color.colorAccent))
            .setSecondaryToolbarColor(_color(R.color.colorPrimaryDark))
            .build()
        setDefaultColorSchemeParams(defaultColors)*/

        //setActionButton(icon, actionLabel, pendingIntent)
        //addMenuItem(menuItemTitle, menuItemPendingIntent)

        //setShowTitle(mShowTitleCheckBox.isChecked())
        //setUrlBarHidingEnabled(mAutoHideAppBarCheckbox.isChecked())

        //setCloseButtonIcon(toBitmap(getDrawable(R.drawable.ic_arrow_back)))

        //setStartAnimations(activity, android.R.anim.slide_in_right, android.R.anim.slide_out_left)
        //setExitAnimations(activity, android.R.anim.slide_in_left, android.R.anim.slide_out_right)

        block()
    }.build()
    CustomTabActivityHelper.openCustomTab(
        activity,
        customTabsIntent,
        Uri.parse(url),
        WebviewFallback()
    )
}