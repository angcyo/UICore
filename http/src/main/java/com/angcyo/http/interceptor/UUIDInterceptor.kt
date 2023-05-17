package com.angcyo.http.interceptor

import android.Manifest
import android.content.Context
import com.angcyo.library.app
import com.angcyo.library.ex.havePermissions
import com.angcyo.library.ex.uuid
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
            .addHeader("androidId", Device.androidId) //86756e10cf9a9562
            .addHeader("log-trace-id", uuid()) //b955430613fb45b5ba04d41a3a61d725
            //.addHeader("language", getCurrentLanguage()) //zh_CN
            //java.lang.IllegalArgumentException: Unexpected char 0x0a at 34 in deviceInfo
            //.addHeader("deviceInfo", Device.beautifyDeviceLog().encode()) //设备一些基础信息, 去掉节省流量
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

    /**获取当前系统语言格式 zh_CN
     * [com.angcyo.core.component.model.LanguageModel.Companion.getCurrentLanguage]*/
    private fun getCurrentLanguage(context: Context = app()): String {
        //zh_CN_#Hans
        val locale = context.resources.configuration.locale
        val language = locale.language
        val country = locale.country
        return language + "_" + country
    }

}