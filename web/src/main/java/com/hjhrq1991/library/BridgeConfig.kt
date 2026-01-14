package com.hjhrq1991.library

import com.hjhrq1991.library.BridgeConfig.customJs


/**
 * @author hjhrq1991 created at 8/22/16 14 41.
 * 配置文件
 */
object BridgeConfig {
    const val toLoadJs = "WebViewJavascriptBridge.js"

    /**
     * 默认的桥名
     * - [customJs]需要替换成的桥名
     */
    const val defaultJs = "WebViewJavascriptBridge"

    /**
     * 自定义桥名
     */
    @JvmField
    var customJs = "androidJs"
}