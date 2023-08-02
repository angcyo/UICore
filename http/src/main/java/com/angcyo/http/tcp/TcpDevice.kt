package com.angcyo.http.tcp

/**
 * Tcp设备信息
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/08/02
 */
data class TcpDevice(
    /**需要发送的目标ip地址
     * 可以是ip/也可以是域名使用域名时, 不要http, 直接www.xxx.com即可*/
    var address: String,
    /**发送的端口*/
    var port: Int,
    /**设备名称*/
    var deviceName: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TcpDevice

        if (address != other.address) return false
        if (port != other.port) return false

        return true
    }

    override fun hashCode(): Int {
        var result = address.hashCode()
        result = 31 * result + port
        return result
    }
}
