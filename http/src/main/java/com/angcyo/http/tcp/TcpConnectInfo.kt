package com.angcyo.http.tcp

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/08/04
 */
data class TcpConnectInfo(
    /**是否是自动连接*/
    var isAutoConnect: Boolean = false,
    /**是否是自动重新连接*/
    var isReConnect: Boolean = false,
    /**是否是主动断开的*/
    var isActiveDisConnected: Boolean = false,
    /**附带的其他信息*/
    var data: Any? = null
)
