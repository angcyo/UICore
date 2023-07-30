package com.angcyo.library.ex

import android.Manifest
import android.content.Context
import android.net.wifi.WifiManager
import com.angcyo.library.app
import com.angcyo.library.component.isWifi
import java.net.NetworkInterface
import java.net.SocketException
import java.util.Locale

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/15
 */

fun <T> ArrayList<T>.append(element: T, maxSize: Int = 10) {
    if (size >= maxSize) {
        removeAt(0)
    }
    add(element)
}

fun <T> MutableList<T>.append(element: T, maxSize: Int = 10) {
    if (size >= maxSize) {
        removeAt(0)
    }
    add(element)
}

/**
 * 获取wifi ip地址
 *
 * [android.Manifest.permission.ACCESS_WIFI_STATE]
 * [android.text.format.Formatter.formatIpAddress]
 */
fun getWifiIP(): String? {
    return try {
        val context = app().applicationContext
        if (context.havePermissions(Manifest.permission.ACCESS_WIFI_STATE)) {
            val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInfo = wifiManager.connectionInfo
            val ipAddress = wifiInfo.ipAddress
            //ipAddress 为0时, 有可能wifi被禁用, 或者未连接. 也有可能是正在连接
            //<unknown ssid>
            //android.text.format.Formatter.formatIpAddress(ipAddress)
            String.format(
                Locale.getDefault(), "%d.%d.%d.%d",
                ipAddress and 0xff, ipAddress shr 8 and 0xff,
                ipAddress shr 16 and 0xff, ipAddress shr 24 and 0xff
            )
        } else {
            null
        }
    } catch (ex: Exception) {
        //Log.e(TAG, ex.getMessage());
        null
    }
}

/**
 * 获取wifi ssid
 * 未连接wifi时, 返回null
 *
 * ```
 * SSID: acy_k3_25g, BSSID: 74:7d:**:**:a3:c2, Supplicant state: COMPLETED, RSSI: -45, Link speed: 433Mbps, Frequency: 5745MHz, Net ID: 1, Metered hint: false, score: 0
 * SSID: <unknown ssid>, BSSID: <none>, Supplicant state: DISCONNECTED, RSSI: -127, Link speed: -1Mbps, Frequency: -1MHz, Net ID: -1, Metered hint: false, score: 0
 * ```
 *
 * [android.Manifest.permission.ACCESS_WIFI_STATE]
 */
fun getWifiSSID(): String? {
    return try {
        val context = app().applicationContext
        if (isWifi && context.havePermissions(Manifest.permission.ACCESS_WIFI_STATE)) {
            val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInfo = wifiManager.connectionInfo
            wifiInfo.ssid.trimStart('"').trimEnd('"')
        } else {
            null
        }
    } catch (ex: Exception) {
        //Log.e(TAG, ex.getMessage());
        null
    }
}

/**
 * fe80::ccf1:47ff:feee:a89d%dummy0
 */
fun getMobileIP(): String? {
    try {
        val en = NetworkInterface.getNetworkInterfaces()
        while (en.hasMoreElements()) {
            val intf = en.nextElement()
            val enumIpAddr = intf.inetAddresses
            while (enumIpAddr.hasMoreElements()) {
                val inetAddress = enumIpAddr.nextElement()
                if (!inetAddress.isLoopbackAddress) {
                    return inetAddress.hostAddress
                }
            }
        }
    } catch (ex: SocketException) {
        // Log.e(TAG, "Exception in Get IP Address: " + ex.toString());
    }
    return null
}