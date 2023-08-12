package com.angcyo.web.api

import android.webkit.WebView
import androidx.fragment.app.Fragment
import com.angcyo.library.annotation.CallPoint

/**
 * https://developer.android.com/reference/android/webkit/WebView#addJavascriptInterface(java.lang.Object,%20java.lang.String)
 *
 * ```
 *  class JsObject {
 *     @JavascriptInterface
 *     public String toString() { return "injectedObject"; }
 *  }
 *  webview.getSettings().setJavaScriptEnabled(true);
 *  webView.addJavascriptInterface(new JsObject(), "injectedObject");
 *  webView.loadData("html", "text/html", null);
 *  webView.loadUrl("javascript:alert(injectedObject.toString())");
 * ```
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/08/12
 */
object WebApi {

    /**原生的api接口*/
    val javascriptInterfaceList = mutableListOf<IJavascriptInterface>().apply {
        add(AndroidCoreApi())
        add(TApi())
    }

    @CallPoint
    fun initJavascriptInterface(fragment: Fragment, webView: WebView) {
        javascriptInterfaceList.forEach {
            try {
                webView.addJavascriptInterface(it, it.objName)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}