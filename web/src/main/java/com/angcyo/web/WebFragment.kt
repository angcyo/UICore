package com.angcyo.web

import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.WebView
import com.angcyo.web.api.WebApi
import com.angcyo.web.core.BaseWebFragment
import com.angcyo.web.core.DslWebView

/**
 * 系统[WebView]网页浏览
 *
 * [com.angcyo.tbs.core.TbsWebFragment]
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/06/08
 */
open class WebFragment : BaseWebFragment() {

    /**[WebView]*/
    var webView: DslWebView? = null

    /**追加[DslWebView], 用于打开网页*/
    override fun attachWebView(
        url: String?,
        data: String?,
        parent: ViewGroup?,
        fromInitialize: Boolean
    ) {
        super.attachWebView(url, data, parent, fromInitialize)

        webView = DslWebView(fContext()).apply {
            id = R.id.lib_web_view
            onInitWebView(this)

            //标题回调
            receivedTitleAction = {
                receivedTitle(it)
                updateHost(originalUrl)
            }

            //进度回调
            progressChangedAction = { url, progress ->
                progressChanged(url, progress)
            }

            //下载回调
            downloadAction = { url, userAgent, contentDisposition, mime, length ->
                download(url, userAgent, contentDisposition, mime, length)
            }

            //打开其他应用回调
            openAppAction = { url, activityInfo, appBean ->
                openApp(url, activityInfo, appBean)
            }

            //选择文件回调
            fileChooseAction = {
                fileChoose(it)
            }

            //api
            WebApi.initJavascriptInterface(this@WebFragment, this)

            //加载url
            if (data.isNullOrEmpty()) {
                loadUrl2(url)
            } else {
                loadDataWithBaseURL2(data)
            }
        }

        parent?.addView(webView, -1, -1)
    }

    /**[attachWebView]*/
    open fun onInitWebView(webView: DslWebView) {

    }

    //<editor-fold desc="生命周期操作">

    override fun onFragmentShow(bundle: Bundle?) {
        super.onFragmentShow(bundle)
        webView?.resumeTimers()
    }

    override fun onFragmentHide() {
        super.onFragmentHide()
        webView?.pauseTimers()
    }

    override fun onDestroy() {
        super.onDestroy()
        webView?.destroy()
    }

    override fun onBackPressed(): Boolean {
        val webView = webView
        if (webView != null && webView.canGoBack()) {
            webView.goBack()
            checkCloseView()
            return false
        }
        return true
    }

    //</editor-fold desc="生命周期操作">

    //<editor-fold desc="其他操作">

    override fun canGoBack(): Boolean = webView?.canGoBack() == true

    override fun goBack() {
        webView?.goBack()
    }

    override fun getWebTitle(): CharSequence? = webView?.title

    override fun getLoadUrl(): String? = webView?.url

    override fun getUserAgentString(): String? = webView?.settings?.userAgentString

    override fun loadUrl(url: String?) {
        webView?.apply {
            if (getLoadUrl() == url) {
                reload()
            } else {
                loadUrl2(url)
            }
        }
    }

    override fun fileChooseResult(files: Array<Uri?>?) {
        webView?.onReceiveValue(files)
    }

    //</editor-fold desc="其他操作">

}