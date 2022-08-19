package com.angcyo.library.component

import java.net.BindException
import java.net.ServerSocket

/**
 * 端口操作类
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/08/19
 */
object Port {

    /**判断端口是否被占用*/
    fun isPortOccupy(port: Int): Boolean {
        var result = false
        var socket: ServerSocket? = null
        try {
            socket = ServerSocket(port)
        } catch (e: BindException) {
            //java.net.BindException: bind failed: EADDRINUSE (Address already in use)
            result = true
        } finally {
            try {
                socket?.close()
            } catch (e: Exception) {
            }
        }
        return result
    }

    /**随机分配一个端口*/
    fun assignPort(): Int {
        var port = 0
        var socket: ServerSocket? = null
        try {
            socket = ServerSocket(0)
            port = socket.localPort
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                socket?.close()
            } catch (e: Exception) {
            }
        }
        return port
    }

    /**生成一个可用的端口*/
    fun generatePort(port: Int): Int {
        var result = port
        while (isPortOccupy(result)) {
            result++
            if (result > 65535) {
                result = assignPort()
            }
        }
        return result
    }
}