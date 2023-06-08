package com.hjhrq1991.library

/**
 * @author hjhrq1991 created at 8/22/16 14 41.
 * 配置文件
 */
object BridgeConfig {
    const val toLoadJs = "WebViewJavascriptBridge.js"

    /**
     * 默认桥名
     */
    const val defaultJs = "WebViewJavascriptBridge"

    /**
     * 自定义桥名
     */
    @JvmField
    var customJs = "androidJs"
}