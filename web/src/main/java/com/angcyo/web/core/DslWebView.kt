package com.angcyo.web.core

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.net.Uri
import android.util.AttributeSet
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.webkit.WebResourceErrorCompat
import com.angcyo.library.L
import com.angcyo.library.component.Web
import com.angcyo.library.component.appBean
import com.angcyo.library.component.dslIntentQuery
import com.angcyo.library.ex.decode
import com.angcyo.library.ex.fileSizeString
import com.angcyo.library.ex.nowTimeString
import com.angcyo.library.model.AppBean
import com.angcyo.library.model.FileChooserParam
import com.angcyo.library.utils.LogFile
import com.angcyo.library.utils.toLogFilePath
import com.angcyo.library.utils.writeTo
import com.hjhrq1991.library.BridgeWebView
import com.hjhrq1991.library.BridgeWebViewClientListener
import com.hjhrq1991.library.OnWebChromeClientListener

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/06/08
 */
open class DslWebView(context: Context, attributeSet: AttributeSet? = null) :
    BridgeWebView(context, attributeSet) {

    //<editor-fold desc="回调">

    /**网页加载进度回调,
     * 等于0时, 表示页面开始加载
     * 等于100时, 表示页面完成加载
     * */
    var progressChangedAction: (url: String?, progress: Int) -> Unit = { _, _ ->

    }

    /**标题接收回调*/
    var receivedTitleAction: (title: String?) -> Unit = {}

    /**接收到的标题*/
    var receivedTitle: String? = null

    /**打开应用回调*/
    var openAppAction: (url: String, activityInfo: ActivityInfo, appBean: AppBean) -> Unit =
        { url, activityInfo, appBean -> L.d("打开应用:${appBean.appName} ${activityInfo.name}") }

    /**下载文件回调*/
    var downloadAction: (
        url: String /*下载地址*/,
        userAgent: String,
        contentDisposition: String,
        mime: String /*文件mime application/zip*/,
        length: Long /*文件大小 b*/
    ) -> Unit =
        { url, userAgent, contentDisposition, mime, length ->
            L.d(
                buildString {
                    append("下载:")
                    append(Web.getFileName(url, contentDisposition))
                    appendLine(length.fileSizeString())
                    appendLine("$url $mime")
                    append("$userAgent $contentDisposition ")
                }
            )
        }

    /**选择文件回调, 选择文件后请务必[onReceiveValue]方法*/
    var fileChooseAction: (param: FileChooserParam) -> Unit = {}

    /**请求拦截回调*/
    var shouldInterceptRequestAction: ((
        view: WebView,
        request: WebResourceRequest?,
    ) -> WebResourceResponse?)? = null

    /**加载[url], 需要设置的请求头*/
    var headerAction: (url: String) -> Map<String, String>? = {
        null
    }

    /**是否需要拦截[url]的加载*/
    var shouldOverrideUrlLoadAction: (
        webView: WebView,
        url: String?
    ) -> Boolean = { webView, url ->
        false
    }

    //</editor-fold desc="回调">

    var _loadUrl: String? = null

    init {
        Web.initWebView(this)

        setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
            downloadAction(url.decode(), userAgent, contentDisposition, mimetype, contentLength)
        }
    }

    var _filePathCallback: ValueCallback<Array<Uri?>>? = null

    override fun init() {
        super.init()

        //1:
        setWebChromeClientListener(object : OnWebChromeClientListener {
            override fun onReceivedTitle(view: WebView, title: String?) {
                receivedTitleAction.invoke(title)
            }

            override fun onShowFileChooser(
                webView: WebView,
                filePathCallback: ValueCallback<Array<Uri?>>,
                fileChooserParams: WebChromeClient.FileChooserParams
            ): Boolean {
                _filePathCallback = filePathCallback
                fileChooseAction(FileChooserParam(1, fileChooserParams.acceptTypes.getOrNull(0)))
                //return super.onShowFileChooser(webView, filePathCallback, fileChooserParams)
                return true
            }

            override fun onConsoleMessage(message: String?, lineNumber: Int, sourceID: String?) {
                super.onConsoleMessage(message, lineNumber, sourceID)
                appendWebLog("${sourceID}:${lineNumber}->${message}", L.INFO)
            }
        })

        //2:
        bridgeWebViewClient?.setBridgeWebViewClientListener(object : BridgeWebViewClientListener {

            //加载资源, css js ttf html等
            override fun onLoadResource(view: WebView, url: String?) {
                appendWebLog("load:$url")
            }

            override fun onReceivedError(
                view: WebView,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
            }

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest?,
                error: WebResourceErrorCompat?
            ): Boolean {
                appendWebLog("异常[${request?.method}]:${request?.url}->${error?.errorCode}:${error?.description}")
                return super.onReceivedError(view, request, error)
            }

            override fun onReceivedHttpError(
                view: WebView,
                request: WebResourceRequest?,
                errorResponse: WebResourceResponse?
            ) {
                super.onReceivedHttpError(view, request, errorResponse)
                appendWebLog("http异常:${errorResponse?.statusCode}:${errorResponse?.mimeType}:[${request?.method}]:${request?.url}")
            }

            override fun shouldInterceptRequest(view: WebView, url: String?): WebResourceResponse? {
                return super.shouldInterceptRequest(view, url)
            }

            //1: 拦截请求, 所有请求都会通过这里. 比如css js等
            override fun shouldInterceptRequest(
                view: WebView,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                appendWebLog("请求[${request?.method}]:${request?.url}")
                return shouldInterceptRequestAction?.invoke(view, request)
                    ?: super.shouldInterceptRequest(view, request)
            }

            override fun shouldOverrideUrlLoading(view: WebView, url: String?): Boolean {
                val urlLog =
                    "加载:${url?.decode()}\no:${view.originalUrl?.decode()}\nu:${view.url?.decode()}\ntitle:${view.title}"
                L.d(urlLog)

                appendWebLog(urlLog)
                return onShouldOverrideUrlLoading(view, url)
            }

            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest?
            ): Boolean {
                L.d("${view.title} q:$request")
                return super.shouldOverrideUrlLoading(view, request)
            }

            //开始加载页面
            override fun onPageStarted(view: WebView, url: String?, bitmap: Bitmap?) {
                super.onPageStarted(view, url, bitmap)
                receivedTitle = view.title
                progressChangedAction(url, 0)
                appendWebLog("开始加载页面:[${view.title}]:$url\n")
            }

            //页面加载完成
            override fun onPageFinished(view: WebView, url: String?) {
                super.onPageFinished(view, url)
                receivedTitle = view.title
                progressChangedAction(url, 100)
                appendWebLog("完成加载页面:[${view.title}]:$url\n")
            }
        })
    }

    /**加载url*/
    open fun onShouldOverrideUrlLoading(view: WebView, url: String?): Boolean {
        if (shouldOverrideUrlLoadAction(view, url)) {
            return true
        }

        url?.run {
            if (startsWith("http")) {
                //additionalHttpHeaders
                view.loadUrl(url, headerAction(url) ?: emptyMap())
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

    /**选择文件后, 调用此方法, 通知给web*/
    fun onReceiveValue(files: Array<Uri?>?) {
        _filePathCallback?.onReceiveValue(files)
        _filePathCallback = null
    }

    /**加载网页*/
    fun loadUrl2(url: String?) {
        if (url?.startsWith("http") == true) {
            _loadUrl = url
        }
        if (url.isNullOrEmpty()) {
            loadUrl("about:block")
        } else {
            loadUrl(url, headerAction(url) ?: emptyMap())
        }
    }

    override fun loadUrl(url: String) {
        super.loadUrl(url)
    }

    override fun loadUrl(url: String, additionalHttpHeaders: Map<String, String>) {
        super.loadUrl(url, additionalHttpHeaders)
    }

    override fun loadData(data: String, mimeType: String?, encoding: String?) {
        super.loadData(data, mimeType, encoding)
    }

    open fun loadData2(data: String, mimeType: String? = "text/html", encoding: String? = "utf-8") {
        loadData(data, mimeType, encoding)
    }

    override fun loadDataWithBaseURL(
        baseUrl: String?,
        data: String,
        mimeType: String?,
        encoding: String?,
        historyUrl: String?
    ) {
        super.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl)
    }

    open fun loadDataWithBaseURL2(
        data: String?,
        mimeType: String = "text/html",
        encoding: String = "utf-8",
        baseUrl: String? = null,
        historyUrl: String? = null
    ) {
        data ?: return
        loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl)
    }

    /**写入web log*/
    open fun appendWebLog(log: String, level: Int = L.DEBUG) {
        "${nowTimeString()} $log \n".writeTo(LogFile.webview.toLogFilePath(), true)
        L.log(level, log)
    }

}