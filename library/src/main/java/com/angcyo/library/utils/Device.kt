package com.angcyo.library.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.net.ConnectivityManager
import android.net.Proxy
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import android.telephony.TelephonyManager
import android.text.format.Formatter
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.angcyo.library.*
import com.angcyo.library.component.pad.Pad
import com.angcyo.library.component.lastContext
import com.angcyo.library.ex.connect
import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException
import java.net.NetworkInterface
import java.util.*
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/30
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
object Device {

    /**设备显示的名称: [厂家 型号]
     * Google Pixel 6
     * */
    val deviceName: String
        get() = "${Build.MANUFACTURER} ${Build.MODEL}"

    //https://developer.android.google.cn/training/articles/user-data-ids
    //86756e10cf9a9562
    var androidId: String = ""
        @SuppressLint("HardwareIds")
        get() = field.ifEmpty {
            Settings.Secure.getString(
                app().contentResolver, Settings.Secure.ANDROID_ID
            )
        }

    //00000000-4759-42f8-ffff-ffffeabf4809
    @Deprecated("相同型号的手机会重复, 请使用[androidId]")
    var deviceId: String = ""
        get() = field.ifEmpty { getUniqueDeviceId() }

    //https://www.jianshu.com/p/59440efa020c
    //设备序列号 //unknown
    val serial = Build.SERIAL

    const val PERFORMANCE_HIGH = 10
    const val PERFORMANCE_MEDIUM = 5
    const val PERFORMANCE_LOW = 3
    const val PERFORMANCE_MIN = 1

    /**
     * 获得独一无二的Psuedo ID, 2021-02-07 相同手机型号, 会出现重复的
     * https://www.jianshu.com/p/130918ed8b2f
     * */
    @Deprecated("相同型号的手机会重复, 请使用[androidId]")
    private fun getUniqueDeviceId(): String {
        var result: String
        var serial: String?
        val idShort = "35" +
                Build.BOARD.length % 10 +
                Build.BRAND.length % 10 +
                Build.CPU_ABI.length % 10 +
                Build.DEVICE.length % 10 +
                Build.DISPLAY.length % 10 +
                Build.HOST.length % 10 +
                Build.ID.length % 10 +
                Build.MANUFACTURER.length % 10 +
                Build.MODEL.length % 10 +
                Build.PRODUCT.length % 10 +
                Build.TAGS.length % 10 +
                Build.TYPE.length % 10 +
                Build.USER.length % 10 //13 位
        try {
            serial = Build::class.java.getField("SERIAL").toString()
            //API>=9 使用serial号
            result = UUID(idShort.hashCode().toLong(), serial.hashCode().toLong()).toString()
        } catch (exception: Exception) { //serial需要一个初始化
            serial = "serial" // 随便一个初始化
            //使用硬件信息拼凑出来的15位号码
            result = UUID(idShort.hashCode().toLong(), serial.hashCode().toLong()).toString()
        }
        deviceId = result
        return result
    }

    /**sd卡已用空间*/
    fun getSdUsedBytes(): Long {
        //SD空间信息
        val statFs =
            StatFs(app().getExternalFilesDir("")?.absolutePath ?: app().filesDir.absolutePath)
        val usedBytes = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            statFs.totalBytes - statFs.availableBytes
        } else {
            0L
        }
        return usedBytes
    }

    /**sd卡可用空间*/
    fun getSdAvailableBytes(): Long {
        //SD空间信息
        val statFs =
            StatFs(app().getExternalFilesDir("")?.absolutePath ?: app().filesDir.absolutePath)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            statFs.availableBytes
        } else {
            0L
        }
    }

    /**sd卡总空间*/
    fun getSdTotalBytes(): Long {
        //SD空间信息
        val statFs =
            StatFs(app().getExternalFilesDir("")?.absolutePath ?: app().filesDir.absolutePath)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            statFs.totalBytes
        } else {
            0L
        }
    }

    /**
     * 获取当前可用内存，返回数据以字节为单位。
     *
     * @param context 可传入应用程序上下文。
     * @return 当前可用内存单位为B。
     */
    fun getAvailableMemory(context: Context = app()): Long {
        val memoryInfo = getMemoryInfo(context)
        return memoryInfo.availMem
    }

    /**获取总内存大小*/
    fun getTotalMemory(context: Context = app()): Long {
        val memoryInfo = getMemoryInfo(context)
        return memoryInfo.totalMem
    }

    /**
     * 获取系统总内存
     *
     * @param context 可传入应用程序上下文。
     * @return 总内存大单位为B。
     */
    fun getTotalMemorySize(): Long {
        val dir = "/proc/meminfo"
        try {
            val fr = FileReader(dir)
            val br = BufferedReader(fr, 2048)
            val memoryLine = br.readLine()
            val subMemoryLine = memoryLine.substring(memoryLine.indexOf("MemTotal:"))
            br.close()
            return subMemoryLine.replace("\\D+".toRegex(), "").toInt() * 1024L
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return 0
    }

    fun getMemoryInfo(context: Context): ActivityManager.MemoryInfo {
        val am =
            context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        am.getMemoryInfo(memoryInfo)
        return memoryInfo
    }

    /**
     * SDCARD是否存
     */
    fun externalMemoryAvailable(): Boolean {
        return Environment.getExternalStorageState() ==
                Environment.MEDIA_MOUNTED
    }

    /**
     * 获取SDCARD总的存储空间
     *
     * @return
     */
    fun getTotalExternalMemorySize(): Long {
        return if (externalMemoryAvailable()) {
            val path = Environment.getExternalStorageDirectory()
            val stat = StatFs(path.path)
            val blockSize: Long
            val totalBlocks: Long
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                blockSize = stat.blockSizeLong
                totalBlocks = stat.blockCountLong
            } else {
                blockSize = stat.blockSize.toLong()
                totalBlocks = stat.blockCount.toLong()
            }
            totalBlocks * blockSize
        } else {
            -1
        }
    }

    /**
     * 获取SDCARD剩余存储空间 kb单位->KB->MB
     *
     * @return
     */
    fun getAvailableExternalMemorySize(): Long {
        return if (externalMemoryAvailable()) {
            val path = Environment.getExternalStorageDirectory()
            val stat = StatFs(path.path)
            val blockSize: Long
            val availableBlocks: Long
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                blockSize = stat.blockSizeLong
                availableBlocks = stat.availableBlocksLong
            } else {
                blockSize = stat.blockSize.toLong()
                availableBlocks = stat.availableBlocks.toLong()
            }
            availableBlocks * blockSize
        } else {
            -1
        }
    }

    /**设备信息*/
    fun deviceInfo(context: Context, builder: Appendable): Appendable {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        builder.appendLine("deviceId/psuedoID: $deviceId")
        builder.appendLine("androidId: $androidId")
        builder.appendLine("id: ${ID.id}")

        builder.appendLine()
        deviceInfoLess(builder)
        builder.appendLine()

        builder.append("memoryClass: ")
        builder.appendln(manager.memoryClass)
        builder.append("largeMemoryClass: ")
        builder.appendln(manager.largeMemoryClass)
        builder.appendLine()
        builder.append("手机内存大小:")
        builder.appendLine(Formatter.formatFileSize(context, getTotalMemorySize()))
        //        builder.appendLine();
//        builder.append("JVM可用内存大小:");
//        builder.appendLine(Formatter.formatFileSize(context, Runtime.getRuntime().maxMemory()));
//        builder.appendLine();
        val memoryInfo: ActivityManager.MemoryInfo = getMemoryInfo(context)
        builder.append("系统总内存:")
        builder.appendLine(Formatter.formatFileSize(context, memoryInfo.totalMem))
        builder.append("系统剩余内存:")
        builder.appendLine(Formatter.formatFileSize(context, memoryInfo.availMem))
        //        builder.append("是否内存警告:");
//        builder.appendLine(memoryInfo.lowMemory);
//        builder.append("阈值:");
//        builder.appendLine(Formatter.formatFileSize(context, memoryInfo.threshold));
//        builder.appendLine();
//        builder.append("getNativeHeapSize:");
//        builder.appendLine(Formatter.formatFileSize(context, Debug.getNativeHeapSize()));
//
//        builder.append("getNativeHeapAllocatedSize:");
//        builder.appendLine(Formatter.formatFileSize(context, Debug.getNativeHeapAllocatedSize()));
//
//        builder.append("getNativeHeapFreeSize:");
//        builder.appendLine(Formatter.formatFileSize(context, Debug.getNativeHeapFreeSize()));
        builder.appendLine()
        builder.append("SD空间大小:")
        builder.appendLine(Formatter.formatFileSize(context, getTotalExternalMemorySize()))
        builder.append("SD可用空间大小:")
        builder.appendLine(Formatter.formatFileSize(context, getAvailableExternalMemorySize()))
        return builder
    }

    fun deviceInfoLess(builder: Appendable, abi: Boolean = true, cpu: Boolean = true) {
        // 硬件制造商/品牌名称/型号/产品名称
        // OnePlus/OnePlus/ONEPLUS A6000/OnePlus6
        // [Build.MODEL] 最终用户可见的名称
        builder.append("API ${Build.VERSION.SDK_INT}/${Build.MANUFACTURER}/${Build.BRAND}/${Build.MODEL}/${Build.PRODUCT}")
        if (abi) {
            builder.appendLine()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder.append(Build.SUPPORTED_ABIS.connect("/"))
            } else {
                builder.append(Build.CPU_ABI)
                builder.append("/")
                builder.append(Build.CPU_ABI2)
            }
            if (CpuUtils.isCpu64) {
                builder.append("[64]")
            }
        }

        if (cpu) {
            //CPU信息
            // CpuUtils.getCpuCurFreq().forEach {
            //     builder.appendLine()
            //     builder.append(it)
            // }

            builder.appendLine()
            builder.append("${CpuUtils.cpuCoreNum}/${CpuUtils.numCpuCores} ${CpuUtils.cpuMinFreqInfo}Hz/${CpuUtils.cpuMinFreq}Hz/${CpuUtils.cpuMaxFreq}Hz")
        }
    }

    /**设备屏幕信息*/
    fun screenInfo(context: Context, builder: Appendable): Appendable {
        builder.apply {

            val decorView = (context as? Activity)?.window?.decorView
            val contentView = (context as? Activity)?.findViewById<View>(Window.ID_ANDROID_CONTENT)

            val displayMetrics = context.resources.displayMetrics
            val widthDp: Float = displayMetrics.widthPixels / displayMetrics.density
            val heightDp: Float = displayMetrics.heightPixels / displayMetrics.density

            // 屏幕尺寸
            val width = displayMetrics.widthPixels / displayMetrics.xdpi
            //displayMetrics.heightPixels / displayMetrics.ydpi

            val dvHeight = decorView?.measuredHeight ?: 0
            val dvWidth = decorView?.measuredWidth ?: 0

            val cvHeight = contentView?.measuredHeight ?: 0
            val cvWidth = contentView?.measuredWidth ?: 0

            val height =
                (decorView?.measuredHeight ?: displayMetrics.heightPixels) / displayMetrics.ydpi

            val x = width.toDouble().pow(2.0)
            val y = height.toDouble().pow(2.0)
            val screenInches = sqrt(x + y)

            append("wPx:").append(displayMetrics.widthPixels)
            append(" hPx:").append(displayMetrics.heightPixels)

            if (decorView != null) {
                append(" dw:").append(decorView.measuredWidth)
                append(" dh:").append(decorView.measuredHeight)
            }

            if (contentView != null) {
                append(" cw:").append(contentView.measuredWidth)
                append(" ch:").append(contentView.measuredHeight)
            }

            //dp值
            appendLine()
            append("wDp:").append(widthDp)
            append(" hDp:").append(heightDp)
            append(" dp:").append(displayMetrics.density)
            append(" sp:").append(displayMetrics.scaledDensity)
            append(" dpi:").appendln(displayMetrics.densityDpi)

            //多少寸, 屏幕寸数
            append("w:").append("%.02f".format(width))
            append(" h:").append("%.02f".format(height))
            append(" inches:").append("%.02f".format(screenInches))
            //导航栏, 状态栏高度
            val statusBarHeight = _statusBarHeight
            val navBarHeight = max(dvWidth - cvWidth, dvHeight - cvHeight)
            append(" sh:").append(statusBarHeight).append(" ")
                .append(statusBarHeight / displayMetrics.density).append("dp")
            append(" nh:").append(navBarHeight).append(" ")
                .append(navBarHeight / displayMetrics.density).append("dp").appendLine()

            //pad
            append(" pad:").append(Pad.isPadSize())
            append(" tablet:").append(Pad.isTabletDevice)
            append(" tw:").append(Pad.isTabletWindow())
            append(" magic:").append(Pad.inMagicWindow())
            append(" mw:").append(Pad.isInMultiWindowMode(lastContext))
            appendLine()

            val rect = Rect()
            val point = Point()
            if (decorView != null) {
                decorView.getGlobalVisibleRect(rect, point)
                append(" d:").append(rect)
                append(" d:").append(point).appendLine()
            }

            if (contentView != null) {
                contentView.getGlobalVisibleRect(rect, point)
                append(" c:").append(rect)
                append(" c:").append(point)

                appendLine()
                contentView.getWindowVisibleDisplayFrame(rect)
                append("frame:").append(rect)
            }

            //刷新率
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val windowManager =
                    context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                val modes = windowManager.defaultDisplay.supportedModes
                val first = modes.first()
                val count = modes.size
                appendLine()
                if (count > 1) {
                    append("1/${count} ")
                }
                append("w:${first.physicalWidth} h:${first.physicalHeight} fps:${first.refreshRate.toInt()} ")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    append(
                        "[${
                            first.alternativeRefreshRates.toList().connect { "${it.toInt()}" }
                        }]"
                    )
                }
            }
        }
        return builder
    }

    /**本地APK编译信息*/
    fun buildString(builder: Appendable): Appendable {
        builder.apply {
            append(getAppVersionName()).append("/").append("${getAppVersionCode()}")
            getAppString("user_name")?.let {
                append(" un:$it")
            }
            getAppString("os_name")?.let {
                append(" on:${it}")
                appendLine()
            }
            getAppString("build_time")?.let {
                append("bt:${it}")
                appendLine()
            }
        }
        return builder
    }

    /**常用想要的设备基础信息*/
    fun beautifyDeviceLog(builder: Appendable = StringBuilder()): String {
        deviceInfoLess(builder)
        builder.appendLine()
        builder.append("${getAppVersionName()}/${getAppVersionCode()}/${getAppString("build_time")}")
        builder.appendLine()
        return builder.toString()
    }

    /**
     * 需要权限:
     * android.permission.READ_PHONE_STATE
     * android.permission.READ_SMS.
     * @param context 上下文
     * @return 返回手机号码 tel number
     */
    @SuppressLint("MissingPermission")
    fun getTelNumber(context: Context): String? {
        val tm = context
            .getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return tm.line1Number
    }

    /** 是否使用代理(WiFi状态下的,避免被抓包) */
    fun isWifiProxy(context: Context = app()): Boolean {
        return !(proxyInfo(context).isNullOrBlank())
    }

    /**代理信息*/
    fun proxyInfo(context: Context = app()): String? {
        val isIcsOrLater = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH
        val proxyAddress: String?
        val proxyPort: Int
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                val cm: ConnectivityManager? =
                    context.getSystemService(ConnectivityManager::class.java)
                proxyAddress = cm?.defaultProxy?.host
                proxyPort = cm?.defaultProxy?.port ?: -1
            }
            isIcsOrLater -> {
                proxyAddress = System.getProperty("http.proxyHost")
                proxyPort = System.getProperty("http.proxyPort")?.toIntOrNull() ?: -1
            }
            else -> {
                proxyAddress = Proxy.getHost(context)
                proxyPort = Proxy.getPort(context)
            }
        }
        return proxyAddress?.run { "$this:$proxyPort" }
    }

    /** 是否正在使用VPN */
    fun isVpnUsed(): Boolean {
        return !vpnInfo().isNullOrBlank()
    }

    /**是否使用了代理Proxy*/
    fun isProxyUsed(context: Context = app()): Boolean {
        return !proxyInfo(context).isNullOrBlank()
    }

    /**vpn信息*/
    fun vpnInfo(): String? {
        var name: String? = null
        try {
            val enumerationList: Enumeration<NetworkInterface>? =
                NetworkInterface.getNetworkInterfaces()
            enumerationList?.run {
                while (hasMoreElements()) {
                    val network = nextElement()
                    if (!network.isUp || network.interfaceAddresses.size == 0) {
                        continue
                    }
                    //Log.d("-----", "isVpnUsed() NetworkInterface Name: " + intf.getName());
                    if ("tun0" == network.name || "ppp0" == network.name) {
                        //RUtils.saveToSDCard("proxy.log", "isVpnUsed:" + intf.name)
                        name = network.name// The VPN is up
                    }
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return name
    }

    /**手机设备性能登录, 越大性能越好*/
    fun performanceLevel(): Int {
        return when {
            CpuUtils.cpuMaxFreq >= 2_800_000L -> PERFORMANCE_HIGH
            CpuUtils.cpuMaxFreq >= 2_000_000L -> PERFORMANCE_MEDIUM
            CpuUtils.cpuMaxFreq >= 1_800_000L -> PERFORMANCE_LOW
            else -> PERFORMANCE_MIN
        }
    }
}

fun Appendable.append(value: Int): Appendable {
    return append(value.toString())
}

fun Appendable.append(value: Boolean): Appendable {
    return append(value.toString())
}

fun Appendable.append(value: Float): Appendable {
    return append(value.toString())
}

fun Appendable.append(value: Rect): Appendable {
    return append(value.toString())
}

fun Appendable.append(value: Point): Appendable {
    return append(value.toString())
}

fun Appendable.appendln(value: Int): Appendable {
    return appendLine(value.toString())
}

fun Appendable.appendln(value: Float): Appendable {
    return appendLine(value.toString())
}