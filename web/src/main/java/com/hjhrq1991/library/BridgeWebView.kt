package com.hjhrq1991.library

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Looper
import android.os.SystemClock
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.webkit.ConsoleMessage
import android.webkit.GeolocationPermissions
import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebStorage
import android.webkit.WebView
import com.hjhrq1991.library.BridgeUtil.getDataFromReturnUrl
import com.hjhrq1991.library.BridgeUtil.getFunctionFromReturnUrl
import com.hjhrq1991.library.BridgeUtil.parseFunctionName
import com.hjhrq1991.library.Message.Companion.toArrayList

/**
 * https://github.com/uknownothingsnow/JsBridge
 * */
@SuppressLint("SetJavaScriptEnabled")
open class BridgeWebView : WebView, WebViewJavascriptBridge {
    var responseCallbacks: MutableMap<String, CallBackFunction> = hashMapOf()
    var messageHandlers: MutableMap<String, BridgeHandler> = hashMapOf()

    /**
     * @param handler default handler,handle messages send by js without assigned handler name,
     * if js message has handler name, it will be handled by named handlers registered by native
     */
    var defaultHandler: BridgeHandler = DefaultHandler()

    var startupMessage: MutableList<Message>? = mutableListOf()

    protected var bridgeWebViewClient: BridgeWebViewClient? = null
    protected var uniqueId: Long = 0

    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context!!,
        attrs,
        defStyle
    ) {
        init()
    }

    constructor(context: Context?) : super(context!!) {
        init()
    }

    protected open fun init() {
        this.isVerticalScrollBarEnabled = false
        this.isHorizontalScrollBarEnabled = false
        this.settings.javaScriptEnabled = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setWebContentsDebuggingEnabled(true)
        }
        this.webViewClient = generateBridgeWebViewClient()
    }

    protected fun generateBridgeWebViewClient(): BridgeWebViewClient {
        return BridgeWebViewClient(this).also { bridgeWebViewClient = it }
    }

    fun handlerReturnData(url: String?) {
        val functionName = getFunctionFromReturnUrl(url!!)
        val f = responseCallbacks[functionName]
        val data = getDataFromReturnUrl(url)
        if (f != null) {
            f.onCallBack(data)
            responseCallbacks.remove(functionName)
            return
        }
    }

    override fun send(data: String?) {
        send(data, null)
    }

    override fun send(data: String?, responseCallback: CallBackFunction?) {
        doSend(null, data, responseCallback)
    }

    private fun doSend(handlerName: String?, data: String?, responseCallback: CallBackFunction?) {
        val m = Message()
        if (!TextUtils.isEmpty(data)) {
            m.data = data
        }
        if (responseCallback != null) {
            val callbackStr = String.format(
                BridgeUtil.CALLBACK_ID_FORMAT,
                (++uniqueId).toString() + (BridgeUtil.UNDERLINE_STR + SystemClock.currentThreadTimeMillis())
            )
            responseCallbacks[callbackStr] = responseCallback
            m.callbackId = callbackStr
        }
        if (!TextUtils.isEmpty(handlerName)) {
            m.handlerName = handlerName
        }
        queueMessage(m)
    }

    private fun queueMessage(m: Message) {
        if (startupMessage != null) {
            startupMessage!!.add(m)
        } else {
            dispatchMessage(m)
        }
    }

    fun dispatchMessage(m: Message) {
        var messageJson = m.toJson()
        //escape special characters for json string
        messageJson = messageJson!!.replace("(\\\\)([^utrn])".toRegex(), "\\\\\\\\$1$2")
        messageJson = messageJson.replace("(?<=[^\\\\])(\")".toRegex(), "\\\\\"")
        val javascriptCommand = String.format(
            BridgeUtil.JS_HANDLE_MESSAGE_FROM_JAVA.replace(
                BridgeConfig.defaultJs,
                BridgeConfig.customJs
            ), messageJson
        )
        if (Thread.currentThread() === Looper.getMainLooper().thread) {
            this.loadUrl(javascriptCommand)
        }
    }

    fun flushMessageQueue() {
        if (Thread.currentThread() === Looper.getMainLooper().thread) {
            loadUrl(
                BridgeUtil.JS_FETCH_QUEUE_FROM_JAVA.replace(
                    BridgeConfig.defaultJs,
                    BridgeConfig.customJs
                ), object : CallBackFunction {
                    override fun onCallBack(data: String?) {
                        // deserializeMessage
                        var list: List<Message>? = null
                        list = try {
                            toArrayList(data)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            return
                        }
                        if (list.isNullOrEmpty()) {
                            return
                        }
                        for (i in list.indices) {
                            val m: Message = list[i]
                            val responseId = m.responseId
                            // 是否是response
                            if (!TextUtils.isEmpty(responseId)) {
                                val function = responseCallbacks[responseId]
                                val responseData = m.responseData
                                function!!.onCallBack(responseData)
                                responseCallbacks.remove(responseId)
                            } else {
                                var responseFunction: CallBackFunction? = null
                                // if had callbackId
                                val callbackId = m.callbackId
                                if (!TextUtils.isEmpty(callbackId)) {
                                    responseFunction = object : CallBackFunction {
                                        override fun onCallBack(data: String?) {
                                            val responseMsg = Message()
                                            responseMsg.responseId = callbackId
                                            responseMsg.responseData = data
                                            queueMessage(responseMsg)
                                        }
                                    }
                                } else {
                                    responseFunction = object : CallBackFunction {
                                        override fun onCallBack(data: String?) {
                                            // do nothing
                                        }
                                    }
                                }
                                (if (!TextUtils.isEmpty(m.handlerName)) {
                                    messageHandlers[m.handlerName]
                                } else {
                                    defaultHandler
                                })?.handler(m.data, responseFunction)
                            }
                        }
                    }
                })
        }
    }

    fun loadUrl(jsUrl: String?, returnCallback: CallBackFunction) {
        jsUrl ?: return
        this.loadUrl(jsUrl)
        responseCallbacks[parseFunctionName(
            jsUrl,
            BridgeConfig.customJs
        )] = returnCallback
    }

    /**
     * register handler,so that javascript can call it
     *
     * @param handlerName handlerName
     * @param handler     Handler
     */
    fun registerHandler(handlerName: String, handler: BridgeHandler?) {
        if (handler != null) {
            messageHandlers[handlerName] = handler
        }
    }

    /**
     * call javascript registered handler
     *
     * @param handlerName handlerName
     * @param data        data
     * @param callBack    callBack
     */
    fun callHandler(handlerName: String?, data: String?, callBack: CallBackFunction?) {
        doSend(handlerName, data, callBack)
    }

    fun setBridgeWebViewClientListener(bridgeWebViewClientListener: BridgeWebViewClientListener?) {
        bridgeWebViewClient!!.setBridgeWebViewClientListener(bridgeWebViewClientListener)
    }
    /**
     * 销毁时调用，移除listener
     */
    /**
     * 销毁时调用，移除listener
     */
    fun removeListener() {
        if (bridgeWebViewClient != null) {
            bridgeWebViewClient!!.removeListener()
        }
        if (onWebChromeClientListener != null) {
            onWebChromeClientListener = null
        }
    }

    private var onWebChromeClientListener: OnWebChromeClientListener? = null

    fun setWebChromeClientListener(webChromeClientListener: OnWebChromeClientListener?) {
        onWebChromeClientListener = webChromeClientListener
        webChromeClient = newWebChromeClient()
    }

    private fun newWebChromeClient(): WebChromeClient {
        return object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                onWebChromeClientListener?.onProgressChanged(view, newProgress)
                    ?: super.onProgressChanged(view, newProgress)
            }

            override fun onReceivedTitle(view: WebView, title: String) {
                onWebChromeClientListener?.onReceivedTitle(view, title) ?: super.onReceivedTitle(
                    view,
                    title
                )
            }

            override fun onReceivedIcon(view: WebView, icon: Bitmap) {
                onWebChromeClientListener?.onReceivedIcon(view, icon) ?: super.onReceivedIcon(
                    view,
                    icon
                )
            }

            override fun onReceivedTouchIconUrl(view: WebView, url: String, precomposed: Boolean) {
                onWebChromeClientListener?.onReceivedTouchIconUrl(view, url, precomposed)
                    ?: super.onReceivedTouchIconUrl(view, url, precomposed)
            }

            override fun onShowCustomView(view: View, callback: CustomViewCallback) {
                onWebChromeClientListener?.onShowCustomView(view, callback)
                    ?: super.onShowCustomView(view, callback)
            }

            override fun onShowCustomView(
                view: View,
                requestedOrientation: Int,
                callback: CustomViewCallback
            ) {
                onWebChromeClientListener?.onShowCustomView(
                    view,
                    requestedOrientation,
                    callback
                ) ?: super.onShowCustomView(view, requestedOrientation, callback)
            }

            override fun onHideCustomView() {
                onWebChromeClientListener?.onHideCustomView() ?: super.onHideCustomView()
            }

            override fun onCreateWindow(
                view: WebView,
                isDialog: Boolean,
                isUserGesture: Boolean,
                resultMsg: android.os.Message
            ): Boolean {
                return onWebChromeClientListener?.onCreateWindow(
                    view,
                    isDialog,
                    isUserGesture,
                    resultMsg
                ) ?: super.onCreateWindow(view, isDialog, isUserGesture, resultMsg)
            }

            override fun onRequestFocus(view: WebView) {
                onWebChromeClientListener?.onRequestFocus(view) ?: super.onRequestFocus(view)
            }

            override fun onCloseWindow(window: WebView) {
                onWebChromeClientListener?.onCloseWindow(window) ?: super.onCloseWindow(window)
            }

            override fun onJsAlert(
                view: WebView,
                url: String,
                message: String,
                result: JsResult
            ): Boolean {
                return onWebChromeClientListener?.onJsAlert(
                    view,
                    url,
                    message,
                    result
                ) ?: super.onJsAlert(view, url, message, result)
            }

            override fun onJsConfirm(
                view: WebView,
                url: String,
                message: String,
                result: JsResult
            ): Boolean {
                return onWebChromeClientListener?.onJsConfirm(
                    view,
                    url,
                    message,
                    result
                ) ?: super.onJsConfirm(view, url, message, result)
            }

            override fun onJsPrompt(
                view: WebView,
                url: String,
                message: String,
                defaultValue: String,
                result: JsPromptResult
            ): Boolean {
                return onWebChromeClientListener?.onJsPrompt(
                    view,
                    url,
                    message,
                    defaultValue,
                    result
                ) ?: super.onJsPrompt(view, url, message, defaultValue, result)
            }

            override fun onJsBeforeUnload(
                view: WebView,
                url: String,
                message: String,
                result: JsResult
            ): Boolean {
                return onWebChromeClientListener?.onJsBeforeUnload(
                    view,
                    url,
                    message,
                    result
                ) ?: super.onJsBeforeUnload(view, url, message, result)
            }

            override fun onExceededDatabaseQuota(
                url: String,
                databaseIdentifier: String,
                quota: Long,
                estimatedDatabaseSize: Long,
                totalQuota: Long,
                quotaUpdater: WebStorage.QuotaUpdater
            ) {
                onWebChromeClientListener?.onExceededDatabaseQuota(
                    url,
                    databaseIdentifier,
                    quota,
                    estimatedDatabaseSize,
                    totalQuota,
                    quotaUpdater
                ) ?: super.onExceededDatabaseQuota(
                    url,
                    databaseIdentifier,
                    quota,
                    estimatedDatabaseSize,
                    totalQuota,
                    quotaUpdater
                )
            }

            /*Android 13 没有
            override fun onReachedMaxAppCacheSize(
                requiredStorage: Long,
                quota: Long,
                quotaUpdater: WebStorage.QuotaUpdater?
            ) {
                if (onWebChromeClientListener != null) {
                    onWebChromeClientListener!!.onReachedMaxAppCacheSize(
                        requiredStorage,
                        quota,
                        quotaUpdater
                    )
                } else {
                    super.onReachedMaxAppCacheSize(requiredStorage, quota, quotaUpdater)
                }
            }*/

            override fun onGeolocationPermissionsShowPrompt(
                origin: String,
                callback: GeolocationPermissions.Callback
            ) {
                onWebChromeClientListener?.onGeolocationPermissionsShowPrompt(origin, callback)
                    ?: super.onGeolocationPermissionsShowPrompt(origin, callback)
            }

            override fun onGeolocationPermissionsHidePrompt() {
                onWebChromeClientListener?.onGeolocationPermissionsHidePrompt()
                    ?: super.onGeolocationPermissionsHidePrompt()
            }

            override fun onPermissionRequest(request: PermissionRequest) {
                onWebChromeClientListener?.onPermissionRequest(request)
                    ?: super.onPermissionRequest(request)
            }

            override fun onPermissionRequestCanceled(request: PermissionRequest) {
                onWebChromeClientListener?.onPermissionRequestCanceled(request)
                    ?: super.onPermissionRequestCanceled(request)
            }

            override fun onJsTimeout(): Boolean {
                return onWebChromeClientListener?.onJsTimeout() ?: super.onJsTimeout()
            }

            override fun onConsoleMessage(message: String, lineNumber: Int, sourceID: String) {
                onWebChromeClientListener?.onConsoleMessage(message, lineNumber, sourceID)
                    ?: super.onConsoleMessage(message, lineNumber, sourceID)
            }

            override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                return onWebChromeClientListener?.onConsoleMessage(
                    consoleMessage
                ) ?: super.onConsoleMessage(consoleMessage)
            }

            override fun getDefaultVideoPoster(): Bitmap? {
                return onWebChromeClientListener?.getDefaultVideoPoster()
                    ?: super.getDefaultVideoPoster()
            }

            override fun getVideoLoadingProgressView(): View? {
                return onWebChromeClientListener?.getVideoLoadingProgressView()
                    ?: super.getVideoLoadingProgressView()
            }

            override fun getVisitedHistory(callback: ValueCallback<Array<String>>) {
                onWebChromeClientListener?.getVisitedHistory(callback) ?: super.getVisitedHistory(
                    callback
                )
            }

            override fun onShowFileChooser(
                webView: WebView,
                filePathCallback: ValueCallback<Array<Uri?>>,
                fileChooserParams: FileChooserParams
            ): Boolean {
                return onWebChromeClientListener?.onShowFileChooser(
                    webView,
                    filePathCallback,
                    fileChooserParams
                ) ?: super.onShowFileChooser(webView, filePathCallback, fileChooserParams)
            }
        }
    }

    /**
     * @param customJs 自定义桥名，可为空，为空时使用默认桥名
     * 自定义桥名回调，如用自定义桥名，请copy一份WebViewJavascriptBridge.js替换文件名
     * 及脚本内所有包含"WebViewJavascriptBridge"的内容为你的自定义桥名
     * @author hjhrq1991 created at 6/20/16 17:32.
     */
    fun setCustom(customJs: String?) {
        BridgeConfig.customJs =
            if (!TextUtils.isEmpty(customJs)) customJs!! else BridgeConfig.defaultJs
    }
}