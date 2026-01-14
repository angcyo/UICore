package com.angcyo.web.core

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.net.Uri
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.core.view.NestedScrollingChildHelper
import androidx.core.view.ViewCompat
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
import kotlin.math.max
import kotlin.math.min

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/06/08
 *
 * # 注册一个 Java 处理函数，以便 JavaScript 可以调用它。
 *
 * ```
 *  webView.registerHandler("submitFromWeb", new BridgeHandler() {
 *      @Override
 *      public void handler(String data, CallBackFunction function) {
 *          Log.i(TAG, "handler = submitFromWeb, data from web = " + data);
 *          function.onCallBack("submitFromWeb exe, response data from Java");
 *      }
 *  });
 *
 *
 *  WebViewJavascriptBridge.callHandler(
 *      'submitFromWeb'
 *      , {'param': str1}
 *      , function(responseData) {
 *          document.getElementById("show").innerHTML = "send get responseData from java, data = " + responseData
 *      }
 *  );
 *
 * ```
 *
 * # 注册一个 JavaScript 处理函数，以便 Java 可以调用它。
 *
 * ```
 *
 * WebViewJavascriptBridge.registerHandler("functionInJs", function(data, responseCallback) {
 *     document.getElementById("show").innerHTML = ("data from Java: = " + data);
 *     var responseData = "Javascript Says Right back aka!";
 *     responseCallback(responseData);
 * });
 *
 *
 *  webView.callHandler("functionInJs", new Gson().toJson(user), new CallBackFunction() {
 *      @Override
 *      public void onCallBack(String data) {
 *
 *      }
 *  });
 *
 *
 * ```
 *
 * https://github.com/uknownothingsnow/JsBridge
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

    val childHelper: NestedScrollingChildHelper by lazy {
        NestedScrollingChildHelper(this)
    }

    init {
        Web.initWebView(this)

        setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
            downloadAction(url.decode(), userAgent, contentDisposition, mimetype, contentLength)
        }

        try {
            isNestedScrollingEnabled = true
            overScrollMode = View.OVER_SCROLL_NEVER
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    var _filePathCallback: ValueCallback<Array<Uri?>>? = null

    override fun init() {
        super.init()

        //1:WebChromeClient
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
                fileChooseAction(
                    FileChooserParam(
                        fileChooserParams.mode == WebChromeClient.FileChooserParams.MODE_OPEN_MULTIPLE,
                        fileChooserParams.acceptTypes.getOrNull(0)
                    )
                )
                //return super.onShowFileChooser(webView, filePathCallback, fileChooserParams)
                return true
            }

            override fun onConsoleMessage(message: String?, lineNumber: Int, sourceID: String?) {
                super.onConsoleMessage(message, lineNumber, sourceID)
                appendWebLog("${sourceID}:${lineNumber}->${message}", L.INFO)
            }
        })

        //2:WebViewClient
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

    //region ---手势---

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        return super.dispatchTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        wrapNestedScrollTouchEvent(event) {
            super.onTouchEvent(it)
        }
        return true
    }

    private var _touchDownX = 0f
    private var _touchDownY = 0f

    private var _touchMoveX = 0f
    private var _touchMoveY = 0f

    /**滚动被消耗的x/y总和*/
    private var _scrollConsumedX = 0f
    private var _scrollConsumedY = 0f

    /**当前手势还有多少没有被消耗*/
    private var _scrollUnconsumedX = 0
    private var _scrollUnconsumedY = 0

    private val scrollConsumed = IntArray(2)
    private val offsetInWindow: IntArray? = null

    /**包裹内嵌滚动事件*/
    fun wrapNestedScrollTouchEvent(
        event: MotionEvent,
        action: (offsetEvent: MotionEvent) -> Unit
    ) {
        if (!isNestedScrollingEnabled) {
            action(event)
            return
        }
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                _touchDownX = event.x
                _touchDownY = event.y
                _scrollConsumedX = 0f
                _scrollConsumedY = 0f

                parent?.requestDisallowInterceptTouchEvent(true)
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL)
                action(event)
            }

            MotionEvent.ACTION_MOVE -> {
                _touchMoveX = event.x
                _touchMoveY = event.y

                val dx = (_touchDownX - _touchMoveX).toInt()
                val dy = (_touchDownY - _touchMoveY).toInt()

                //内嵌滚动之前
                scrollConsumed.fill(0)
                val consumed = dispatchNestedPreScroll(dx, dy, scrollConsumed, offsetInWindow)

                //被消耗的距离
                var consumedX = scrollConsumed[0]
                consumedX = if (dx > 0) {
                    min(consumedX, dx)
                } else {
                    max(consumedX, dx)
                }

                var consumedY = scrollConsumed[1]
                consumedY = if (dy > 0) {
                    min(consumedY, dy)
                } else {
                    max(consumedY, dy)
                }

                //累加被消耗的距离
                _scrollConsumedX += consumedX
                _scrollConsumedY += consumedY

                _scrollUnconsumedX = dx - consumedX
                _scrollUnconsumedY = dy - consumedY

                //view自己滚动
                val offsetEvent = MotionEvent.obtain(event)
                offsetEvent.offsetLocation(0f, -_scrollConsumedY)
                action(event)
                offsetEvent.recycle()
            }

            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                parent?.requestDisallowInterceptTouchEvent(false)
                stopNestedScroll()
                action(event)
            }
        }
    }

    override fun canScrollVertically(direction: Int): Boolean {//-1 顶部 1 底部 可滚动
        //getContentWidth() //no api?

        val scrollY = scrollY

        return if (direction > 0) {
            //底部是否可以滚动
            val webViewHeight = height
            val contentHeight = contentHeight
            contentHeight > scrollY + webViewHeight
        } else if (direction < 0) {
            //顶部是否可以滚动
            scrollY > 0
        } else {
            super.canScrollVertically(direction)
        }
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
    }

    //当前滚动需要的距离
    private var _targetDeltaX = 0
    private var _targetDeltaY = 0

    private var _currentScrollX = 0
    private var _currentScrollY = 0

    override fun overScrollBy(
        deltaX: Int,
        deltaY: Int,
        scrollX: Int,
        scrollY: Int,
        scrollRangeX: Int,
        scrollRangeY: Int,
        maxOverScrollX: Int,
        maxOverScrollY: Int,
        isTouchEvent: Boolean
    ): Boolean {
        _targetDeltaX = deltaX
        _targetDeltaY = deltaY

        _currentScrollX = scrollX
        _currentScrollY = scrollY
        return super.overScrollBy(
            deltaX,
            deltaY,
            scrollX,
            scrollY,
            scrollRangeX,
            scrollRangeY,
            maxOverScrollX,
            maxOverScrollY,
            isTouchEvent
        )
    }

    override fun onOverScrolled(scrollX: Int, scrollY: Int, clampedX: Boolean, clampedY: Boolean) {
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY)
        if (clampedY) {
            //滚动到顶部/底部
        }

        //消耗的滚动距离
        val consumedX = getScrollX() - _currentScrollX
        val consumedY = getScrollY() - _currentScrollY

        //未消耗的滚动距离
        //val unConsumedX = _targetDeltaX - consumedX
        //val unConsumedY = _targetDeltaY - consumedY

        val unConsumedX = _scrollUnconsumedX - consumedX
        val unConsumedY = _scrollUnconsumedY - consumedY

        val dispatch =
            dispatchNestedScroll(consumedX, consumedY, unConsumedX, unConsumedY, offsetInWindow)
        if (dispatch) {
            _scrollConsumedX += unConsumedX
            _scrollConsumedY += unConsumedY
        }
    }

    //endregion ---手势---
}