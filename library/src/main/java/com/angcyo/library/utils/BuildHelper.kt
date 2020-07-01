package com.angcyo.library.utils

import android.os.Build
import android.os.Build.VERSION
import android.util.Log
import java.lang.reflect.Field
import java.util.*


/**
 * https://github.com/sufadi/AndroidCpuTools
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020-7-1
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
object BuildHelper {
    private val TAG = BuildHelper::class.java.simpleName

    /**
     * Build class所有的字段属性
     * Build.BOARD : Z91
     * Build.BOOTLOADER : unknown
     * Build.BRAND : FaDi
     * Build.CPU_ABI : arm64-v8a
     * Build.CPU_ABI2 :
     * Build.DEVICE : Z91
     * Build.DISPLAY : TEST_FaDi_Z91_S100_20180108
     * Build.FINGERPRINT : FaDi/Z91/Z91:7.1.1/N6F26Q/1515397384:user/release-keys
     * Build.HARDWARE : mt6739
     * Build.HOST : 69959bbb90c6
     * Build.ID : N6F26Q
     * Build.IS_DEBUGGABLE : true
     * Build.IS_EMULATOR : false
     * Build.MANUFACTURER : FaDi
     * Build.MODEL : Z91
     * Build.PERMISSIONS_REVIEW_REQUIRED : false
     * Build.PRODUCT : Z91
     * Build.RADIO : unknown
     * Build.SERIAL : 0123456789ABCDEF
     * Build.SUPPORTED_32_BIT_ABIS : [Ljava.lang.String;@305cf5e
     * Build.SUPPORTED_64_BIT_ABIS : [Ljava.lang.String;@f5c1f3f
     * Build.SUPPORTED_ABIS : [Ljava.lang.String;@578b00c
     * Build.TAG : Build
     * Build.TAGS : release-keys
     * Build.TIME : 1515397382000
     * Build.TYPE : user
     * Build.UNKNOWN : unknown
     * Build.USER : FaDi
     * Build.VERSION.ACTIVE_CODENAMES : [Ljava.lang.String;@f4ecd55
     * Build.VERSION.ALL_CODENAMES : [Ljava.lang.String;@bdb836a
     * Build.VERSION.BASE_OS :
     * Build.VERSION.CODENAME : REL
     * Build.VERSION.INCREMENTAL : 1515397384
     * Build.VERSION.PREVIEW_SDK_INT : 0
     * Build.VERSION.RELEASE : 7.1.1
     * Build.VERSION.RESOURCES_SDK_INT : 25
     * Build.VERSION.SDK : 25
     * Build.VERSION.SDK_INT : 25
     * Build.VERSION.SECURITY_PATCH : 2017-11-05
     */
    val allBuildInformation: List<String>
        get() {
            val result: MutableList<String> =
                ArrayList()
            val fields: Array<Field> = Build::class.java.declaredFields
            for (field in fields) {
                try {
                    field.isAccessible = true
                    val info = "Build." + field.name + " : " + field.get(null)
                    Log.w(TAG, info)
                    result.add(info)
                } catch (e: Exception) {
                    Log.e(TAG, "an error occured when collect crash info", e)
                }
            }
            val fieldsVersion: Array<Field> = VERSION::class.java.declaredFields
            for (field in fieldsVersion) {
                try {
                    field.isAccessible = true
                    val info = "Build.VERSION." + field.name + " : " + field.get(null)
                    Log.w(TAG, info)
                    result.add(info)
                } catch (e: Exception) {
                    Log.e(TAG, "an error occured when collect crash info", e)
                }
            }
            return result
        }

    // 手机制造商
    val product: String
        get() = Build.PRODUCT

    // 系统定制商
    val brand: String
        get() = Build.BRAND

    // 硬件制造商
    val manufacturer: String
        get() = Build.MANUFACTURER

    // 平台信息
    val hardWare: String
        get() {
            var result: String = Build.HARDWARE
            if (result.matches("qcom".toRegex())) {
                Log.d(TAG, "Qualcomm platform")
                result = "高通平台(Qualcomm) - $result"
            } else if (result.matches("mt[0-9]*".toRegex())) {
                result = "MTK平台(MediaTek) - $result"
            }
            return result
        }

    // 型号
    val mode: String
        get() = Build.MODEL

    // Android 系统版本
    val androidVersion: String
        get() = VERSION.RELEASE

    // CPU 指令集，可以查看是否支持64位
    val cpuAbi: String
        get() = Build.CPU_ABI

    val isCpu64: Boolean
        get() {
            var result = false
            if (Build.CPU_ABI.contains("arm64")) {
                result = true
            }
            return result
        }

    // 显示模块
    val display: String
        get() = Build.DISPLAY

    // SDK 当前版本号
    val curSDK: Int
        get() = VERSION.SDK_INT
}