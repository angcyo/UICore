package com.hjhrq1991.library

import android.content.Context
import android.text.TextUtils
import android.webkit.WebView
import com.angcyo.library.L
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

object BridgeUtil {
    const val YY_OVERRIDE_SCHEMA = "yy://"
    const val YY_RETURN_DATA =
        YY_OVERRIDE_SCHEMA + "return/" //格式为   yy://return/{function}/returncontent
    const val YY_FETCH_QUEUE = YY_RETURN_DATA + "_fetchQueue/"
    const val EMPTY_STR = ""
    const val UNDERLINE_STR = "_"
    const val SPLIT_MARK = "/"
    const val CALLBACK_ID_FORMAT = "JAVA_CB_%s"
    const val JS_HANDLE_MESSAGE_FROM_JAVA =
        "javascript:WebViewJavascriptBridge._handleMessageFromNative('%s');"
    const val JS_FETCH_QUEUE_FROM_JAVA = "javascript:WebViewJavascriptBridge._fetchQueue();"
    const val JAVASCRIPT_STR = "javascript:"

    @JvmStatic
    fun parseFunctionName(jsUrl: String, customJs: String): String {
        return jsUrl.replace("javascript:$customJs.", "").replace("\\(.*\\);".toRegex(), "")
    }

    @JvmStatic
    fun getDataFromReturnUrl(url: String): String? {
        if (url.startsWith(YY_FETCH_QUEUE)) {
            return url.replace(YY_FETCH_QUEUE, EMPTY_STR)
        }
        val temp = url.replace(YY_RETURN_DATA, EMPTY_STR)
        val functionAndData = temp.split(SPLIT_MARK.toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
        if (functionAndData.size >= 2) {
            val sb = StringBuilder()
            for (i in 1 until functionAndData.size) {
                sb.append(functionAndData[i])
            }
            return sb.toString()
        }
        return null
    }

    @JvmStatic
    fun getFunctionFromReturnUrl(url: String): String? {
        val temp = url.replace(YY_RETURN_DATA, EMPTY_STR)
        val functionAndData = temp.split(SPLIT_MARK.toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
        return if (functionAndData.size >= 1) {
            functionAndData[0]
        } else null
    }

    /**
     * js 文件将注入为第一个script引用
     *
     * @param view webview
     * @param url url
     */
    fun webViewLoadJs(view: WebView, url: String) {
        var js = "var newscript = document.createElement(\"script\");"
        js += "newscript.src=\"$url\";"
        js += "document.scripts[0].parentNode.insertBefore(newscript,document.scripts[0]);"
        view.loadUrl("javascript:$js")
    }

    fun webViewLoadLocalJs(view: WebView, path: String?) {
        val jsContent = assetFile2Str(view.context, path)
        view.loadUrl("javascript:$jsContent")
    }

    @JvmStatic
    fun webViewLoadLocalJs(view: WebView, path: String?, defaultJs: String, customJs: String?) {
        L.i("准备加载桥接js->$path $defaultJs $customJs")
        var jsContent = assetFile2Str(view.context, path)
        if (!TextUtils.isEmpty(jsContent)) {
            jsContent = jsContent!!.replace(defaultJs.toRegex(), customJs!!)
        }
        view.loadUrl("javascript:$jsContent")
    }

    fun assetFile2Str(c: Context, urlStr: String?): String? {
        var `in`: InputStream? = null
        try {
            `in` = c.assets.open(urlStr!!)
            val bufferedReader = BufferedReader(InputStreamReader(`in`))
            var line: String? = null
            val sb = StringBuilder()
            do {
                line = bufferedReader.readLine()
                if (line != null && !line.matches("^\\s*\\/\\/.*".toRegex())) {
                    sb.append(line)
                }
            } while (line != null)
            bufferedReader.close()
            `in`.close()
            return sb.toString()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (`in` != null) {
                try {
                    `in`.close()
                } catch (e: IOException) {
                }
            }
        }
        return null
    }
}