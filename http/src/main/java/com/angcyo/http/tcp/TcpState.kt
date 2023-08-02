package com.angcyo.http.tcp

/** TCP状态
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/07/04
 */
data class TcpState(
    /**设备信息*/
    val tcpDevice: TcpDevice,
    /**
     * [com.angcyo.http.tcp.Tcp.CONNECT_STATE_CONNECTING]
     * [com.angcyo.http.tcp.Tcp.CONNECT_STATE_CONNECTED]
     * [com.angcyo.http.tcp.Tcp.CONNECT_STATE_CONNECT_SUCCESS]
     * [com.angcyo.http.tcp.Tcp.CONNECT_STATE_DISCONNECT]
     * */
    val state: Int,
    /**附加数据*/
    val data: Any? = null
)
