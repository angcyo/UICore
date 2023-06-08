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
import com.angcyo.library.ex.toStr

/**
 * @author hjhrq1991 created at 5/10/16 15:12.
 * 超链接回调
 */
interface BridgeWebViewClientListener {
    /**
     * 非js桥的超链接回调回去自行处理，api21以下会调用
     *
     * @author hjhrq1991 created at 5/10/16 15:12.
     */
    fun shouldOverrideUrlLoading(view: WebView, url: String?): Boolean

    /**
     * 非js桥的超链接回调回去自行处理，api21及以上会调用
     */
    fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest?): Boolean =
        shouldOverrideUrlLoading(view, request?.url?.toStr())

    fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {}

    fun onPageFinished(view: WebView, url: String?) {}

    fun onReceivedError(view: WebView, errorCode: Int, description: String?, failingUrl: String?) {}

    fun onLoadResource(view: WebView, url: String?) {}

    fun shouldInterceptRequest(view: WebView, url: String?): WebResourceResponse? = null

    fun shouldInterceptRequest(view: WebView, request: WebResourceRequest?): WebResourceResponse? =
        shouldInterceptRequest(view, request?.url?.toStr())

    fun onTooManyRedirects(view: WebView, cancelMsg: Message?, continueMsg: Message?): Boolean =
        false

    fun onFormResubmission(view: WebView, dontResend: Message?, resend: Message?): Boolean = false
    fun doUpdateVisitedHistory(view: WebView, url: String?, isReload: Boolean) {}
    fun onReceivedSslError(view: WebView, handler: SslErrorHandler?, error: SslError?): Boolean =
        false

    fun onReceivedClientCertRequest(view: WebView, request: ClientCertRequest?): Boolean = false

    fun onReceivedHttpAuthRequest(
        view: WebView,
        handler: HttpAuthHandler?,
        host: String?,
        realm: String?
    ): Boolean = false

    fun shouldOverrideKeyEvent(view: WebView, event: KeyEvent?): Boolean = false
    fun onUnhandledKeyEvent(view: WebView, event: KeyEvent?): Boolean = false
    fun onScaleChanged(view: WebView, oldScale: Float, newScale: Float) {}
    fun onReceivedLoginRequest(view: WebView, realm: String?, account: String?, args: String?) {}
    fun onPageCommitVisible(view: WebView, url: String?) {}
    fun onReceivedError(
        view: WebView,
        request: WebResourceRequest?,
        error: WebResourceErrorCompat?
    ): Boolean = false

    fun onReceivedHttpError(
        view: WebView,
        request: WebResourceRequest?,
        errorResponse: WebResourceResponse?
    ) {
    }
}