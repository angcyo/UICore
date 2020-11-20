package com.angcyo.http

import com.angcyo.http.base.gson
import com.angcyo.http.interceptor.LogInterceptor
import com.angcyo.http.interceptor.UUIDInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.net.Proxy
import java.util.concurrent.TimeUnit

/**
 * OkHttp3 Retrofit 配置
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/25
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
class DslHttpConfig {

    companion object {
        //秒
        var TIME_OUT: Long = 5
    }

    /*----------OkHttp3-----------*/

    var okHttpClient: OkHttpClient? = null

    val defaultOkHttpClientBuilder = OkHttpClient.Builder().apply {
        connectTimeout(TIME_OUT, TimeUnit.SECONDS)
        readTimeout(TIME_OUT, TimeUnit.SECONDS)
        writeTimeout(TIME_OUT, TimeUnit.SECONDS)
        proxy(Proxy.NO_PROXY)
        followRedirects(true)
        followSslRedirects(true)
        addInterceptor(UUIDInterceptor())
        addInterceptor(LogInterceptor())
    }

    val onConfigOkHttpClient = mutableListOf<(OkHttpClient.Builder) -> Unit>()

    /**可以使用默认的[okHttpClient], 也可以返回自定义的client*/
    var onBuildHttpClient: (OkHttpClient.Builder) -> OkHttpClient = {
        okHttpClient ?: it.build()
    }

    /**base url*/
    var onGetBaseUrl: () -> String = { "http://api.angcyo.com" }

    /*----------Retrofit-----------*/

    var retrofit: Retrofit? = null

    val defaultRetrofitBuilder = Retrofit.Builder().apply {

    }

    var onBuildRetrofit: (Retrofit.Builder, OkHttpClient) -> Retrofit = { builder, client ->
        retrofit ?: builder.apply {
            baseUrl(onGetBaseUrl())
            addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
            addConverterFactory(GsonConverterFactory.create(gson()))
            client(client)
        }.build()
    }
}