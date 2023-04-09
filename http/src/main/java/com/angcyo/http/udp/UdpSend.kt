package com.angcyo.http.udp

import com.angcyo.library.L
import com.angcyo.library.annotation.CallPoint
import com.angcyo.library.annotation.DSL
import com.angcyo.library.ex.size
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

/**
 * 使用UDP发送数据, 如果地址是: 255.255.255.255 , 那么就是广播
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2023/04/09
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class UdpSend {

    /**需要发送的目标ip地址,
     * 如果地址是: 255.255.255.255 , 那么就是广播
     * */
    var address: String? = null

    /**发送的端口*/
    var port = 0

    /**需要发送的数据*/
    var bytes: ByteArray? = null

    /**发送数据的回调*/
    var onSendAction: (error: Exception?) -> Unit = { }

    /**是否被取消*/
    var isCancel: AtomicBoolean = AtomicBoolean(false)

    /**开始发送数据*/
    @CallPoint
    fun startSend() {
        thread {
            try {
                val socket = DatagramSocket()
                val data = DatagramPacket(
                    bytes,
                    0,
                    bytes.size(),
                    InetAddress.getByName(address),
                    port
                )//数据包
                socket.soTimeout = 5_000 //5s超时
                socket.send(data)//发送UDP数据包
                socket.close()
                if (!isCancel.get()) {
                    onSendAction(null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (!isCancel.get()) {
                    onSendAction(e)
                }
            }
            L.i("UDP发送数据结束")
        }
    }
}

/**使用UDP发送数据*/
@DSL
fun udpSend(
    content: String,
    port: Int,
    address: String,
    init: UdpSend.() -> Unit
): UdpSend {
    return UdpSend().apply {
        this.port = port
        this.address = address
        this.bytes = content.toByteArray(UdpReceive.DEF_CHARSET)
        init()
        startSend()
    }
}