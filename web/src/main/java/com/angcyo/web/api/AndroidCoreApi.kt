package com.angcyo.web.api

import android.webkit.JavascriptInterface
import androidx.annotation.Keep
import com.angcyo.library._navBarHeight
import com.angcyo.library._screenHeight
import com.angcyo.library._screenWidth
import com.angcyo.library._statusBarHeight
import com.angcyo.library.app
import com.angcyo.library.getAppVersionCode
import com.angcyo.library.getAppVersionName

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/08/12
 */
@Keep
class AndroidCoreApi : IJavascriptInterface {
    override val objName: String = "androidCoreApi"

    @JavascriptInterface
    fun getAppVersionName() = app().getAppVersionName()

    @JavascriptInterface
    fun getAppVersionCode() = app().getAppVersionCode()

    /**
     * ```
     * androidCoreApi.getStatusBarHeight()
     * ```
     * */
    @JavascriptInterface
    fun getStatusBarHeight() = _statusBarHeight

    @JavascriptInterface
    fun getNavBarHeight() = _navBarHeight

    @JavascriptInterface
    fun getScreenWidth() = _screenWidth

    @JavascriptInterface
    fun getScreenHeight() = _screenHeight

}