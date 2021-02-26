package com.angcyo.http.interceptor

import android.Manifest
import com.angcyo.library.app
import com.angcyo.library.ex.havePermissions
import com.angcyo.library.utils.Device
import com.angcyo.library.utils.getIMEI
import okhttp3.Interceptor
import okhttp3.Response

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/11/20
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 *
 * 设备码请求头拦截器
 */

class UUIDInterceptor : Interceptor {

    companion object {
        val PUBLIC_HEADER = hashMapOf<String, String>()
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val newRequest = chain.request().newBuilder()
            //.addHeader("deviceId", Device.deviceId)
            .addHeader("androidId", Device.androidId)
            .addHeader("deviceInfo", Device.beautifyDeviceLog()) //设备一些基础信息
            .apply {
                PUBLIC_HEADER.forEach { entry ->
                    addHeader(entry.key, entry.value)
                }

                if (app().havePermissions(Manifest.permission.READ_PHONE_STATE)) {
                    val imei = app().getIMEI(log = false) ?: ""
                    if (imei.isNotEmpty()) {
                        addHeader("imei", app().getIMEI(log = false) ?: "")
                    }
                }
            }
            .build()
        return chain.proceed(newRequest)
    }
}