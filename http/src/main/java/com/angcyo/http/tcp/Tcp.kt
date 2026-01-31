package com.angcyo.http.tcp

import androidx.annotation.WorkerThread
import com.angcyo.http.rx.doBack
import com.angcyo.library.L
import com.angcyo.library.LTime
import com.angcyo.library.component.ICancel
import com.angcyo.library.component.hawk.LibLpHawkKeys
import com.angcyo.library.ex.clamp
import com.angcyo.library.ex.fileSizeString
import com.angcyo.library.ex.isDebuggerConnected
import com.angcyo.library.ex.nowTime
import com.angcyo.library.ex.sleep
import com.angcyo.library.libAppFile
import com.angcyo.library.utils.Constant
import com.angcyo.library.utils.LogFile
import com.angcyo.library.utils.writeTo
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.ConnectException
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.Socket
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
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

    /**代理*/
    var proxy: Proxy? = null

    /**读流的超时时长, 同时也是超时时长
     * [java.net.Socket.setSoTimeout]*/
    var soTimeout = 10000

    /**多久之后未连接, 或者未收到数据则视为断开了连接*/
    var connectTimeout = 5000

    /**[java.net.Socket.setKeepAlive]*/
    var keepAlive = true

    /**[java.net.Socket.setTcpNoDelay]*/
    var tcpNoDelay = true

    /**数据缓存大小*/
    var bufferSize = 4096

    /**发送延迟, 毫秒*/
    var sendDelay = 0L

    /**开始时发送的延迟[sendDelay]*/
    var firstSendDelay = 0L

    /**发送超过此字节数据之后, 延迟[sendDelay]*/
    var sendDelayByteCount = 0L

    /**事件观察者*/
    var listeners = CopyOnWriteArraySet<TcpListener>()

    /**最后一次发送数据时间, 如果发送数据后, 一定时间内未收到数据, 则判断为断开连接
     * [connectTimeout]*/
    private var _lastSendTime: Long = 0
    private var _receiveTimeOut: Long? = null

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
            socket = if (proxy == null) {
                Socket()
            } else {
                Socket(proxy)
            }
            socket?.keepAlive = keepAlive
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
        return try {
            socket?.run { isConnected && !isClosed } ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**连接到服务器*/
    fun connect(info: TcpConnectInfo?) {
        if (!init()) {
            return
        }
        if (isConnected()) {
            try {
                onSocketConnectSuccess(info)
            } catch (e: Exception) {
                e.printStackTrace()
                release(null)
            }
            return
        }
        for (listener in listeners) {
            listener.onConnectStateChanged(
                this, TcpState(tcpDevice!!.apply {
                    connectState = CONNECT_STATE_CONNECTING
                }, CONNECT_STATE_CONNECTING, info), info
            )
        }
        doBack {
            try {
                var tryCount = 0
                while (this.socket?.isConnected == false) {
                    try {
                        val host = if (LibLpHawkKeys.enableUseIpConnect) tcpDevice!!.resolveHost
                            ?: tcpDevice!!.address else tcpDevice!!.address
                        val port = tcpDevice!!.port
                        "TCP准备连接:$host:${port} /$tryCount".apply {
                            L.d(this)
                            writeTo(libAppFile(LogFile.log, Constant.LOG_FOLDER_NAME))
                        }
                        val socketAddress = InetSocketAddress(host, port)
                        //socket?.bind(socketAddress)

                        //Socket(InetAddress.getByName(tcpDevice!!.address), tcpDevice!!.port)

                        socket?.connect(socketAddress, connectTimeout)
                        onSocketConnectSuccess(info)
                        socket?.tcpNoDelay = tcpNoDelay
                    } catch (e: ConnectException) {
                        e.printStackTrace()
                        throw e
                    } catch (e: UnknownHostException) {
                        e.printStackTrace()
                        throw e
                    } catch (e: IOException) {
                        //SocketException SocketTimeoutException
                        this.socket = null
                        init()
                        tryCount++
                        if (tryCount > 3) {
                            //重连失败后, 断开连接
                            release(info)
                            throw e
                        }
                        sleep(1000)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                for (listener in listeners) {
                    listener.onConnectStateChanged(
                        this, TcpState(tcpDevice!!.apply {
                            connectState = CONNECT_STATE_ERROR
                        }, CONNECT_STATE_ERROR, info, e), info
                    )
                }
            }
        }
    }

    /**重连*/
    private fun reconnect(info: TcpConnectInfo?) {
        try {
            socket?.close()
        } catch (e: Exception) {
        }
        socket = null
        if (!init()) {
            return
        }
        while (!isConnected()) {
            try {
                val i = info ?: TcpConnectInfo()
                i.isReConnect = true
                connect(i)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            sleep(1000)
        }
    }

    override fun cancel(data: Any?) {
        socket ?: return
        if (data is TcpConnectInfo) {
            release(data)
        } else {
            release(TcpConnectInfo(data = data))
        }
    }

    /**释放资源*/
    fun release(info: TcpConnectInfo?) {
        try {
            _lastSendTime = 0
            val device = tcpDevice
            _inputThread?.interrupt()
            _outputThread?.interrupt()

            val socket = socket
            val isClosed = socket?.isClosed == true
            if (!isClosed && device != null) {
                for (listener in listeners) {
                    listener.onConnectStateChanged(
                        this, TcpState(device.apply {
                            connectState = CONNECT_STATE_DISCONNECTING
                        }, CONNECT_STATE_DISCONNECTING, info), info
                    )
                }
            }

            //2024-5-9 强行直接断开成功
            if (device != null) {
                for (listener in listeners) {
                    listener.onConnectStateChanged(
                        this, TcpState(device.apply {
                            connectState = CONNECT_STATE_DISCONNECT
                        }, CONNECT_STATE_DISCONNECT, info), info
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
    private fun onSocketConnectSuccess(info: TcpConnectInfo?) {
        val socket = socket ?: return
        _socketInputStream = socket.getInputStream()
        _socketOutputStream = socket.getOutputStream()
        startInputThread()
        for (listener in listeners) {
            listener.onConnectStateChanged(
                this, TcpState(tcpDevice!!.apply {
                    connectState = CONNECT_STATE_CONNECT_SUCCESS
                }, CONNECT_STATE_CONNECT_SUCCESS, info), info
            )
        }
    }

    /**socket被关闭后触发*/
    private fun onSocketClose(info: TcpConnectInfo?) {
        for (listener in listeners) {
            listener.onConnectStateChanged(
                this, TcpState(tcpDevice!!.apply {
                    connectState = CONNECT_STATE_DISCONNECT
                }, CONNECT_STATE_DISCONNECT, info), info
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
                        _lastSendTime = 0
                        L.d("TCP接收数据:${tcpDevice!!.address}:${tcpDevice!!.port} [${read}/${bufferSize}]")
                        if (read > 0) {
                            val receiveBytes = bytes.copyOf(read)
                            for (listener in listeners) {
                                listener.onReceiveBytes(this, receiveBytes)
                            }
                        }
                    } catch (e: SocketException) {
                        //java.net.SocketException: Connection reset
                        e.printStackTrace()
                        if (tcpDevice?.connectState == CONNECT_STATE_CONNECT_SUCCESS) {
                            reconnect(null) // 重连
                        }
                    } catch (e: SocketTimeoutException) {
                        //L.v("TCP接收数据超时:$address:$port [${e.message}] ...")
                        //val timeout = true
                        /*try {
                            socket.sendUrgentData(0xff)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }*/
                        val timeout = _receiveTimeOut ?: connectTimeout.toLong()
                        if (!isDebuggerConnected() && _lastSendTime > 0 && timeout > 0 && nowTime() - _lastSendTime > timeout) {
                            //断开了连接
                            //2024-5-9 不主动断开
                            //release(null)
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

    /**发送数据
     * [receiveTimeOut] 数据接收超时, 多久未收到数据时视为断开了连接*/
    fun send(packet: ByteArray, receiveTimeOut: Long? = null) {
        val socket = socket
        val allSize = packet.size.toLong()
        var sendSize = 0L
        var sendPercentage = 0f //发送百分比

        if (socket == null || socket.isClosed) {
            for (listener in listeners) {
                listener.onSendStateChanged(this, SEND_STATE_ERROR, allSize, TcpClosedException())
            }
            release(null)
            return
        }
        _lastSendTime = 0
        _receiveTimeOut = null
        _outputThread = thread {
            if (!socket.isConnected) {
                for (listener in listeners) {
                    listener.onSendStateChanged(
                        this, SEND_STATE_ERROR, allSize, TcpClosedException()
                    )
                }
                release(null)
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
                        //已发送数据量, 此数据量超过延迟数据量时, 启动延迟
                        var delaySendSize = 0L

                        if (firstSendDelay > 0) {
                            sleep(firstSendDelay)
                        }
                        while (true) {
                            val size = bytesInput.read(buffer)
                            if (size > 0) {
                                outputStream.write(buffer, 0, size)
                                outputStream.flush()

                                sendSize += size
                                delaySendSize += size
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

                            if (sendDelay > 0 && sendDelayByteCount in 1..delaySendSize) {
                                delaySendSize = 0
                                sleep(sendDelay)
                            }
                        }

                        for (listener in listeners) {
                            listener.onSendStateChanged(this, SEND_STATE_FINISH, allSize, null)
                        }

                        //发送完成
                        _lastSendTime = nowTime()
                        _receiveTimeOut = receiveTimeOut
                    } catch (e: Exception) {
                        if (socket.isClosed) {
                            L.e("TCP已断开:${tcpDevice?.address}:${tcpDevice?.port} [${e}]")
                            release(null)
                        } else {
                            e.printStackTrace()
                        }
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
        fun onConnectStateChanged(tcp: Tcp, state: TcpState, info: TcpConnectInfo?) {

        }

        /**收到字节数据*/
        fun onReceiveBytes(tcp: Tcp, bytes: ByteArray) {

        }

        /**发送状态改变通知
         * [sendAllSize] 需要发送的总字节数*/
        fun onSendStateChanged(tcp: Tcp, state: Int, sendAllSize: Long, error: Exception?) {

        }

        /**发送数据进度
         * [error] 发送是否有异常
         * [progress] 发送进度[0~100]*/
        fun onSendProgress(tcp: Tcp, allSize: Long, sendSize: Long, progress: Float) {

        }
    }
}