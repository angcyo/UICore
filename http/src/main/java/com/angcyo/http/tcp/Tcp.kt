package com.angcyo.http.tcp

import androidx.annotation.WorkerThread
import com.angcyo.http.rx.doBack
import com.angcyo.library.L
import com.angcyo.library.component.ICancel
import com.angcyo.library.ex.clamp
import com.angcyo.library.ex.fileSizeString
import com.angcyo.library.ex.nowTime
import com.angcyo.library.ex.sleep
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.io.InputStream
import java.io.OutputStream
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

        /**已连接*/
        const val CONNECT_STATE_CONNECTED = 2

        /**连接成功*/
        const val CONNECT_STATE_CONNECT_SUCCESS = 3

        /**断开连接*/
        const val CONNECT_STATE_DISCONNECT = 4

        /**连接失败*/
        const val CONNECT_STATE_ERROR = -1

        /**开始发送*/
        const val SEND_STATE_START = 0

        /**发送完成*/
        const val SEND_STATE_FINISH = 1

        /**发送错误*/
        const val SEND_STATE_ERROR = -1
    }

    /**需要发送的目标ip地址
     * 可以是ip/也可以是域名使用域名时, 不要http, 直接www.xxx.com即可*/
    var address: String? = null

    /**发送的端口*/
    var port = 80

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
    fun init() {
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
    }

    /**连接到服务器*/
    fun connect() {
        init()
        val socket = socket ?: return
        if (socket.isConnected) {
            for (listener in listeners) {
                listener.onConnectStateChanged(this, CONNECT_STATE_CONNECTED)
            }
            return
        }
        for (listener in listeners) {
            listener.onConnectStateChanged(this, CONNECT_STATE_CONNECTING)
        }
        doBack {
            try {
                L.d("TCP准备连接:$address:$port")
                socket.connect(InetSocketAddress(address, port), soTimeout)
                onSocketConnectSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
                for (listener in listeners) {
                    listener.onConnectStateChanged(this, CONNECT_STATE_ERROR)
                }
            }
        }
    }

    /**重连*/
    private fun reconnect() {
        val socket = socket ?: return
        while (!socket.isConnected) {
            sleep(1000)
            try {
                socket.connect(InetSocketAddress(address, port), socket.soTimeout)
                onSocketConnectSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun cancel() {
        socket ?: return
        release()
    }

    /**释放资源*/
    fun release() {
        _inputThread?.interrupt()
        _outputThread?.interrupt()
        try {
            _socketInputStream?.close()
            _socketOutputStream?.close()
            val socket = socket
            socket?.shutdownInput()
            socket?.shutdownOutput()
            if (socket != null && socket.isConnected) {
                try {
                    socket.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                for (listener in listeners) {
                    listener.onConnectStateChanged(this, CONNECT_STATE_DISCONNECT)
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
    private fun onSocketConnectSuccess() {
        val socket = socket ?: return
        _socketInputStream = socket.getInputStream()
        _socketOutputStream = socket.getOutputStream()
        startInputThread()
        for (listener in listeners) {
            listener.onConnectStateChanged(this, CONNECT_STATE_CONNECT_SUCCESS)
        }
    }

    /**socket被关闭后触发*/
    private fun onSocketClose() {
        for (listener in listeners) {
            listener.onConnectStateChanged(this, CONNECT_STATE_DISCONNECT)
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
                while (socket.isConnected) {
                    try {
                        //L.d("TCP准备接收数据:$address:$port")
                        val read = inputStream.read(bytes) //阻塞
                        L.d("TCP接收数据:$address:$port [${read}/${bufferSize}]")
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

        if (socket == null) {
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
                                    append("TCP发送:$address:$port ")
                                    append("[${sendSize}/${allSize}] ")
                                    append("进度:${progress}% ")
                                    val duration = nowTime() - startSendTime
                                    val speedStr =
                                        (sendSize * 1000 / max(1, duration)).fileSizeString()
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
        fun onConnectStateChanged(tcp: Tcp, state: Int) {

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