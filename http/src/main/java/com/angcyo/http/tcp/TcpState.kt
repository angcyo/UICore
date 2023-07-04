package com.angcyo.http.tcp

/** TCP状态
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/07/04
 */
data class TcpState(
    val tcp: Tcp,
    val state: Int,
    val data: Any? = null
)
