package com.angcyo.http

import com.angcyo.http.base.gson
import com.angcyo.http.interceptor.LogInterceptor
import com.angcyo.http.interceptor.UUIDInterceptor
import okhttp3.Interceptor
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

    //客户端队形
    var okHttpClient: OkHttpClient? = null

    //构造器
    val defaultOkHttpClientBuilder: OkHttpClient.Builder
        get() = OkHttpClient.Builder().apply {
            connectTimeout(TIME_OUT, TimeUnit.SECONDS)
            readTimeout(TIME_OUT, TimeUnit.SECONDS)
            writeTimeout(TIME_OUT, TimeUnit.SECONDS)
            proxy(Proxy.NO_PROXY)
            followRedirects(true)
            followSslRedirects(true)

            //UUID
            addNetworkInterceptor(UUIDInterceptor())

            //日志拦截器, 放在最后拦截
            addNetworkInterceptor(LogInterceptor())
            /*addNetworkInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })*/

            /* https://github.com/MrWu94/AndroidNote/wiki/Okhttp-%E7%9A%84addInterceptor-%E5%92%8C-addNetworkInterceptor-%E7%9A%84%E5%8C%BA%E5%88%AB%EF%BC%9F
                addInterceptor（应用拦截器）：
                1，不需要担心中间过程的响应,如重定向和重试.
                2，总是只调用一次,即使HTTP响应是从缓存中获取.
                3，观察应用程序的初衷. 不关心OkHttp注入的头信息如: If-None-Match.
                4，允许短路而不调用 Chain.proceed(),即中止调用.
                5，允许重试,使 Chain.proceed()调用多次.

                addNetworkInterceptor（网络拦截器）：
                1，能够操作中间过程的响应,如重定向和重试.
                2，当网络短路而返回缓存响应时不被调用.
                3，只观察在网络上传输的数据.
                4，携带请求来访问连接.
            * */
        }

    //构造器配置
    val onConfigOkHttpClient = mutableListOf<(OkHttpClient.Builder) -> Unit>()

    /**可以使用默认的[okHttpClient], 也可以返回自定义的client*/
    var onBuildHttpClient: (OkHttpClient.Builder) -> OkHttpClient = {
        okHttpClient ?: it.build()
    }

    /** baseUrl must end in '/' */
    var onGetBaseUrl: () -> String = { "http://api.angcyo.com/" }

    /**调用此方法, 添加自定义的配置*/
    fun configHttpBuilder(config: (OkHttpClient.Builder) -> Unit) {
        onConfigOkHttpClient.add(config)
    }

    /*----------Retrofit-----------*/

    var retrofit: Retrofit? = null

    val defaultRetrofitBuilder: Retrofit.Builder
        get() = Retrofit.Builder().apply {

        }

    var onBuildRetrofit: (Retrofit.Builder, OkHttpClient) -> Retrofit = { builder, client ->
        retrofit ?: builder.apply {
            //baseUrl must end in /
            val getBaseUrl = onGetBaseUrl()
            if (getBaseUrl.endsWith("/")) {
                baseUrl(getBaseUrl)
            } else {
                baseUrl("${getBaseUrl}/")
            }
            addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
            addConverterFactory(GsonConverterFactory.create(gson()))
            client(client)
        }.build()
    }

    fun reset() {
        retrofit = null
        okHttpClient = null
    }
}

/**添加拦截器*/
fun OkHttpClient.Builder.addInterceptorEx(interceptor: Interceptor, index: Int = -1) {
    with(interceptors()) {
        if (!this.contains(interceptor)) {
            if (index in this.indices) {
                add(index, interceptor)
            } else {
                add(interceptor)
            }
        }
    }
}