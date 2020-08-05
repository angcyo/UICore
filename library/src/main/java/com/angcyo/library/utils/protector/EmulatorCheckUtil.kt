package com.angcyo.library.utils.protector

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.text.TextUtils
import com.angcyo.library.app

/**
 * https://github.com/lamster2018/EasyProtector
 * Project Name:EasyProtector
 * Package Name:com.lahm.library
 * Created by lahm on 2018/6/8 15:01 .
 */
class EmulatorCheckUtil private constructor() {

    fun readSysProperty(
        context: Context? = app(),
        callback: EmulatorCheckCallback? = null
    ): Boolean {

        requireNotNull(context) { "context must not be null" }
        var suspectCount = 0

        //检测硬件名称
        val hardwareResult =
            checkFeaturesByHardware()
        when (hardwareResult.result) {
            CheckResult.RESULT_MAYBE_EMULATOR -> ++suspectCount
            CheckResult.RESULT_EMULATOR -> {
                callback?.findEmulator("hardware = " + hardwareResult.value)
                return true
            }
        }

        //检测渠道
        val flavorResult = checkFeaturesByFlavor()
        when (flavorResult.result) {
            CheckResult.RESULT_MAYBE_EMULATOR -> ++suspectCount
            CheckResult.RESULT_EMULATOR -> {
                callback?.findEmulator("flavor = " + flavorResult.value)
                return true
            }
        }

        //检测设备型号
        val modelResult = checkFeaturesByModel()
        when (modelResult.result) {
            CheckResult.RESULT_MAYBE_EMULATOR -> ++suspectCount
            CheckResult.RESULT_EMULATOR -> {
                callback?.findEmulator("model = " + modelResult.value)
                return true
            }
        }

        //检测硬件制造商
        val manufacturerResult =
            checkFeaturesByManufacturer()
        when (manufacturerResult.result) {
            CheckResult.RESULT_MAYBE_EMULATOR -> ++suspectCount
            CheckResult.RESULT_EMULATOR -> {
                callback?.findEmulator("manufacturer = " + manufacturerResult.value)
                return true
            }
        }

        //检测主板名称
        val boardResult = checkFeaturesByBoard()
        when (boardResult.result) {
            CheckResult.RESULT_MAYBE_EMULATOR -> ++suspectCount
            CheckResult.RESULT_EMULATOR -> {
                callback?.findEmulator("board = " + boardResult.value)
                return true
            }
        }

        //检测主板平台
        val platformResult =
            checkFeaturesByPlatform()
        when (platformResult.result) {
            CheckResult.RESULT_MAYBE_EMULATOR -> ++suspectCount
            CheckResult.RESULT_EMULATOR -> {
                callback?.findEmulator("platform = " + platformResult.value)
                return true
            }
        }

        //检测基带信息
        val baseBandResult =
            checkFeaturesByBaseBand()
        when (baseBandResult.result) {
            CheckResult.RESULT_MAYBE_EMULATOR -> suspectCount += 2 //模拟器基带信息为null的情况概率相当大
            CheckResult.RESULT_EMULATOR -> {
                callback?.findEmulator("baseBand = " + baseBandResult.value)
                return true
            }
        }

        //检测传感器数量
        val sensorNumber = getSensorNumber(context)
        if (sensorNumber <= 7) ++suspectCount

        //检测已安装第三方应用数量
        val userAppNumber = userAppNumber
        if (userAppNumber <= 5) ++suspectCount

        //检测是否支持闪光灯
        val supportCameraFlash = supportCameraFlash(context)
        if (!supportCameraFlash) ++suspectCount
        //检测是否支持相机
        val supportCamera = supportCamera(context)
        if (!supportCamera) ++suspectCount
        //检测是否支持蓝牙
        val supportBluetooth = supportBluetooth(context)
        if (!supportBluetooth) ++suspectCount

        //检测光线传感器
        val hasLightSensor = hasLightSensor(context)
        if (!hasLightSensor) ++suspectCount

        //检测进程组信息
        val cgroupResult = checkFeaturesByCgroup()
        if (cgroupResult.result == CheckResult.RESULT_MAYBE_EMULATOR) ++suspectCount
        if (callback != null) {
            val stringBuffer = StringBuffer("Test start")
                .append("\r\n").append("hardware = ").append(hardwareResult.value)
                .append("\r\n").append("flavor = ").append(flavorResult.value)
                .append("\r\n").append("model = ").append(modelResult.value)
                .append("\r\n").append("manufacturer = ").append(manufacturerResult.value)
                .append("\r\n").append("board = ").append(boardResult.value)
                .append("\r\n").append("platform = ").append(platformResult.value)
                .append("\r\n").append("baseBand = ").append(baseBandResult.value)
                .append("\r\n").append("sensorNumber = ").append(sensorNumber)
                .append("\r\n").append("userAppNumber = ").append(userAppNumber)
                .append("\r\n").append("supportCamera = ").append(supportCamera)
                .append("\r\n").append("supportCameraFlash = ").append(supportCameraFlash)
                .append("\r\n").append("supportBluetooth = ").append(supportBluetooth)
                .append("\r\n").append("hasLightSensor = ").append(hasLightSensor)
                .append("\r\n").append("cgroupResult = ").append(cgroupResult.value)
                .append("\r\n").append("suspectCount = ").append(suspectCount)
            callback.findEmulator(stringBuffer.toString())
        }
        //嫌疑值大于3，认为是模拟器
        return suspectCount > 3
    }

    private fun getUserAppNum(userApps: String?): Int {
        if (TextUtils.isEmpty(userApps)) return 0
        val result =
            userApps!!.split("package:".toRegex()).toTypedArray()
        return result.size
    }

    private fun getProperty(propName: String): String? {
        val property = CommandUtil.singleInstance.getProperty(propName)
        return if (TextUtils.isEmpty(property)) null else property
    }

    /**
     * 特征参数-硬件名称
     *
     * @return 0表示可能是模拟器，1表示模拟器，2表示可能是真机
     */
    private fun checkFeaturesByHardware(): CheckResult {
        val hardware = getProperty("ro.hardware")
            ?: return CheckResult(
                CheckResult.RESULT_MAYBE_EMULATOR,
                null
            )
        val result: Int
        val tempValue = hardware.toLowerCase()
        result = when (tempValue) {
            "ttvm", "nox", "cancro", "intel", "vbox", "vbox86", "android_x86" -> CheckResult.RESULT_EMULATOR
            else -> CheckResult.RESULT_UNKNOWN
        }
        return CheckResult(result, hardware)
    }

    /**
     * 特征参数-渠道
     *
     * @return 0表示可能是模拟器，1表示模拟器，2表示可能是真机
     */
    private fun checkFeaturesByFlavor(): CheckResult {
        val flavor = getProperty("ro.build.flavor")
            ?: return CheckResult(
                CheckResult.RESULT_MAYBE_EMULATOR,
                null
            )
        val result: Int
        val tempValue = flavor.toLowerCase()
        result =
            if (tempValue.contains("vbox")) CheckResult.RESULT_EMULATOR else if (tempValue.contains(
                    "sdk_gphone"
                )
            ) CheckResult.RESULT_EMULATOR else CheckResult.RESULT_UNKNOWN
        return CheckResult(result, flavor)
    }

    /**
     * 特征参数-设备型号
     *
     * @return 0表示可能是模拟器，1表示模拟器，2表示可能是真机
     */
    private fun checkFeaturesByModel(): CheckResult {
        val model = getProperty("ro.product.model")
            ?: return CheckResult(
                CheckResult.RESULT_MAYBE_EMULATOR,
                null
            )
        val result: Int
        val tempValue = model.toLowerCase()
        result =
            if (tempValue.contains("google_sdk")) CheckResult.RESULT_EMULATOR else if (tempValue.contains(
                    "emulator"
                )
            ) CheckResult.RESULT_EMULATOR else if (tempValue.contains(
                    "android sdk built for x86"
                )
            ) CheckResult.RESULT_EMULATOR else CheckResult.RESULT_UNKNOWN
        return CheckResult(result, model)
    }

    /**
     * 特征参数-硬件制造商
     *
     * @return 0表示可能是模拟器，1表示模拟器，2表示可能是真机
     */
    private fun checkFeaturesByManufacturer(): CheckResult {
        val manufacturer = getProperty("ro.product.manufacturer")
            ?: return CheckResult(
                CheckResult.RESULT_MAYBE_EMULATOR,
                null
            )
        val result: Int
        val tempValue = manufacturer.toLowerCase()
        result =
            if (tempValue.contains("genymotion")) CheckResult.RESULT_EMULATOR else if (tempValue.contains(
                    "netease"
                )
            ) CheckResult.RESULT_EMULATOR //网易MUMU模拟器
            else CheckResult.RESULT_UNKNOWN
        return CheckResult(result, manufacturer)
    }

    /**
     * 特征参数-主板名称
     *
     * @return 0表示可能是模拟器，1表示模拟器，2表示可能是真机
     */
    private fun checkFeaturesByBoard(): CheckResult {
        val board = getProperty("ro.product.board")
            ?: return CheckResult(
                CheckResult.RESULT_MAYBE_EMULATOR,
                null
            )
        val result: Int
        val tempValue = board.toLowerCase()
        result =
            if (tempValue.contains("android")) CheckResult.RESULT_EMULATOR else if (tempValue.contains(
                    "goldfish"
                )
            ) CheckResult.RESULT_EMULATOR else CheckResult.RESULT_UNKNOWN
        return CheckResult(result, board)
    }

    /**
     * 特征参数-主板平台
     *
     * @return 0表示可能是模拟器，1表示模拟器，2表示可能是真机
     */
    private fun checkFeaturesByPlatform(): CheckResult {
        val platform = getProperty("ro.board.platform")
            ?: return CheckResult(
                CheckResult.RESULT_MAYBE_EMULATOR,
                null
            )
        val result: Int
        val tempValue = platform.toLowerCase()
        result =
            if (tempValue.contains("android")) CheckResult.RESULT_EMULATOR else CheckResult.RESULT_UNKNOWN
        return CheckResult(result, platform)
    }

    /**
     * 特征参数-基带信息
     *
     * @return 0表示可能是模拟器，1表示模拟器，2表示可能是真机
     */
    private fun checkFeaturesByBaseBand(): CheckResult {
        val baseBandVersion = getProperty("gsm.version.baseband")
            ?: return CheckResult(
                CheckResult.RESULT_MAYBE_EMULATOR,
                null
            )
        val result: Int
        result =
            if (baseBandVersion.contains("1.0.0.0")) CheckResult.RESULT_EMULATOR else CheckResult.RESULT_UNKNOWN
        return CheckResult(result, baseBandVersion)
    }

    /**
     * 获取传感器数量
     */
    private fun getSensorNumber(context: Context): Int {
        val sm =
            context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        return sm.getSensorList(Sensor.TYPE_ALL).size
    }

    /**
     * 获取已安装第三方应用数量
     */
    private val userAppNumber: Int
        private get() {
            val userApps = CommandUtil.singleInstance.exec("pm list package -3")
            return getUserAppNum(userApps)
        }

    /**
     * 是否支持相机
     */
    private fun supportCamera(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)
    }

    /**
     * 是否支持闪光灯
     */
    private fun supportCameraFlash(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
    }

    /**
     * 是否支持蓝牙
     */
    private fun supportBluetooth(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)
    }

    /**
     * 判断是否存在光传感器来判断是否为模拟器
     * 部分真机也不存在温度和压力传感器。其余传感器模拟器也存在。
     *
     * @return false为模拟器
     */
    private fun hasLightSensor(context: Context): Boolean {
        val sensorManager =
            context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor =
            sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) //光线传感器
        return if (null == sensor) false else true
    }

    /**
     * 特征参数-进程组信息
     */
    private fun checkFeaturesByCgroup(): CheckResult {
        val filter = CommandUtil.singleInstance.exec("cat /proc/self/cgroup")
            ?: return CheckResult(
                CheckResult.RESULT_MAYBE_EMULATOR,
                null
            )
        return CheckResult(
            CheckResult.RESULT_UNKNOWN,
            filter
        )
    }

    private object SingletonHolder {
        val INSTANCE = EmulatorCheckUtil()
    }

    companion object {
        val singleInstance: EmulatorCheckUtil
            get() = SingletonHolder.INSTANCE
    }
}