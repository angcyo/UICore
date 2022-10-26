package com.angcyo.widget

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.net.Uri
import android.util.AttributeSet
import android.view.View
import android.webkit.*
import com.angcyo.library.L
import com.angcyo.library.component.Web
import com.angcyo.library.component.appBean
import com.angcyo.library.component.dslIntentQuery
import com.angcyo.library.ex.decode
import com.angcyo.library.ex.loadUrl
import com.angcyo.library.ex.nowTimeString
import com.angcyo.library.model.AppBean
import com.angcyo.library.utils.LogFile
import com.angcyo.library.utils.toLogFilePath
import com.angcyo.library.utils.writeTo

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/10/08
 */
open class WrapWebView(context: Context, attributeSet: AttributeSet? = null) :
    WebView(context, attributeSet) {

    //<editor-fold desc="WebViewClient">

    val webClient: WebViewClient = object : WebViewClient() {
        override fun onLoadResource(view: WebView, url: String?) {
            super.onLoadResource(view, url)
        }

        override fun onReceivedError(
            view: WebView,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
            super.onReceivedError(view, request, error)
        }

        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            return super.shouldOverrideUrlLoading(view, url)
        }

        override fun shouldOverrideUrlLoading(
            webView: WebView,
            request: WebResourceRequest
        ): Boolean {
            val urlLog =
                "加载:${request.url}\no:${webView.originalUrl?.decode()}\nu:${webView.url?.decode()}\ntitle:${webView.title}"
            L.d(urlLog)
            return onShouldOverrideUrlLoading(this, webView, request.url.loadUrl())
        }

        //开始加载页面
        override fun onPageStarted(webView: WebView, url: String?, bitmap: Bitmap?) {
            super.onPageStarted(webView, url, bitmap)
            progressChangedAction(url, 0)
            appendWebLog("开始加载页面:[${webView.title}]:$url\n")
        }

        //页面加载完成
        override fun onPageFinished(webView: WebView, url: String?) {
            super.onPageFinished(webView, url)
            progressChangedAction(url, 100)
            appendWebLog("完成加载页面:[${webView.title}]:$url\n")
        }
    }

    /**网页加载进度回调,
     * 等于0时, 表示页面开始加载
     * 等于100时, 表示页面完成加载
     * */
    var progressChangedAction: (url: String?, progress: Int) -> Unit = { _, _ ->

    }

    /**是否需要拦截[url]的加载*/
    var shouldOverrideUrlLoadAction: (
        webClient: WebViewClient,
        webView: WebView,
        url: String?
    ) -> Boolean = { webClient, webView, url ->
        false
    }

    /**加载[url], 需要设置的请求头*/
    var headerAction: (url: String) -> Map<String, String>? = {
        null
    }

    /**打开应用回调*/
    var openAppAction: (url: String, activityInfo: ActivityInfo, appBean: AppBean) -> Unit =
        { url, activityInfo, appBean -> L.d("打开应用:${appBean.appName} ${activityInfo.name}") }

    //</editor-fold desc="WebViewClient">

    //<editor-fold desc="WebChromeClient">

    val chromeClient: WebChromeClient = object : WebChromeClient() {

        //<editor-fold desc="基础回调">

        //接收页面标题,在[onPageStarted]之后
        override fun onReceivedTitle(webView: WebView?, title: String?) {
            super.onReceivedTitle(webView, title)
            receivedTitle = title
            L.d("${webView?.originalUrl} ${webView?.url} $title")
            this@WrapWebView.receivedTitleAction(title)
        }

        override fun onProgressChanged(webView: WebView?, progress: Int) {
            super.onProgressChanged(webView, progress)
            //L.d("${webView.originalUrl} ${webView.url} $progress")
            progressChangedAction(webView?.url, progress)
        }

        //</editor-fold desc="基础回调">

        //<editor-fold desc="全屏播放视频">

        var viewCallback: CustomViewCallback? = null

        override fun onShowCustomView(
            view: View,
            viewCallback: CustomViewCallback
        ) {
            super.onShowCustomView(view, viewCallback)
            // 此处的 view 就是全屏的视频播放界面，需要把它添加到我们的界面上
            this.viewCallback = viewCallback

            L.i(view, " ", viewCallback)
        }

        override fun onShowCustomView(
            view: View,
            requestedOrientation: Int,
            viewCallback: CustomViewCallback
        ) {
            super.onShowCustomView(view, requestedOrientation, viewCallback)
            onShowCustomView(view, viewCallback)
        }

        override fun onHideCustomView() {
            super.onHideCustomView()
            // 退出全屏播放，我们要把之前添加到界面上的视频播放界面移除
            viewCallback?.onCustomViewHidden()

            L.i(viewCallback)
        }

        //</editor-fold desc="全屏播放视频">

        //<editor-fold desc="其他">

        override fun onPermissionRequest(permissionRequest: PermissionRequest?) {
            super.onPermissionRequest(permissionRequest)
            L.i(permissionRequest)
            appendWebLog("权限:onPermissionRequest:${permissionRequest?.origin}:${permissionRequest?.resources}")
        }

        override fun onPermissionRequestCanceled(permissionRequest: PermissionRequest?) {
            super.onPermissionRequestCanceled(permissionRequest)
            L.i(permissionRequest)
            appendWebLog("权限:onPermissionRequestCanceled:${permissionRequest?.origin}:${permissionRequest?.resources}")
        }

        override fun onGeolocationPermissionsHidePrompt() {
            super.onGeolocationPermissionsHidePrompt()
            L.i()
            appendWebLog("权限:onGeolocationPermissionsHidePrompt")
        }

        override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
            L.d("${consoleMessage.sourceId()}#${consoleMessage.lineNumber()}:${consoleMessage.message()}")
            return super.onConsoleMessage(consoleMessage)
        }

        //</editor-fold desc="其他">
    }

    /**接收到的标题*/
    var receivedTitle: String? = null

    /**标题接收回调*/
    var receivedTitleAction: (title: String?) -> Unit = {}

    //</editor-fold desc="WebChromeClient">

    init {
        webViewClient = webClient
        webChromeClient = chromeClient

        Web.initWebView(this)
    }

    /**写入web log*/
    open fun appendWebLog(log: String) {
        "${nowTimeString()} $log \n".writeTo(LogFile.webview.toLogFilePath(), true)
    }

    /**加载url*/
    open fun onShouldOverrideUrlLoading(
        webClient: WebViewClient,
        webView: WebView,
        url: String?
    ): Boolean {

        if (shouldOverrideUrlLoadAction(webClient, webView, url)) {
            return true
        }

        url?.run {
            if (startsWith("http")) {
                //additionalHttpHeaders
                webView.loadUrl(url, headerAction(url) ?: emptyMap())
            } else {
                //查询是否是app intent
                dslIntentQuery {
                    queryData = Uri.parse(url)
                    queryCategory = listOf(Intent.CATEGORY_BROWSABLE)
                }.apply {
                    if (isNotEmpty()) {
                        //找到了
                        first().activityInfo.run {
                            openAppAction(url.decode(), this, packageName.appBean()!!)
                        }
                    }
                }
            }
        }

        return true
    }

}