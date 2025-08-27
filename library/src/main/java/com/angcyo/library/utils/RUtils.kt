package com.angcyo.library.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.GET_ACTIVITIES
import android.content.res.Configuration
import android.graphics.Point
import android.media.AudioManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Looper
import android.os.Process.myUid
import android.telephony.*
import android.util.DisplayMetrics
import android.view.Surface
import android.view.WindowManager
import com.angcyo.library.L
import com.angcyo.library.annotation.CallComplianceAfter
import com.angcyo.library.app
import com.angcyo.library.component.RNetwork.isWifiConnect
import com.angcyo.library.component.hawk.LibHawkKeys
import com.angcyo.library.ex.checkPermissions
import com.angcyo.library.ex.have
import com.angcyo.library.ex.patternList
import com.angcyo.library.toast
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.lang.reflect.Field
import java.lang.reflect.Method


/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/02
 */

object RUtils {

    fun chatQQIntent(context: Context, qq: String = "664738095"): Intent? {
        return if (context.checkApkExist("com.tencent.mobileqq")) {
            val url = "mqqwpa://im/chat?chat_type=wpa&uin=$qq"
            Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } else {
            L.w("未找到应用程序[com.tencent.mobileqq]")
            null
        }
    }

    /** qq咨询
     * 网页版: http://wpa.qq.com/msgrd?v=3&uin=664738095&site=qq&menu=yes
     * "http://wpa.qq.com/msgrd?v=3&uin=${app().memoryConfigBean.qq ?: "664738095"}&site=qq&menu=yes"
     * */
    fun chatQQ(context: Context, qq: String = "664738095"): Boolean {
        try {
            chatQQIntent(context, qq)?.run {
                context.startActivity(this)
                return true
            } ?: toast("您没有安装腾讯QQ")
        } catch (e: Exception) {
            e.printStackTrace()
            toast("您没有安装腾讯QQ")
        }
        return false
    }

    fun joinQQGroupIntent(
        context: Context,
        key: String = "TO1ybOZnKQHSLcUlwsVfOt6KQMGLmuAW"
    ): Intent? {
        return if (context.checkApkExist("com.tencent.mobileqq")) {
            val url =
                "mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D$key"
            Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } else {
            null
        }
    }

    /** 快速加群
     * 2017-5-15 274306954
     * 2022-07-26 830784469
     * */
    fun joinQQGroup(context: Context, key: String = "TO1ybOZnKQHSLcUlwsVfOt6KQMGLmuAW"): Boolean {
        // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面
        // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            joinQQGroupIntent(context, key)?.run {
                context.startActivity(this)
                return true
            } ?: toast("您没有安装腾讯QQ")
        } catch (e: Exception) { // 未安装手Q或安装的版本不支持
            toast("您没有安装腾讯QQ")
        }
        return false
    }

    /**跳转到抖音用户个人信息页*/
    fun toDyUserProfile(context: Context, userId: String = "58661599176"): Boolean {
        return if (context.checkApkExist("com.ss.android.ugc.aweme")) {
            val url = "snssdk1128://user/profile/${userId}"
            //snssdk1128://aweme/detail/作品id号

            //kwai://profile/123456 //快手
            Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(this)
            }
            true
        } else {
            toast("您没有安装抖音")
            false
        }
    }

    /**
     * 判断是否是主线程
     */
    fun isMainThread(): Boolean {
        return Looper.myLooper() == Looper.getMainLooper()
    }

    /**判断手机是否root
     * https://www.cnblogs.com/waylife/p/3846440.html*/
    fun isRoot(): Boolean {
        val paths = arrayOf(
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        )

        var result = false

        for (path in paths) {
            result = result || isSUExist(path)
            if (result) {
                break
            }
        }

        return try {
            result ||
                    (File("/system/app/Superuser.apk").exists()) ||
                    Build.TAGS?.contains("test-keys") == true
        } catch (e: Exception) {
            false
        }
    }

    fun isSUExist(path: String): Boolean {
        return File(path).exists() && isExecutable(path)
    }

    /**是否有可执行权限*/
    fun isExecutable(filePath: String): Boolean {
        var p: Process? = null
        try {
            p = Runtime.getRuntime().exec("ls -l $filePath")
            // 获取返回内容
            val `in` = BufferedReader(InputStreamReader(p.inputStream))
            val str: String? = `in`.readLine()
            L.i("isExecutable ", str)
            if (str != null && str.length >= 4) {
                val flag = str[3]
                if (flag == 's' || flag == 'x') return true
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            p?.destroy()
        }
        return false
    }

    /**判断是否root, 会弹窗*/
    fun isRootUI(): Boolean {
        return try {
            Runtime.getRuntime().exec("su").outputStream != null
        } catch (e: IOException) {
            L.w(e.message)
            false
        }
    }

    /**判断应用程序是否可以被debug
     * [ApplicationInfo.FLAG_DEBUGGABLE]*/
    fun isAppDebug(context: Context = app(), packageName: String = app().packageName): Boolean {
        try {
            if (LibHawkKeys.isCompliance) {
                @CallComplianceAfter
                val packageInfo = context.packageManager.getPackageInfo(packageName, GET_ACTIVITIES)
                if (packageInfo != null) {
                    val info = packageInfo.applicationInfo
                    return (info?.flags ?: 0) and ApplicationInfo.FLAG_DEBUGGABLE != 0
                }
            } else {
                val info = context.applicationInfo
                return info.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
            }
        } catch (e: Exception) {
        }
        return false
    }

    /**
     * 通过主动抛出异常，检查堆栈信息来判断是否存在XP框架
     *
     * @return
     */
    fun isXposedExistByThrow(): Boolean {
        return try {
            throw Exception("gg")
        } catch (e: Exception) {
            for (stackTraceElement in e.stackTrace) {
                if (stackTraceElement.className.contains("de.robv.android.xposed.XposedBridge")) return true
            }
            false
        }
    }

    /**
     * 判断应用是否多开
     * 检测原始的包名，多开应用会hook处理getPackageName方法
     * 顺着这个思路，如果在应用列表里出现了同样的包，那么认为该应用被多开了
     *
     * @param context
     * @param callback
     * @return
     */
    fun checkByOriginApkPackageName(context: Context = app(), packageName: String): Boolean {
        try {
            var count = 0
            val pm = context.packageManager
            val pkgs = pm.getInstalledPackages(0)
            for (info in pkgs) {
                if (packageName == info.packageName) {
                    count++
                }
            }
            return count > 1
        } catch (ignore: Exception) {
        }
        return false
    }

    /**根据包名, 获取对应的[base.apk]文件对象.
     * 请使用[android.content.pm.ApplicationInfo.sourceDir]
     * 获取到的路径一样
     * 关闭 FileProvider
     * */
    fun getApkFileByPackageName(packageName: String): File? {
        var path = ""
        var file: File? = null
        for (i in 0..10) {
            path = if (i == 0) {
                "/data/app/${packageName}/base.apk"
            } else {
                "/data/app/${packageName}-${i}/base.apk"
            }
            val temp = File(path)

            if (temp.exists()) {
                file = temp
                break
            }
        }
        return file
    }

    /**
     * 小米手机"后台弹出界面(允许应用在后台弹出界面)"权限问题解决方案
     * https://blog.csdn.net/shenshibaoma/article/details/103909618
     * */
    fun isMIUIBackgroundAllowed(context: Context = app()): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return try {
                val ops = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager

                val field: Field =
                    AppOpsManager::class.java.getField("OP_BACKGROUND_START_ACTIVITY")
                field.isAccessible = true
                val opValue = field.get(ops) as Int

                val op = opValue
                val method: Method = ops::class.java.getMethod(
                    "checkOpNoThrow",
                    Int::class.java, Int::class.java, String::class.java
                )
                val result = method.invoke(ops, op, myUid(), context.packageName) as Int
                result == AppOpsManager.MODE_ALLOWED
            } catch (e: Exception) {
                L.e("not support")
                false
            }
        }
        return true
    }

    /**通过命令获取系统属性
     * https://www.jianshu.com/p/7a8eb5cc35b0
     * */
    fun getSystemProperty(propName: String): String? {
        val line: String
        var input: BufferedReader? = null
        try {
            val p = Runtime.getRuntime().exec("getprop $propName")
            input = BufferedReader(InputStreamReader(p.inputStream), 1024)
            line = input.readLine()
            input.close()
        } catch (ex: IOException) {
            L.i("Unable to read sysprop $propName", ex)
            return null
        } finally {
            if (input != null) {
                try {
                    input.close()
                } catch (e: IOException) {
                    L.i("Exception while closing InputStream", e)
                }
            }
        }
        return line
    }

    /**获取miui系统版本号
     * V5 V7 V12*/
    fun getMIUIVersion(): Long? {
        return getLongNumFromStr("ro.miui.ui.version.name".getSystemProperty())
    }

    /**支持负数
     * [str] 需要提取的字符串
     * [positive] 是否只获取正数, 否则会支持负数*/
    fun getLongNumFromStr(str: String?, positive: Boolean = false): Long? = if (positive)
        str?.patternList("\\d+")?.firstOrNull()?.toLongOrNull()
    else
        str?.patternList("[-]?\\d+")?.firstOrNull()?.toLongOrNull()

    /**
     * "V>=0.89-.128 89.128"
     * 支持正负浮点数
     * [str] 需要提取的字符串
     * [positive] 是否只获取正数, 否则会支持负数
     * */
    fun getFloatNumFromStr(str: String?, positive: Boolean = false): Float? = if (positive)
        str?.patternList("[\\d.]*\\d+")?.firstOrNull()?.toFloatOrNull()
    else
        str?.patternList("[-]?[\\d.]*\\d+")?.firstOrNull()?.toFloatOrNull()

    /**
     * 修复:
     * java.util.concurrent.TimeoutException: com.android.internal.os.BinderInternal$GcWatcher.finalize() timed out after 10 seconds
     * https://stackoverflow.com/questions/24021609/how-to-handle-java-util-concurrent-timeoutexception-android-os-binderproxy-fin
     * */
    fun fixFinalizerWatchdogDaemon() {
        try {
            val clazz = Class.forName("java.lang.Daemons\$FinalizerWatchdogDaemon")
            val method = clazz.superclass!!.getDeclaredMethod("stop")
            method.isAccessible = true
            val field = clazz.getDeclaredField("INSTANCE")
            field.isAccessible = true
            method.invoke(field[null])
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    /**获取手机信号强度
     * 4G网络 最佳范围 >-90dBm 越大越好
     * 3G网络最佳范围  >-90dBm  越大越好  ps:中国移动3G获取不到  返回的无效dbm值是正数（85dbm）
     * 在这个范围的已经确定是3G，但不同运营商的3G有不同的获取方法，故在此需做判断 判断运营商与网络类型的工具类在最下方
     * 2G网络最佳范围>-90dBm 越大越好
     *
     * -50dBm~0dBm范围内，恭喜你，你的信号已经好得很了。话说你就站在基站旁边是吧，哈
     * -90dBm~-60dBm，同样恭喜你，你基本不会面临打不了电话的问题。如果打不了的，找运营商吧，那是他们的问题。
     * https://blog.csdn.net/pan0755/article/details/78437249
     * */
    @SuppressLint("MissingPermission")
    fun getMobileDbm(context: Context): Int {
        var dbm = -1
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            try {
                val cellInfoList = tm.allCellInfo
                if (null != cellInfoList) {
                    for (cellInfo in cellInfoList) {
                        L.d(cellInfo)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && cellInfo is CellInfoTdscdma) {
                            dbm = cellInfo.cellSignalStrength.dbm
                        } else if (cellInfo is CellInfoGsm) {
                            val cellSignalStrength = cellInfo.cellSignalStrength
                            dbm = cellSignalStrength.dbm
                            //L.i("cellSignalStrengthGsm→$cellSignalStrength")
                        } else if (cellInfo is CellInfoCdma) {
                            val cellSignalStrength = cellInfo.cellSignalStrength
                            dbm = cellSignalStrength.dbm
                            //L.i("cellSignalStrengthCdma→$cellSignalStrength")
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && cellInfo is CellInfoWcdma) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                                val cellSignalStrength = cellInfo.cellSignalStrength
                                dbm = cellSignalStrength.dbm
                                //L.i("cellSignalStrengthWcdma→$cellSignalStrength")
                            }
                        } else if (cellInfo is CellInfoLte) {
                            val cellSignalStrength = cellInfo.cellSignalStrength
                            dbm = cellSignalStrength.dbm
                            //L.i("cellSignalStrengthLte.getAsuLevel()\t" + cellSignalStrength.asuLevel)
                            //L.i("cellSignalStrengthLte.getDbm()\t " + cellSignalStrength.dbm)
                            //L.i("cellSignalStrengthLte.getLevel()\t " + cellSignalStrength.level)
                            //L.i("cellSignalStrengthLte.getTimingAdvance()\t " + cellSignalStrength.timingAdvance)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                //L.i("cellSignalStrengthLte.getCqi()\t" + cellSignalStrength.cqi)
                                //L.i("cellSignalStrengthLte.getRsrq()\t " + cellSignalStrength.rsrq)
                                //L.i("cellSignalStrengthLte.getRsrp()\t " + cellSignalStrength.rsrp)
                                //L.i("cellSignalStrengthLte.getRssnr()\t " + cellSignalStrength.rssnr)
                            }
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && cellInfo is CellInfoNr) {
                            val cellSignalStrengthNr = cellInfo.cellSignalStrength
                            dbm = cellSignalStrengthNr.dbm
                            //L.i(cellSignalStrengthNr)
                        }
                        break
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        /*val listener: PhoneStateListener = object : PhoneStateListener() {
            override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
                */
        /**
         * {mCdma=CellSignalStrengthCdma:
         * cdmaDbm=2147483647 cdmaEcio=2147483647 evdoDbm=2147483647 evdoEcio=2147483647 evdoSnr=2147483647
         * level=0 oplevel=0,mGsm=CellSignalStrengthGsm: rssi=2147483647 ber=2147483647 mTa=2147483647 mLevel=0,
         *
         * mWcdma=CellSignalStrengthWcdma: ss=2147483647 ber=2147483647 rscp=2147483647 ecno=2147483647 level=0 oplevel=0,
         *
         * mTdscdma=CellSignalStrengthTdscdma: rssi=2147483647 ber=2147483647 rscp=2147483647 level=0,
         *
         * mLte=CellSignalStrengthLte: rssi=-71 rsrp=-95 rsrq=-8 rssnr=48 cqi=2147483647 ta=2147483647 level=4 oplevel=4,
         *
         * mNr=CellSignalStrengthNr:{ csiRsrp = 2147483647 csiRsrq = 2147483647 csiSinr = 2147483647 ssRsrp = 2147483647 ssRsrq = 2147483647 ssSinr = 2147483647 level = 0 },
         *
         * primary=CellSignalStrengthLte,voice level =4,data level =4,isGsm =true}
         * *//*
                // 0表示非常差的信号强度，而4表示非常强的信号强度
                //L.w(signalStrength.level)
                L.w(signalStrength)
            }
        }
        tm.listen(listener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS)*/
        return dbm
    }

    /**获取wifi信号强度*/
    fun getWifiRssi(context: Context): Int {
        var rssi = -1
        if (isWifiConnect(context)) {
            val mWifiInfo =
                (context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager).connectionInfo
            rssi = mWifiInfo.rssi //获取wifi信号强度
            if (rssi > -50 && rssi < 0) { //最强
                L.i("wifi 最强")
            } else if (rssi > -70 && rssi < -50) { //较强
                L.i("wifi 较强")
            } else if (rssi > -80 && rssi < -70) { //较弱
                L.i("wifi 较弱")
            } else if (rssi > -100 && rssi < -80) { //微弱
                L.i("wifi 微弱")
            }
        } else {
            //无连接
            L.i("无wifi连接")
        }
        return rssi
    }

    /**获取手机设备的宽度*/
    fun getDeviceWidth(): Int {
        val wm = app().getSystemService(Context.WINDOW_SERVICE) as WindowManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            val point = Point()
            wm.defaultDisplay.getRealSize(point)
            return point.x
        } else {
            val dm = DisplayMetrics()
            wm.defaultDisplay.getMetrics(dm)
            val width = dm.widthPixels
            val height = dm.heightPixels
            return height
        }
    }

    fun getDeviceHeight(): Int {
        val wm = app().getSystemService(Context.WINDOW_SERVICE) as WindowManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            val point = Point()
            wm.defaultDisplay.getRealSize(point)
            return point.y
        } else {
            val dm = DisplayMetrics()
            wm.defaultDisplay.getMetrics(dm)
            val width = dm.widthPixels
            val height = dm.heightPixels
            return height
        }
    }
}

/** 检查APK是否安装 */
fun Context.checkApkExist(packageName: String?): Boolean {
    return if (packageName.isNullOrBlank()) {
        false
    } else try {
        val packageManager = packageManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            packageManager.getApplicationInfo(
                packageName,
                PackageManager.MATCH_UNINSTALLED_PACKAGES
            )
        } else {
            packageManager.getApplicationInfo(
                packageName,
                PackageManager.GET_UNINSTALLED_PACKAGES
            )
        }
        true
    } catch (e: Exception) {
        false
    }
}

/**[Activity]返回状态*/
fun Int.resultString(): String {
    return when (this) {
        Activity.RESULT_OK -> "RESULT_OK"
        Activity.RESULT_CANCELED -> "RESULT_CANCELED"
        Activity.RESULT_FIRST_USER -> "RESULT_FIRST_USER"
        else -> "UNKNOWN"
    }
}

/**[Activity]返回状态*/
fun Int.isResultOk(): Boolean = this == Activity.RESULT_OK

/**音频焦点*/
fun Int.audioFocusString(): String {
    return when (this) {
        AudioManager.AUDIOFOCUS_GAIN -> "AudioManager.AUDIOFOCUS_GAIN"
        AudioManager.AUDIOFOCUS_LOSS -> "AudioManager.AUDIOFOCUS_LOSS"
        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> "AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK"
        else -> "UNKNOWN"
    }
}

/**屏幕方向, 横屏 or 竖屏*/
fun Int.orientationString(): String {
    return when (this) {
        Configuration.ORIENTATION_LANDSCAPE -> "LANDSCAPE"
        Configuration.ORIENTATION_PORTRAIT -> "PORTRAIT"
        Configuration.ORIENTATION_UNDEFINED -> "UNDEFINED"
        Configuration.ORIENTATION_SQUARE -> "SQUARE"
        else -> "UNKNOWN"
    }
}

/**屏幕旋转角度*/
fun Int.rotationString(): String {
    return when (this) {
        Surface.ROTATION_0 -> "ROTATION_0"
        Surface.ROTATION_90 -> "ROTATION_90"
        Surface.ROTATION_180 -> "ROTATION_180"
        Surface.ROTATION_270 -> "ROTATION_270"
        else -> "UNKNOWN"
    }
}

/**
 * 获取设备唯一标识码, 需要权限 android.permission.READ_PHONE_STATE
 * 868938012791119
 *
 * IMEI(International Mobile Equipment Identity) 是国际移动设备身份码的缩写
 * IMEI是联通移动手机的标识，MEID是电信手机的标识。
 *
 * https://www.jianshu.com/p/e714e42483ba
 *
 * AndroidQ 已经拿不到IMEI/MEDI了
 * https://stackoverflow.com/questions/55173823/i-am-getting-imei-null-in-android-q
 */
@SuppressLint("MissingPermission", "HardwareIds")
fun Context.getIMEI(
    requestPermission: Boolean = false,
    slotIndex: Int = Int.MIN_VALUE,
    log: Boolean = true
): String? {
    var imei: String? = null
    try {
        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        if (telephonyManager != null) {
            imei = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (slotIndex != Int.MIN_VALUE) {
                    telephonyManager.getImei(slotIndex)
                } else {
                    telephonyManager.imei
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (slotIndex != Int.MIN_VALUE) {
                        telephonyManager.getDeviceId(slotIndex)
                    } else {
                        telephonyManager.deviceId
                    }
                } else {
                    telephonyManager.deviceId
                }
            }
        }
        //L.w("call: getIMEI([])-> " + imei);
        //L.w("call: getIMEI([])-> " + imei);
    } catch (e: SecurityException) {
        //The user 10198 does not meet the requirements to access device identifiers.
        if (log) {
            L.e("IMEI获取失败, 请检查权限:" + e.message)
        }
        if (requestPermission) {
            checkPermissions(Manifest.permission.READ_PHONE_STATE)
        }
    } catch (e: java.lang.Exception) {
        if (log) {
            e.printStackTrace()
        }
        //L.e("call: getIMEI([])-> " + imei + " " + e.getMessage());
    }
    return imei
}

fun String.getSystemProperty() = RUtils.getSystemProperty(this)


///**[positive] 是否只获取正数, 否则会支持负数*/
//fun String?.getLongNum(positive: Boolean = false) = RUtils.getLongNumFromStr(this, positive)
//
///**[positive] 是否只获取正数, 否则会支持负数*/
//fun String?.getFloatNum(positive: Boolean = false) = RUtils.getFloatNumFromStr(this, positive)

/**
 * 从字符串中, 获取正负整数
 * [positive] 是否只获取正数, 否则会支持负数*/
fun String?.getLongNum(positive: Boolean = false) = getLongNumList(positive)?.firstOrNull()

fun String?.getLongNumStringList(positive: Boolean = false) = (if (positive)
    this?.patternList("\\d+") else this?.patternList("[-]?\\d+"))?.run {
    val result = mutableListOf<String>()
    for (str in this) {
        str.toLongOrNull()?.let {
            result.add(str)
        }
    }
    result
}

fun String?.getLongNumList(positive: Boolean = false) = (if (positive)
    this?.patternList("\\d+")
else
    this?.patternList("[-]?\\d+"))?.run {
    val result = mutableListOf<Long>()
    for (str in this) {
        str.toLongOrNull()?.let {
            result.add(it)
        }
    }
    result
}

/**
 * 从字符串中, 获取正负浮点数
 * [positive] 是否只获取正数, 否则会支持负数*/
fun String?.getFloatNum(positive: Boolean = false) = getFloatNumList(positive)?.firstOrNull()

fun String?.getFloatNumList(positive: Boolean = false) = (if (positive)
    this?.patternList("[\\d.]*\\d+")
else
    this?.patternList("[-]?[\\d.]*\\d+"))?.run {
    val result = mutableListOf<Float>()
    for (str in this) {
        str.toFloatOrNull()?.let {
            result.add(it)
        }
    }
    result
}

/**从字符串中获取回去http地址, url地址*/
fun String?.getUrlList(regex: String = PATTERN_URL): List<String>? = this?.patternList(regex)

//region ---canvas---

/**是否是GCode内容*/
fun String.isGCodeContent() =
    have("(G90)|(G91)\\s*(G20)|(G21)") || have("(G20)|(G21)\\s*(G90)|(G91)")

/**是否是GCode内容*/
fun String.isSvgContent() = have("</svg>")

//endregion ---canvas---
