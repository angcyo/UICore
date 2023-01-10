package com.angcyo.library.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Point
import android.net.Uri
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Environment
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.telephony.TelephonyManager.PHONE_TYPE_CDMA
import android.telephony.TelephonyManager.PHONE_TYPE_GSM
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.angcyo.library.L
import com.angcyo.library.app
import com.angcyo.library.ex.md5
import java.io.*
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.net.NetworkInterface
import java.util.*


/**
 * 获取设备唯一id
 *
 * https://github.com/z244370114/DeviceLibray
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/12/01
 */
object ID {

    private var _id: String? = null
    private var deviceName = "/.device.txt"
    private val pathName: String
        get() = getRootPathExternalFirst() + "/.angcyo/${app().packageName}/.device"

    /**设备唯一id*/
    val id: String
        get() {
            if (_id.isNullOrBlank()) {
                if (isFileExists(pathName + deviceName)) {
                    _id = File(pathName + deviceName).readText()
                    if (_id.isNullOrBlank()) {
                        _id = getDeviceId()
                    }
                } else {
                    _id = getDeviceId()
                    writeTxtToFile(_id, pathName, deviceName)
                }
            }
            return if (_id.isNullOrBlank()) Device.androidId else _id ?: ""
        }

    private fun isFileExists(filePath: String?): Boolean {
        var file: File? = null
        if (!filePath.isNullOrBlank()) {
            file = File(filePath)
        }
        if (file == null) return false
        return if (file.exists()) {
            true
        } else isFileExistsApi29(filePath)
    }

    private fun isFileExistsApi29(filePath: String?): Boolean {
        if (Build.VERSION.SDK_INT >= 29) {
            try {
                val uri: Uri = Uri.parse(filePath)
                val cr: ContentResolver = app().contentResolver
                val afd = cr.openAssetFileDescriptor(uri, "r") ?: return false
                try {
                    afd.close()
                } catch (ignore: IOException) {
                }
            } catch (e: FileNotFoundException) {
                return false
            }
            return true
        }
        return false
    }

    private fun getRootPathExternalFirst(): String? {
        var rootPath: String? = getExternalStoragePath()
        //        String rootPath = getExternalAppObbPath()
        if (rootPath.isNullOrBlank()) {
            rootPath = getRootPath()
        }
        return rootPath
    }

    /**
     * 将字符串写入到文本文件中
     *
     * @param content
     * @param filePath
     * @param fileName
     */
    private fun writeTxtToFile(content: String?, filePath: String, fileName: String) {
        try {
            if (isHaveSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                makeFilePath(filePath, fileName)
                content ?: return
                val outputStream = FileOutputStream(filePath + fileName)
                outputStream.write(content.toByteArray())
                outputStream.close()
            }
        } catch (e: Exception) {
        }
    }


    /**
     * 生成文件
     *
     * @param filePath
     * @param fileName
     * @return
     */
    private fun makeFilePath(filePath: String, fileName: String): File? {
        var file: File? = null
        try {
            makeRootDirectory(filePath)
            file = File(filePath + fileName)
            if (!file.exists()) {
                file.createNewFile()
            }
        } catch (e: IOException) {
            //no op
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return file
    }

    /**
     * 生成文件夹
     *
     * @param filePath
     */
    private fun makeRootDirectory(filePath: String?) {
        filePath ?: return
        var file: File? = null
        try {
            file = File(filePath)
            //不存在就新建
            if (!file.exists()) {
                file.mkdir()
            }
        } catch (e: Exception) {
        }
    }

    /**
     * Return the path of /storage/emulated/0.
     *
     * @return the path of /storage/emulated/0
     */
    private fun getExternalStoragePath(): String? {
        return if (!isSDCardEnableByEnvironment()) "" else Environment.getExternalStorageDirectory()?.absolutePath
    }

    private fun isSDCardEnableByEnvironment(): Boolean {
        return Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()
    }

    /**
     * Return the path of /system.
     *
     * @return the path of /system
     */
    private fun getRootPath(): String? = Environment.getRootDirectory().absolutePath

    private fun getDeviceId(): String? {
        try {
            val stringBuffer = StringBuffer()
            var meid = ""
            var imei = ""
            var serial: String? = ""
            var mac: String? = ""
            var manufacturer: String? = ""
            if (getJudgeSIMCount() == 2) {
                meid = getMeId() ?: ""
                if (meid.isBlank()) {
                    imei = getIMEI(0) ?: ""
                    stringBuffer.append(imei).append("/")
                } else {
                    stringBuffer.append(meid).append("/")
                }
            } else {
                imei = getIMEI(0) ?: ""
                stringBuffer.append(imei).append("/")
            }
            serial = getSerialNumbers()
            manufacturer = Build.MANUFACTURER
            if (serial.isNullOrBlank()) {
                mac = getMacAddress()
                stringBuffer.append(mac).append("/")
            } else {
                stringBuffer.append(serial).append("/")
            }
            stringBuffer.append(manufacturer).append("/")
            stringBuffer.append(Build.BRAND).append("/")
            stringBuffer.append(Build.DEVICE).append("/")
            stringBuffer.append(Build.HARDWARE).append("/")
            stringBuffer.append(Build.MODEL).append("/")
            stringBuffer.append(Build.PRODUCT).append("/")
            stringBuffer.append(Build.TAGS).append("/")
            stringBuffer.append(Build.TYPE).append("/")
            stringBuffer.append(Build.USER).append("/")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                stringBuffer.append(Build.SUPPORTED_ABIS[0]).append("/")
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                stringBuffer.append(getResolutions()).append("/")
            }
            stringBuffer.append(getScreenDensity()).append("/")
            stringBuffer.append(getScreenDensityDpi())
            return stringBuffer.toString().md5()
        } catch (e: Exception) {
            return ""
        }
    }

    /**
     * 获取屏幕宽高
     *
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun getResolutions(): String {
        //获取真实屏幕宽高
        val outSize = Point()
        val wm = app().getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.defaultDisplay.getRealSize(outSize)
        val x: Int = outSize.x
        val y: Int = outSize.y
        return "$x*$y"
    }

    private fun getScreenDensity(): Float {
        return Resources.getSystem().displayMetrics.density
    }

    private fun getScreenDensityDpi(): Int {
        return Resources.getSystem().displayMetrics.densityDpi
    }

    /**
     * @return 获取当前SIM卡数量
     */
    @SuppressLint("MissingPermission")
    private fun getJudgeSIMCount(): Int {
        if (ActivityCompat.checkSelfPermission(
                app(),
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return 0
        }
        var count = 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            count = SubscriptionManager.from(app()).activeSubscriptionInfoCount
            return count
        }
        return count
    }

    /**
     * 获取手机序列号
     *
     * @return 手机序列号
     */
    @SuppressLint("MissingPermission")
    private fun getSerialNumbers(): String? {
        var serial: String? = ""
        try {
            serial = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) { //9.0+
                Build.getSerial()
            } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) { //8.0+
                Build.SERIAL
            } else { //8.0-
                val c = Class.forName("android.os.SystemProperties")
                val get = c.getMethod("get", String::class.java)
                get.invoke(c, "ro.serialno") as String
            }
        } catch (e: Exception) {
            //e.printStackTrace()
            L.e("读取设备序列号异常：$e")
        }
        return serial
    }

    /**
     * 隐私政策: 2023-1-10
     * @param slotId slotId为卡槽Id，它的值为 0、1；
     * @return
     */
    private fun getIMEI(slotId: Int): String? {
        return null
        /*return try {
            val manager = getTelephonyManager()
            val method =
                manager.javaClass.getMethod("getImei", Int::class.javaPrimitiveType)
            method.invoke(manager, slotId) as String
        } catch (e: java.lang.Exception) {
            null
        }*/
    }

    private fun getMeId(): String? {
        return getImeiOrMeId(false)
    }

    private fun isHaveSelfPermission(permission: String): Boolean {
        return (ActivityCompat.checkSelfPermission(
            app(),
            permission
        ) == PackageManager.PERMISSION_GRANTED)
    }

    private fun getTelephonyManager(): TelephonyManager {
        return app().getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    }

    private fun getMinOne(s0: String?, s1: String?): String {
        val empty0: Boolean = s0.isNullOrEmpty()
        val empty1: Boolean = s1.isNullOrEmpty()
        if (empty0 && empty1) return ""
        if (!empty0 && !empty1) {
            return if (s0!! <= s1!!) {
                s0
            } else {
                s1
            }
        }
        return if (!empty0) s0!! else s1!!
    }

    private fun getSystemPropertyByReflect(key: String): String? {
        try {
            @SuppressLint("PrivateApi") val clz = Class.forName("android.os.SystemProperties")
            val getMethod: Method = clz.getMethod("get", String::class.java, String::class.java)
            return getMethod.invoke(clz, key, "") as String?
        } catch (e: java.lang.Exception) { /**/
        }
        return ""
    }

    @SuppressLint("MissingPermission")
    private fun getImeiOrMeId(isImei: Boolean): String {
        if (!isHaveSelfPermission(Manifest.permission.READ_PHONE_STATE)) {
            return ""
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return ""
        }
        val tm: TelephonyManager = getTelephonyManager()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return if (isImei) {
                getMinOne(tm.getImei(0), tm.getImei(1))
            } else {
                getMinOne(tm.getMeid(0), tm.getMeid(1))
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val ids: String? =
                getSystemPropertyByReflect(if (isImei) "ril.gsm.imei" else "ril.cdma.meid")
            if (!ids.isNullOrBlank()) {
                val idArr = ids.split(",").toTypedArray()
                return if (idArr.size == 2) {
                    getMinOne(idArr[0], idArr[1])
                } else {
                    idArr[0]
                }
            }
            var id0 = tm.deviceId
            var id1: String? = null
            try {
                val method: Method =
                    tm.javaClass.getMethod("getDeviceId", Int::class.javaPrimitiveType)
                id1 = method.invoke(tm, if (isImei) PHONE_TYPE_GSM else PHONE_TYPE_CDMA) as String
            } catch (e: NoSuchMethodException) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            } catch (e: InvocationTargetException) {
                e.printStackTrace()
            }
            if (isImei) {
                if (id0 != null && id0.length < 15) {
                    id0 = ""
                }
                if (id1 != null && id1.length < 15) {
                    id1 = ""
                }
            } else {
                if (id0 != null && id0.length == 14) {
                    id0 = ""
                }
                if (id1 != null && id1.length == 14) {
                    id1 = ""
                }
            }
            return getMinOne(id0, id1)
        } else {
            val deviceId = tm.deviceId
            if (isImei) {
                if (deviceId != null && deviceId.length >= 15) {
                    return deviceId
                }
            } else {
                if (deviceId != null && deviceId.length == 14) {
                    return deviceId
                }
            }
        }
        return ""
    }

    //---

    /**
     * 获取MAC地址
     *
     * @return
     */
    private fun getMacAddress(): String? {
        var mac: String? = "02:00:00:00:00:00"
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            mac = getMacDefault()
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            mac = getMacAddresss()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mac = getMacFromHardware()
        }
        return mac
    }


    /**
     * Android  6.0 之前（不包括6.0）
     * 必须的权限  <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"></uses-permission>
     *
     * @return
     */
    @SuppressLint("MissingPermission")
    private fun getMacDefault(): String? {
        var mac = "02:00:00:00:00:00"
        val wifi = app().applicationContext
            .getSystemService(Context.WIFI_SERVICE) as WifiManager ?: return mac
        var info: WifiInfo? = null
        try {
            info = wifi.connectionInfo
        } catch (e: java.lang.Exception) {
        }
        if (info == null) {
            return null
        }
        mac = info.macAddress
        if (mac.isNotEmpty()) {
            mac = mac.uppercase(Locale.ENGLISH)
        }
        return mac
    }

    /**
     * Android 6.0（包括） - Android 7.0（不包括）
     *
     * @return
     */
    private fun getMacAddresss(): String {
        var WifiAddress = "02:00:00:00:00:00"
        try {
            WifiAddress =
                BufferedReader(FileReader(File("/sys/class/net/wlan0/address"))).readLine()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return WifiAddress
    }


    /**
     * 遍历循环所有的网络接口，找到接口是 wlan0
     * 必须的权限 <uses-permission android:name="android.permission.INTERNET"></uses-permission>
     *
     * @return
     */
    private fun getMacFromHardware(): String {
        try {
            val all: List<NetworkInterface> =
                Collections.list(NetworkInterface.getNetworkInterfaces())
            for (nif in all) {
                if (!nif.name.equals("wlan0", true)) continue
                val macBytes: ByteArray = nif.hardwareAddress ?: return ""
                val res1 = StringBuilder()
                for (b in macBytes) {
                    res1.append(String.format("%02X:", b))
                }
                if (res1.isNotEmpty()) {
                    res1.deleteCharAt(res1.length - 1)
                }
                return res1.toString()
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return "02:00:00:00:00:00"
    }


}