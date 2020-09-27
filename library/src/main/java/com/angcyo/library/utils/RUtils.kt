package com.angcyo.library.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Looper
import android.os.Process.myUid
import android.telephony.TelephonyManager
import android.view.Surface
import com.angcyo.library.L
import com.angcyo.library.app
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

    /** 快速加群 */
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

    /**判断应用程序是否可以被debug*/
    fun isAppDebug(context: Context = app(), packageName: String = app().packageName): Boolean {
        try {
            val packageInfo = context.packageManager.getPackageInfo(packageName, 1)
            if (packageInfo != null) {
                val info = packageInfo.applicationInfo
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

    /**支持负数*/
    fun getLongNumFromStr(str: String?): Long? =
        str?.patternList("[-]?\\d+")?.firstOrNull()?.toLongOrNull()

    /**
     * "V>=0.89-.128 89.128"
     * 支持正负浮点数
     * */
    fun getFloatNumFromStr(str: String?): Float? =
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
 */
@SuppressLint("MissingPermission", "HardwareIds")
fun Context.getIMEI(): String? {
    var imei: String? = null
    try {
        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        if (telephonyManager != null) {
            imei = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                telephonyManager.imei
            } else {
                telephonyManager.deviceId
            }
        }
        //L.w("call: getIMEI([])-> " + imei);
    } catch (e: java.lang.Exception) {
        L.e("IMEI获取失败, 请检查权限:" + e.message)
        //e.printStackTrace();
        //L.e("call: getIMEI([])-> " + imei + " " + e.getMessage());
    }
    return imei
}

fun String.getSystemProperty() = RUtils.getSystemProperty(this)

fun String?.getLongNum() = RUtils.getLongNumFromStr(this)
fun String?.getFloatNum() = RUtils.getFloatNumFromStr(this)