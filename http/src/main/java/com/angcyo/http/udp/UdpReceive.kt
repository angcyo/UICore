package com.angcyo.http.udp

import com.angcyo.library.L
import com.angcyo.library.annotation.CallPoint
import com.angcyo.library.annotation.DSL
import com.angcyo.library.ex.size
import java.net.BindException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.SocketTimeoutException
import java.nio.charset.Charset
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

/**
 * 使用UDP接收数据
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2023/04/09
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class UdpReceive {

    companion object {
        /**buffer大小*/
        const val BUFFER_SIZE = 1024

        /**默认的字符编码*/
        val DEF_CHARSET: Charset = Charsets.UTF_8
    }

    /**接收的端口*/
    var port = 0

    /**接收的字节大小*/
    var bufferSize: Int = BUFFER_SIZE

    /**字符编码*/
    var charset: Charset = DEF_CHARSET

    /**接收超时时长, 0表示一直等待, 秒*/
    var receiveTimeout: Int = 5_000

    /**超时后, 是否一直重试*/
    var timeoutRetry: Boolean = true

    /**接收到数据的回调*/
    var onReceiveAction: (content: String?, error: Exception?) -> Unit = { _, _ -> }

    /**是否被取消*/
    var isCancel: AtomicBoolean = AtomicBoolean(false)

    /**开始接收数据*/
    @CallPoint
    fun startReceive() {
        thread {
            var socket: DatagramSocket? = null
            try {
                socket = DatagramSocket(port)
                val buffer = ByteArray(bufferSize)
                val data = DatagramPacket(buffer, buffer.size())//数据包存放
                L.i("UDP开始接收数据[$port]")
                socket.soTimeout = receiveTimeout //5s超时
                while (true) {
                    try {
                        socket.receive(data)//接收UDP数据
                        break
                    } catch (e: SocketTimeoutException) {
                        if (timeoutRetry) {
                            continue
                        } else {
                            throw e
                        }
                    }
                }
                socket.close()

                //内容结构: AABB
                val contentBuffer = buffer.sliceArray(0 until minOf(data.length, bufferSize))
                val content = contentBuffer.toString(charset)
                if (!isCancel.get()) {
                    onReceiveAction(content, null)
                }
            } catch (e: Exception) {
                try {
                    socket?.close()
                } catch (e: Exception) {
                }
                e.printStackTrace()
                if (e is BindException) {
                    L.w("UDP端口[$port]被占用, 请更换端口!")
                }
                if (!isCancel.get()) {
                    onReceiveAction(null, e)
                }
            }
            L.i("UDP接收数据结束")
        }
    }
}

/**使用UDP接收数据*/
@DSL
fun udpReceive(
    port: Int,
    bufferSize: Int = UdpReceive.BUFFER_SIZE,
    init: UdpReceive.() -> Unit = {},
    onReceiveAction: (content: String?, error: Exception?) -> Unit = { _, _ -> }
): UdpReceive {
    return UdpReceive().apply {
        this.port = port
        this.bufferSize = bufferSize
        this.onReceiveAction = onReceiveAction
        init()
        startReceive()
    }
}