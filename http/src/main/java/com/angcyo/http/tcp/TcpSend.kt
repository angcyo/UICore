package com.angcyo.http.tcp

import android.app.PendingIntent.CanceledException
import com.angcyo.http.rx.doBack
import com.angcyo.library.L
import com.angcyo.library.annotation.CallPoint
import com.angcyo.library.annotation.Implementation
import com.angcyo.library.annotation.ThreadDes
import com.angcyo.library.component.ICancel
import com.angcyo.library.ex.clamp
import com.angcyo.library.ex.sleep
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.net.ConnectException
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 使用TCP发送数据
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/04/26
 */
class TcpSend : ICancel {

    /**需要发送的目标ip地址
     * 可以是ip/也可以是域名使用域名时, 不要http, 直接www.xxx.com即可*/
    var address: String? = null

    /**发送的端口*/
    var port = 80

    /**发送数据缓存大小*/
    var sendBufferSize = 4096

    /**发送延迟, 毫秒*/
    var sendDelay = 0

    /**发送超过此字节数据之后, 延迟[sendDelay]*/
    var sendDelayByteCount = 0

    /**读流的超时时长, 同时也是连接超时时长*/
    var soTimeout = 5000

    /**需要发送的数据*/
    var sendBytes: ByteArray? = null

    /**接收到的数据*/
    var receiveBytes: ByteArray? = null

    /**发送的进度[0~1]*/
    var sendPercentage = 0f

    /**接收的进度[0~1], 需要知道总共有多少数据需要接收, 才能计算出来*/
    @Implementation
    private var receivePercentage = 0f

    /**发送进度回调[0~100]*/
    @ThreadDes("子线程回调")
    var onSendProgressAction: (progress: Int) -> Unit = {}

    /**发送数据的回调,
     * [receiveBytes] 接收到数据
     * [percentage] 发送进度[0~100]
     * [error] 是否有异常*/
    @ThreadDes("子线程回调")
    var onSendAction: (receiveBytes: ByteArray?, error: Exception?) -> Unit =
        { _, _ -> }

    /**是否被取消*/
    var isCancel: AtomicBoolean = AtomicBoolean(false)

    /**是否被关闭*/
    var isClose: AtomicBoolean = AtomicBoolean(false)

    /**读取数据完成后, 是否自动关闭socket*/
    var autoClose: AtomicBoolean = AtomicBoolean(true)

    /**是否要输出log*/
    var log: Boolean = L.debug

    /**开始发送数据*/
    @CallPoint
    fun startSend() {
        doBack {
            try {
                val socket = Socket()
                _socket = socket
                socket.soTimeout = soTimeout //5s超时
                socket.connect(InetSocketAddress(address, port), socket.soTimeout)
                socket.tcpNoDelay = true
                L.i("TCP连接成功:$address:$port")

                //在线程中, 读取数据
                doBack {
                    readRunnable.run()
                    close()//读取完成后, 关闭socket
                }

                //写入数据到网络
                val buffer = ByteArray(sendBufferSize)
                val outputStream = socket.getOutputStream()
                sendPercentage = 0f

                val allSize = sendBytes?.size ?: 0
                var sendSize = 0L
                var delaySendSize = 0L
                DataInputStream(ByteArrayInputStream(sendBytes)).use { bytesInput ->
                    while (true) {
                        val size = bytesInput.read(buffer)
                        if (size > 0) {
                            outputStream.write(buffer, 0, size)
                            outputStream.flush()

                            sendSize += size
                            delaySendSize += size
                            sendPercentage = sendSize * 1f / allSize

                            //发送进度
                            val progress = clamp((sendPercentage * 100).toInt(), 0, 100)
                            L.i("TCP发送:$address:$port [${sendSize}/${allSize}] 进度:${progress}%")
                            onSendProgressAction(progress)

                            if (size < sendBufferSize) {
                                break
                            }
                        } else {
                            break
                        }
                        if (sendDelay > 0 && sendDelayByteCount in 1..delaySendSize) {
                            delaySendSize = 0
                            sleep(sendDelay.toLong())
                        }
                    }
                }

                /*socket.getOutputStream().write(sendBytes)*/
                /*DataOutputStream(socket.getOutputStream()).use { output ->
                    output.write(sendBytes)
                    output.flush()
                    socket.shutdownOutput() //close流之后, socket会自动关闭
                }*/

                L.d("TCP发送数据:$address:$port ${allSize}bytes")
            } catch (e: Exception) {
                if (e is SocketTimeoutException) {
                    //java.net.SocketTimeoutException: failed to connect to /192.168.31.16 (port 8080) from /192.168.31.57 (port 46400) after 5000ms
                    logE("TCP连接超时[${soTimeout}ms]:$address:$port $e")
                } else if (e is ConnectException || e is UnknownHostException) {
                    //java.net.ConnectException: failed to connect to /192.168.31.5 (port 8080) from /192.168.31.57 (port 51032) after 5000ms: isConnected failed: EHOSTUNREACH (No route to host)
                    logE("TCP连接异常[${soTimeout}ms]:$address:$port $e")
                } else {
                    logE("TCP发送数据异常:$address:$port $e")
                    e.printStackTrace()
                }
                if (isCancel.get()) {
                    onSendAction(null, CanceledException())
                } else {
                    onSendAction(null, e)
                }
            }
            //L.i("TCP发送数据结束:$address:$port")
        }
    }

    /**主动取消*/
    override fun cancel(data: Any?) {
        isCancel.set(true)
        close()
    }

    /**主动关闭*/
    fun close() {
        isClose.set(true)
        _socket?.let {
            if (!it.isClosed) {
                it.close()
            }
        }
    }

    private fun logE(msg: String) {
        if (log) {
            L.e(msg)
        }
    }

    private fun error(e: Exception) {
        if (isCancel.get()) {
            onSendAction(null, CanceledException())
        } else {
            onSendAction(null, e)
        }
    }

    private var _socket: Socket? = null
    private val readRunnable = Runnable {
        val socket = _socket ?: return@Runnable
        try {//读取数据
            DataInputStream(socket.getInputStream()).use { input ->
                val bufferSize = 1024
                val output = ByteArrayOutputStream(bufferSize)
                val receiveBytes = ByteArray(bufferSize)

                while (isClose.get().not()) {
                    val size = input.read(receiveBytes) //阻塞方法
                    if (size > 0) {
                        output.write(receiveBytes, 0, size)
                        if (size < bufferSize) {
                            //读流完成
                            if (autoClose.get()) {
                                break
                            }
                        }
                    } else {
                        if (autoClose.get()) {
                            break
                        }
                    }
                }
                socket.shutdownInput()
                output.flush()
                this.receiveBytes = output.toByteArray()
                L.d("TCP读取数据:$address:$port ${this.receiveBytes?.size}bytes")

                output.close()

                if (isCancel.get()) {
                    onSendAction(null, CanceledException())
                } else {
                    onSendAction(this.receiveBytes, null)
                }
            }
        } catch (e: Exception) {
            if (e is SocketTimeoutException) {
                //java.net.SocketTimeoutException: failed to connect to /
                logE("TCP读取数据超时:$address:$port $e")
            } else {
                logE("TCP读取数据异常:$address:$port $e")
                //e.printStackTrace()
            }
            error(e)
        }
    }
}

/**使用tcp发送数据, 并接收. 自动关闭和销毁*/
fun tcpSend(
    address: String,
    port: Int,
    writeBytes: ByteArray,
    timeout: Int = 5000,
    @ThreadDes("工作线程回调")
    action: (receiveBytes: ByteArray?, error: Exception?) -> Unit
): TcpSend {
    val tcpSend = TcpSend()
    tcpSend.address = address
    tcpSend.port = port
    tcpSend.soTimeout = timeout
    tcpSend.sendBytes = writeBytes
    tcpSend.onSendAction = action
    tcpSend.startSend()
    return tcpSend
}