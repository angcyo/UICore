package com.hjhrq1991.library

import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Message
import android.view.KeyEvent
import android.webkit.ClientCertRequest
import android.webkit.HttpAuthHandler
import android.webkit.SslErrorHandler
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.webkit.WebResourceErrorCompat
import androidx.webkit.WebViewClientCompat
import com.hjhrq1991.library.BridgeUtil.webViewLoadLocalJs
import java.io.UnsupportedEncodingException
import java.net.URLDecoder

/**
 * 2023-6-8 angcyo
 * @author hjhrq1991 created at 8/22/16 14 41.
 */
open class BridgeWebViewClient(private val webView: BridgeWebView) : WebViewClientCompat() {
    /**
     * 是否重定向，避免web为渲染即跳转导致系统未调用onPageStarted就调用onPageFinished方法引起的js桥初始化失败
     */
    private var isRedirected = false

    /**
     * onPageStarted连续调用次数,避免渲染立马跳转可能连续调用onPageStarted多次并且调用shouldOverrideUrlLoading后不调用onPageStarted引起的js桥未初始化问题
     */
    private var onPageStartedCount = 0

    private var bridgeWebViewClientListener: BridgeWebViewClientListener? = null

    fun setBridgeWebViewClientListener(bridgeWebViewClientListener: BridgeWebViewClientListener?) {
        this.bridgeWebViewClientListener = bridgeWebViewClientListener
    }

    fun removeListener() {
        if (bridgeWebViewClientListener != null) {
            bridgeWebViewClientListener = null
        }
    }

    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        //modify：hjhrq1991，web为渲染即跳转导致系统未调用onPageStarted就调用onPageFinished方法引起的js桥初始化失败
        var url = url
        if (onPageStartedCount < 2) {
            isRedirected = true
        }
        onPageStartedCount = 0
        try {
            url = URLDecoder.decode(url, "UTF-8")
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        return if (url.startsWith(BridgeUtil.YY_RETURN_DATA)) { // 如果是返回数据
            webView.handlerReturnData(url)
            true
        } else if (url.startsWith(BridgeUtil.YY_OVERRIDE_SCHEMA)) { //
            webView.flushMessageQueue()
            true
        } else {
            bridgeWebViewClientListener?.shouldOverrideUrlLoading(view, url)
                ?: super.shouldOverrideUrlLoading(view, url)
        }
    }

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        var url = request.url.toString()
        //modify：hjhrq1991，web为渲染即跳转导致系统未调用onPageStarted就调用onPageFinished方法引起的js桥初始化失败
        if (onPageStartedCount < 2) {
            isRedirected = true
        }
        onPageStartedCount = 0
        try {
            url = URLDecoder.decode(url, "UTF-8")
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        return if (url.startsWith(BridgeUtil.YY_RETURN_DATA)) { // 如果是返回数据
            webView.handlerReturnData(url)
            true
        } else if (url.startsWith(BridgeUtil.YY_OVERRIDE_SCHEMA)) { //
            webView.flushMessageQueue()
            true
        } else {
            bridgeWebViewClientListener?.shouldOverrideUrlLoading(view, request)
                ?: super.shouldOverrideUrlLoading(view, request)
        }
    }

    override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
        //modify：hjhrq1991，web为渲染即跳转导致系统未调用onPageStarted就调用onPageFinished方法引起的js桥初始化失败
        isRedirected = false
        onPageStartedCount++
        bridgeWebViewClientListener?.onPageStarted(view, url, favicon)
    }

    override fun onPageFinished(view: WebView, url: String) {
        //modify：hjhrq1991，web为渲染即跳转导致系统未调用onPageStarted就调用onPageFinished方法引起的js桥初始化失败
        if (BridgeConfig.toLoadJs != null && !url.contains("about:blank") && !isRedirected) {
            webViewLoadLocalJs(
                view,
                BridgeConfig.toLoadJs,
                BridgeConfig.defaultJs,
                BridgeConfig.customJs
            )
        }
        if (webView.startupMessage != null) {
            for (m in webView.startupMessage!!) {
                webView.dispatchMessage(m)
            }
            webView.startupMessage = null
        }
        bridgeWebViewClientListener?.onPageFinished(view, url)
    }

    override fun onReceivedError(
        view: WebView,
        errorCode: Int,
        description: String,
        failingUrl: String
    ) {
        bridgeWebViewClientListener?.onReceivedError(view, errorCode, description, failingUrl)
    }

    override fun onLoadResource(view: WebView, url: String) {
        bridgeWebViewClientListener?.onLoadResource(view, url)
    }

    override fun shouldInterceptRequest(view: WebView, url: String): WebResourceResponse? {
        return bridgeWebViewClientListener?.shouldInterceptRequest(view, url)
            ?: super.shouldInterceptRequest(view, url)
    }

    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest
    ): WebResourceResponse? {
        return if (bridgeWebViewClientListener != null) {
            bridgeWebViewClientListener?.shouldInterceptRequest(view, request)
        } else {
            super.shouldInterceptRequest(view, request)
        }
    }

    override fun onTooManyRedirects(view: WebView, cancelMsg: Message, continueMsg: Message) {
        val interrupt =
            bridgeWebViewClientListener?.onTooManyRedirects(view, cancelMsg, continueMsg) ?: false
        if (!interrupt) {
            super.onTooManyRedirects(view, cancelMsg, continueMsg)
        }
    }

    override fun onFormResubmission(view: WebView, dontResend: Message, resend: Message) {
        val interrupt =
            bridgeWebViewClientListener?.onFormResubmission(view, dontResend, resend) ?: false
        if (!interrupt) {
            super.onFormResubmission(view, dontResend, resend)
        }
    }

    override fun doUpdateVisitedHistory(view: WebView, url: String, isReload: Boolean) {
        bridgeWebViewClientListener?.doUpdateVisitedHistory(view, url, isReload)
    }

    override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
        val interrupt =
            bridgeWebViewClientListener?.onReceivedSslError(view, handler, error) ?: false
        if (!interrupt) {
            super.onReceivedSslError(view, handler, error)
        }
    }

    override fun onReceivedClientCertRequest(view: WebView, request: ClientCertRequest) {
        val interrupt =
            bridgeWebViewClientListener?.onReceivedClientCertRequest(view, request) ?: false
        if (!interrupt) {
            super.onReceivedClientCertRequest(view, request)
        }
    }

    override fun onReceivedHttpAuthRequest(
        view: WebView,
        handler: HttpAuthHandler,
        host: String,
        realm: String
    ) {
        val interrupt =
            bridgeWebViewClientListener?.onReceivedHttpAuthRequest(view, handler, host, realm)
                ?: false
        if (!interrupt) {
            super.onReceivedHttpAuthRequest(view, handler, host, realm)
        }
    }

    override fun shouldOverrideKeyEvent(view: WebView, event: KeyEvent): Boolean {
        return bridgeWebViewClientListener?.shouldOverrideKeyEvent(view, event)
            ?: super.shouldOverrideKeyEvent(view, event)
    }

    override fun onUnhandledKeyEvent(view: WebView, event: KeyEvent) {
        val interrupt = bridgeWebViewClientListener?.onUnhandledKeyEvent(view, event) ?: false
        if (!interrupt) {
            super.onUnhandledKeyEvent(view, event)
        }
    }

    override fun onScaleChanged(view: WebView, oldScale: Float, newScale: Float) {
        bridgeWebViewClientListener?.onScaleChanged(view, oldScale, newScale)
    }

    override fun onReceivedLoginRequest(
        view: WebView,
        realm: String,
        account: String?,
        args: String
    ) {
        bridgeWebViewClientListener?.onReceivedLoginRequest(view, realm, account, args)
    }

    override fun onPageCommitVisible(view: WebView, url: String) {
        bridgeWebViewClientListener?.onPageCommitVisible(view, url)
    }

    override fun onReceivedError(
        view: WebView,
        request: WebResourceRequest,
        error: WebResourceErrorCompat
    ) {
        val interrupt = bridgeWebViewClientListener?.onReceivedError(view, request, error) ?: false
        if (!interrupt) {
            super.onReceivedError(view, request, error)
        }
    }

    override fun onReceivedHttpError(
        view: WebView,
        request: WebResourceRequest,
        errorResponse: WebResourceResponse
    ) {
        bridgeWebViewClientListener?.onReceivedHttpError(view, request, errorResponse)
    }
}