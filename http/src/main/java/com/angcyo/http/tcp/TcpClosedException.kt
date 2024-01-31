package com.angcyo.http.tcp

/**
 * Wifi设备已关闭
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/10/25
 */
class TcpClosedException(message: String? = "Tcp Closed") : Exception(message)