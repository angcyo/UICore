package com.angcyo.http.interceptor

import com.angcyo.library.app
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
    override fun intercept(chain: Interceptor.Chain): Response {
        val newRequest = chain.request().newBuilder()
            .addHeader("deviceId", Device.deviceId)
            .addHeader("androidId", Device.androidId)
            .addHeader("imei", app().getIMEI() ?: "")
            .build()
        return chain.proceed(newRequest)
    }
}