package com.angcyo.glide.progress

import me.jessyan.progressmanager.ProgressManager
import okhttp3.OkHttpClient

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/12/03
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
object GlideProgress {
    var okHttpClient: OkHttpClient? = null
        get() {
            if (field == null) {
                field = defaultOkHttpClientBuilder.build()
            }
            return field
        }

    val defaultOkHttpClientBuilder = OkHttpClient.Builder().apply {
        /*connectTimeout(DslHttpConfig.TIME_OUT, TimeUnit.SECONDS)
        readTimeout(DslHttpConfig.TIME_OUT, TimeUnit.SECONDS)
        writeTimeout(DslHttpConfig.TIME_OUT, TimeUnit.SECONDS)
        proxy(Proxy.NO_PROXY)
        followRedirects(true)
        followSslRedirects(true)
        addInterceptor(UUIDInterceptor())
        addInterceptor(LogInterceptor())*/
        addInterceptor(ProgressManager.getInstance().interceptor)
    }
}