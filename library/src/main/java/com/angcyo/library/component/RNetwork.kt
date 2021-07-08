package com.angcyo.library.component

import android.annotation.SuppressLint
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.*
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.angcyo.library.L
import com.angcyo.library.app
import com.angcyo.library.component.RNetwork.getNetWorkState
import com.angcyo.library.component.RNetwork.notifyObservers
import com.angcyo.library.ex.getMobileIP
import com.angcyo.library.ex.getWifiIP
import com.angcyo.library.utils.RUtils


/**
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2019/05/13
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

@SuppressLint("MissingPermission")
object RNetwork {

    private val mObservers = mutableListOf<NetStateChangeObserver>()

    val mainHandler: Handler by lazy {
        Handler(Looper.getMainLooper())
    }

    var netBroadcastReceiver: NetBroadcastReceiver? = null

    /**
     * 注册网络变化Observer
     */
    fun registerObserver(observer: NetStateChangeObserver) {
        if (!mObservers.contains(observer)) {
            mObservers.add(observer)
        }
    }

    /**
     * 取消网络变化Observer的注册
     */
    fun unregisterObserver(observer: NetStateChangeObserver) {
        mObservers.remove(observer)
    }

    /**
     * 通知所有的Observer网络状态变化
     */
    internal fun notifyObservers(networkType: NetworkType) {
        L.w("网络改变->$networkType ->${getWifiIP()} ${getMobileIP()}")

        fun notify() {
            for (observer in mObservers) {
                observer.onNetConnected(networkType)
                if (networkType == NetworkType.NETWORK_NO) {
                    observer.onNetDisconnected()
                }
            }
        }

        if (RUtils.isMainThread()) {
            notify()
        } else {
            mainHandler.post {
                notify()
            }
            //在画中画模式中, 所有子线程跳转主线程的异步操作都无法执行.所以, 这里直接使用 handler
//            ThreadExecutor.instance().onMain {
//                notify()
//            }

//            Rx.main {
//                notify()
//            }
        }
    }

    var _isInit = false

    /** xml 需要声明权限:
     * permissions: android.permission.CHANGE_NETWORK_STATE, android.permission.WRITE_SETTINGS.
     * */
    fun init(application: Application = app()) {
        if (_isInit) {
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val connectivityManager =
                application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            // 请注意这里会有一个版本适配bug，所以请在这里添加非空判断
            connectivityManager.requestNetwork(NetworkRequest.Builder().build(),
                object : ConnectivityManager.NetworkCallback() {

                    /**
                     * 网络可用的回调
                     */
                    override fun onAvailable(network: Network) {
                        super.onAvailable(network)
                        L.d("onAvailable")
                        notifyObservers(NetworkType.NETWORK_AVAILABLE)
                    }

                    /**
                     * 网络丢失的回调, 子线程回调 ConnectivityThread
                     */
                    override fun onLost(network: Network) {
                        super.onLost(network)
                        L.d("onLost")
                        notifyObservers(NetworkType.NETWORK_NO)
                    }

                    /**
                     * 当建立网络连接时，回调连接的属性, 子线程回调 ConnectivityThread
                     */
                    override fun onLinkPropertiesChanged(
                        network: Network,
                        linkProperties: LinkProperties
                    ) {
                        super.onLinkPropertiesChanged(network, linkProperties)
                        L.d("onLinkPropertiesChanged")
                        val networkInfo = connectivityManager.getNetworkInfo(network)

                        if (networkInfo != null && networkInfo.isConnected) {
                            if (networkInfo.type == ConnectivityManager.TYPE_WIFI) {
                                notifyObservers(NetworkType.NETWORK_WIFI)
                                return
                            } else if (networkInfo.type == ConnectivityManager.TYPE_MOBILE) {
                                notifyObservers(NetworkType.NETWORK_MOBILE)
                                return
                            }
                        } else {
                            //网络异常
                        }
                        notifyObservers(NetworkType.NETWORK_NO)
                        return
                    }

                    /**
                     * 按照官方的字面意思是，当我们的网络的某个能力发生了变化回调，那么也就是说可能会回调多次
                     *
                     * 之后在仔细的研究
                     */
                    override fun onCapabilitiesChanged(
                        network: Network,
                        networkCapabilities: NetworkCapabilities
                    ) {
                        super.onCapabilitiesChanged(network, networkCapabilities)
                        L.d("onCapabilitiesChanged")
                    }

                    /**
                     * 在网络失去连接的时候回调，但是如果是一个生硬的断开，他可能不回调
                     */
                    override fun onLosing(network: Network, maxMsToLive: Int) {
                        super.onLosing(network, maxMsToLive)
                        L.d("onLosing")
                    }

                    /**
                     * 按照官方注释的解释，是指如果在超时时间内都没有找到可用的网络时进行回调
                     */
                    override fun onUnavailable() {
                        super.onUnavailable()
                        L.d("onUnavailable")
                    }
                })
            _isInit = true
        } else
        /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)*/ {
            //实例化IntentFilter对象
            val filter = IntentFilter()
            filter.addAction("android.net.conn.CONNECTIVITY_CHANGE")
            netBroadcastReceiver = NetBroadcastReceiver()
            //注册广播接收
            application.registerReceiver(netBroadcastReceiver, filter)

            _isInit = true
        }
    }

    fun getNetWorkState(context: Context = app()): NetworkType {
        //得到连接管理器对象
        val connectivityManager = context
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val activeNetworkInfo = connectivityManager
            .activeNetworkInfo
        //如果网络连接，判断该网络类型
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
            if (activeNetworkInfo.type == ConnectivityManager.TYPE_WIFI) {
                return NetworkType.NETWORK_WIFI//wifi
            } else if (activeNetworkInfo.type == ConnectivityManager.TYPE_MOBILE) {
                return NetworkType.NETWORK_MOBILE//mobile
            }
        } else {
            //网络异常
            return NetworkType.NETWORK_NO
        }
        return NetworkType.NETWORK_NO
    }

    fun checkState(context: Context = app()): BooleanArray {
        val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        var isWifiConn = false
        var isMobileConn = false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //获取所有网络连接的信息
            val networks = connMgr.allNetworks
            //用于存放网络连接信息
            //val sb = StringBuilder()
            //通过循环将网络信息逐个取出来

            for (i in networks.indices) {
                //获取ConnectivityManager对象对应的NetworkInfo对象
                val networkInfo = connMgr.getNetworkInfo(networks[i])

                if ("WIFI".equals(networkInfo?.typeName, ignoreCase = true)) {
                    isWifiConn = networkInfo?.isConnected == true
                } else if ("MOBILE".equals(networkInfo?.typeName, ignoreCase = true)) {
                    //现在的手机, 在wifi 连接的时候, mobile 也会是连接状态
                    isMobileConn = networkInfo?.isConnected == true
                }
            }
        } else {
            var networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
            isWifiConn = networkInfo?.isConnected == true

            //获取移动数据连接的信息
            networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
            isMobileConn = networkInfo?.isConnected == true
        }

        return booleanArrayOf(isWifiConn, isMobileConn)
    }

    /** wifi连接 */
    fun isWifi(context: Context = app()): Boolean {
        return checkState(context)[0]
    }

    /** mobile连接*/
    fun isMobile(context: Context = app()): Boolean {
        val checkState = checkState(context)
        //wifi 未连接的情况下, mobile 连接
        return !checkState[0] && checkState[1]
    }

    /** 有网络*/
    fun isConnect(context: Context = app()): Boolean {
        return checkState(context)[0] || checkState(context)[1]
    }

    /**[android.Manifest.permission.ACCESS_NETWORK_STATE]*/
    fun isWifiConnect(context: Context = app()): Boolean {
        val manager: ConnectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)?.isConnected == true
    }
}

/**
 * <receiver android:name=".network.NetBroadcastReceiver">
 * <intent-filter>
 * <action android:name="android.net.conn.CONNECTIVITY_CHANGE"></action>
</intent-filter> *
</receiver> *
 */
class NetBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // 如果相等的话就说明网络状态发生了变化
        L.i("NetBroadcastReceiver", "NetBroadcastReceiver changed")
        if (intent.action == ConnectivityManager.CONNECTIVITY_ACTION) {
            val netWorkState = getNetWorkState(context)
            // 当网络发生变化，判断当前网络状态，并通过NetEvent回调当前网络状态
            notifyObservers(netWorkState)
        }
    }
}

/**
 * 网络状态变化观察者
 */
interface NetStateChangeObserver {

    /**网络断开*/
    fun onNetDisconnected() {

    }

    /**网络连接类型改变*/
    fun onNetConnected(networkType: NetworkType) {

    }
}

enum class NetworkType {
    NETWORK_WIFI,
    NETWORK_MOBILE,
    NETWORK_AVAILABLE,
    NETWORK_UNKNOWN,
    NETWORK_NO
}

/**网络是否有效*/
@SuppressLint("MissingPermission")
fun isNetworkAvailable(context: Context = app()): Boolean {
    try {
        val cm =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val network = cm.activeNetwork
            val networkCapabilities = cm.getNetworkCapabilities(network)
            return networkCapabilities != null &&
                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }
        val info = cm.activeNetworkInfo
        if (null != info && info.isConnected && info.isAvailable) {
            return true
        }
    } catch (e: Exception) {
        L.e("current network is not available")
        return false
    }
    return false
}