package com.angcyo.library.component

import android.webkit.CookieSyncManager
import android.webkit.WebSettings
import android.webkit.WebView
import com.angcyo.library.ex.getFileAttachmentName

/**
 *
 * [com.angcyo.tbs.core.inner.TbsWeb]
 * [com.angcyo.library.component.Web]
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/01
 */
object Web {

    /**自定义的UA*/
    var CUSTOM_UA: String? = null

    /**额外的UA标识*/
    var UA_EXTEND = " angcyo"

    /**初始化*/
    fun initWebView(webView: WebView) {
        val webSetting: WebSettings = webView.settings
        initWebSettings(webSetting)

        //
        webSetting.setAppCachePath(webView.context.cacheDir.absolutePath)
        webSetting.databasePath = webView.context.cacheDir.absolutePath
        webSetting.setGeolocationDatabasePath(webView.context.cacheDir.absolutePath)

        //
        CookieSyncManager.createInstance(webView.context)
        CookieSyncManager.getInstance().sync()
    }

    fun initWebSettings(webSetting: WebSettings) {
        webSetting.javaScriptEnabled = true
        webSetting.javaScriptCanOpenWindowsAutomatically = true
        webSetting.allowFileAccess = true
        webSetting.layoutAlgorithm = WebSettings.LayoutAlgorithm.NARROW_COLUMNS
        webSetting.setSupportZoom(true)//支持放大
        webSetting.builtInZoomControls = true//放大控制
        webSetting.displayZoomControls = false//放大控件
        webSetting.useWideViewPort = true //自适应窗口
        webSetting.loadWithOverviewMode = true //this
        webSetting.setSupportMultipleWindows(false)//this
        // webSetting.setLoadWithOverviewMode(true);
        webSetting.setAppCacheEnabled(true)//this
        webSetting.databaseEnabled = true;//this
        webSetting.domStorageEnabled = true
        webSetting.setGeolocationEnabled(true)
        webSetting.setAppCacheMaxSize(Long.MAX_VALUE)
        // webSetting.setPageCacheCapacity(IX5WebSettings.DEFAULT_CACHE_CAPACITY);//this
        webSetting.pluginState = WebSettings.PluginState.ON //ON_DEMAND
        webSetting.setRenderPriority(WebSettings.RenderPriority.HIGH)//this
        webSetting.cacheMode = WebSettings.LOAD_DEFAULT//this
        // this.getSettingsExtension().setPageCacheCapacity(IX5WebSettings.DEFAULT_CACHE_CAPACITY);//extension
        // settings 的设计

        webSetting.defaultTextEncodingName = "utf-8"
        webSetting.mediaPlaybackRequiresUserGesture = true//this

        //UA设置
        webSetting.userAgentString = (CUSTOM_UA ?: webSetting.userAgentString) + UA_EXTEND
    }

    /**
     * attachment;filename="百度手机助手(360手机助手).apk"
     * attachment;filename="baidusearch_AndroidPhone_1021446w.apk"
     */
    fun getFileName(url: String, attachment: String?): String? =
        url.getFileAttachmentName(attachment)
}