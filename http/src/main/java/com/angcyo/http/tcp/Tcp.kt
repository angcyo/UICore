package com.angcyo.http.tcp

import androidx.annotation.WorkerThread
import com.angcyo.http.rx.doBack
import com.angcyo.library.L
import com.angcyo.library.LTime
import com.angcyo.library.component.ICancel
import com.angcyo.library.ex.clamp
import com.angcyo.library.ex.fileSizeString
import com.angcyo.library.ex.nowTime
import com.angcyo.library.ex.sleep
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.ConnectException
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketException
import java.net.SocketTimeoutException
import java.util.concurrent.CopyOnWriteArraySet
import kotlin.concurrent.thread
import kotlin.math.max

/**
 * TCP 简单的数据传输
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/05/06
 */
class Tcp : ICancel {

    companion object {
        /**连接中*/
        const val CONNECT_STATE_CONNECTING = 1

        /**连接成功, 首次连接成功*/
        const val CONNECT_STATE_CONNECT_SUCCESS = 2

        /**开始断开连接*/
        const val CONNECT_STATE_DISCONNECTING = 3

        /**已断开连接*/
        const val CONNECT_STATE_DISCONNECT = 4

        /**连接失败*/
        const val CONNECT_STATE_ERROR = -1

        //---

        /**开始发送*/
        const val SEND_STATE_START = 0

        /**发送完成*/
        const val SEND_STATE_FINISH = 1

        /**发送错误*/
        const val SEND_STATE_ERROR = -1
    }

    /**当前连接的设备*/
    var tcpDevice: TcpDevice? = null

    val address: String?
        get() = tcpDevice?.address

    val port: Int?
        get() = tcpDevice?.port

    /**读流的超时时长, 同时也是超时时长*/
    var soTimeout = 5000

    /**数据缓存大小*/
    var bufferSize = 4096

    /**发送延迟*/
    var sendDelay = 0

    /**事件观察者*/
    var listeners = CopyOnWriteArraySet<TcpListener>()

    private var socket: Socket? = null

    /**初始化*/
    @Synchronized
    fun init(): Boolean {
        if (tcpDevice == null) {
            return false
        }
        if (socket == null || socket?.isClosed == true) {
            try {
                socket?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            socket = Socket()
            socket?.keepAlive = true
            socket?.soTimeout = soTimeout
        }
        //java.net.SocketException: Socket is not connected
        //val inputStream = socket?.getInputStream()
        //val outputStream = socket?.getOutputStream()
        //L.i("test")
        return true
    }

    /**TCP是否已连上
     * 关闭套接字不会清除其连接状态，这意味着如果已关闭的套接字在关闭之前已成功连接，则此方法将返回 true 已关闭的套接字（请参阅 isClosed()）。
     * */
    fun isConnected(): Boolean {
        return socket?.run { isConnected && !isClosed } ?: false
    }

    /**连接到服务器*/
    fun connect(data: Any?) {
        if (!init()) {
            return
        }
        if (socket?.isConnected == true) {
            return
        }
        for (listener in listeners) {
            listener.onConnectStateChanged(
                this,
                TcpState(tcpDevice!!.apply {
                    connectState = CONNECT_STATE_CONNECTING
                }, CONNECT_STATE_CONNECTING, data)
            )
        }
        doBack {
            try {
                var tryCount = 0
                while (this.socket?.isConnected == false) {
                    try {
                        L.d("TCP准备连接:${tcpDevice!!.address}:${tcpDevice!!.port} /$tryCount")
                        socket?.connect(
                            InetSocketAddress(tcpDevice!!.address, tcpDevice!!.port),
                            soTimeout
                        )
                        onSocketConnectSuccess(data)
                    } catch (e: ConnectException) {
                        e.printStackTrace()
                        throw e
                    } catch (e: IOException) {
                        //SocketException SocketTimeoutException
                        this.socket = null
                        init()
                        tryCount++
                        if (tryCount > 5) {
                            throw e
                        }
                        sleep(1000)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                for (listener in listeners) {
                    listener.onConnectStateChanged(
                        this,
                        TcpState(tcpDevice!!.apply {
                            connectState = CONNECT_STATE_ERROR
                        }, CONNECT_STATE_ERROR, e)
                    )
                }
            }
        }
    }

    /**重连*/
    private fun reconnect(data: Any?) {
        val socket = socket ?: return
        while (!socket.isConnected) {
            sleep(1000)
            try {
                socket.connect(InetSocketAddress(tcpDevice!!.address, tcpDevice!!.port), soTimeout)
                onSocketConnectSuccess(data)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun cancel(data: Any?) {
        socket ?: return
        release(data)
    }

    /**释放资源*/
    fun release(data: Any?) {
        try {
            val device = tcpDevice
            _inputThread?.interrupt()
            _outputThread?.interrupt()

            val socket = socket
            val isClosed = socket?.isClosed == true
            if (!isClosed && device != null) {
                for (listener in listeners) {
                    listener.onConnectStateChanged(
                        this,
                        TcpState(device.apply {
                            connectState = CONNECT_STATE_DISCONNECTING
                        }, CONNECT_STATE_DISCONNECTING, data)
                    )
                }
            }

            try {
                socket?.shutdownInput()
                _socketInputStream?.close()
            } catch (e: Exception) {
            }
            try {
                socket?.shutdownOutput()
                _socketOutputStream?.close()
            } catch (e: Exception) {
            }
            if (!isClosed && socket != null && socket.isConnected) {
                while (!socket.isClosed) {
                    try {
                        socket.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                if (device != null) {
                    for (listener in listeners) {
                        listener.onConnectStateChanged(
                            this,
                            TcpState(device.apply {
                                connectState = CONNECT_STATE_DISCONNECT
                            }, CONNECT_STATE_DISCONNECT, data)
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        socket = null
    }

    //---

    private var _socketInputStream: InputStream? = null
    private var _socketOutputStream: OutputStream? = null

    /**连接成功后触发, 启动2个读写线程*/
    private fun onSocketConnectSuccess(data: Any?) {
        val socket = socket ?: return
        _socketInputStream = socket.getInputStream()
        _socketOutputStream = socket.getOutputStream()
        startInputThread()
        for (listener in listeners) {
            listener.onConnectStateChanged(
                this,
                TcpState(tcpDevice!!.apply {
                    connectState = CONNECT_STATE_CONNECT_SUCCESS
                }, CONNECT_STATE_CONNECT_SUCCESS, data)
            )
        }
    }

    /**socket被关闭后触发*/
    private fun onSocketClose(data: Any?) {
        for (listener in listeners) {
            listener.onConnectStateChanged(
                this,
                TcpState(tcpDevice!!.apply {
                    connectState = CONNECT_STATE_DISCONNECT
                }, CONNECT_STATE_DISCONNECT, data)
            )
        }
    }

    private var _inputThread: Thread? = null
    private var _outputThread: Thread? = null

    /**开始读取线程*/
    private fun startInputThread() {
        val socket = socket ?: return
        _inputThread = thread {
            try {
                val inputStream = _socketInputStream ?: return@thread
                val bytes = ByteArray(bufferSize)
                while (socket.isConnected && !socket.isClosed) {
                    try {
                        //L.d("TCP准备接收数据:$address:$port")
                        val read = inputStream.read(bytes) //阻塞
                        L.d("TCP接收数据:${tcpDevice!!.address}:${tcpDevice!!.port} [${read}/${bufferSize}]")
                        if (read > 0) {
                            val receiveBytes = bytes.copyOf(read)
                            for (listener in listeners) {
                                listener.onReceiveBytes(this, receiveBytes)
                            }
                        }
                    } catch (e: SocketException) {
                        e.printStackTrace()
                    } catch (e: SocketTimeoutException) {
                        //L.v("TCP接收数据超时:$address:$port [${e.message}] ...")
                        val timeout = true
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**发送数据*/
    fun send(packet: ByteArray) {
        val socket = socket
        val allSize = packet.size
        var sendSize = 0
        var sendPercentage = 0f //发送百分比

        if (socket == null || socket.isClosed) {
            for (listener in listeners) {
                listener.onSendStateChanged(this, SEND_STATE_ERROR, allSize, NullPointerException())
            }
            return
        }
        _outputThread = thread {
            if (!socket.isConnected) {
                for (listener in listeners) {
                    listener.onSendStateChanged(
                        this,
                        SEND_STATE_ERROR,
                        allSize,
                        IllegalStateException()
                    )
                }
                return@thread
            }

            try {
                val outputStream = _socketOutputStream ?: return@thread
                DataInputStream(ByteArrayInputStream(packet)).use { bytesInput ->
                    val buffer = ByteArray(bufferSize)
                    for (listener in listeners) {
                        listener.onSendStateChanged(this, SEND_STATE_START, allSize, null)
                    }

                    try {
                        val startSendTime = nowTime()
                        while (true) {
                            val size = bytesInput.read(buffer)
                            if (size > 0) {
                                outputStream.write(buffer, 0, size)
                                outputStream.flush()

                                sendSize += size
                                sendPercentage = sendSize * 1f / allSize

                                //发送进度
                                val progress = clamp(sendPercentage * 100, 0f, 100f)
                                L.i(buildString {
                                    val duration = nowTime() - startSendTime
                                    append("TCP发送:${tcpDevice!!.address}:${tcpDevice!!.port} ")
                                    append(LTime.time(startSendTime))
                                    append(" [${sendSize}/${allSize}] ")
                                    append("进度:${progress}% ")
                                    val speedStr =
                                        (sendSize * 1000L / max(1, duration)).fileSizeString()
                                    append("${speedStr}/s")
                                })

                                for (listener in listeners) {
                                    listener.onSendProgress(this, allSize, sendSize, progress)
                                }

                                if (size < bufferSize) {
                                    break
                                }
                            } else {
                                break
                            }
                            if (sendDelay > 0) {
                                sleep(sendDelay.toLong())
                            }
                        }

                        for (listener in listeners) {
                            listener.onSendStateChanged(this, SEND_STATE_FINISH, allSize, null)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /*private fun startOutputThread() {
        val socket = socket ?: return
        _outputThread = thread {
            val outputStream = socket.getOutputStream()
            while (socket.isConnected) {
                try {
                    val bytes = ByteArray(bufferSize)
                    val read = outputStream.write(bytes)
                    if (read > 0) {
                        val receiveBytes = bytes.copyOf(read)
                        for (listener in listeners) {
                            listener.onSendProgress(this, receiveBytes)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }*/

    @WorkerThread
    interface TcpListener {

        /**连接状态改变通知*/
        fun onConnectStateChanged(tcp: Tcp, state: TcpState) {

        }

        /**收到字节数据*/
        fun onReceiveBytes(tcp: Tcp, bytes: ByteArray) {

        }

        /**发送状态改变通知
         * [sendAllSize] 需要发送的总字节数*/
        fun onSendStateChanged(tcp: Tcp, state: Int, sendAllSize: Int, error: Exception?) {

        }

        /**发送数据进度
         * [error] 发送是否有异常
         * [progress] 发送进度[0~100]*/
        fun onSendProgress(tcp: Tcp, allSize: Int, sendSize: Int, progress: Float) {

        }
    }
}