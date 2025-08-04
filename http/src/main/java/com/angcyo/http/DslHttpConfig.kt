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

    /**构造器配置
     * [defaultOkHttpClientBuilder]
     * */
    val onConfigOkHttpClient = mutableListOf<(OkHttpClient.Builder) -> Unit>()

    /**可以使用默认的[okHttpClient], 也可以返回自定义的client*/
    var onBuildHttpClient: (OkHttpClient.Builder) -> OkHttpClient = {
        okHttpClient ?: it.build()
    }

    /** baseUrl must end in '/'
     * 可以优先获取自定义的地址 [com.angcyo.core.component.HttpConfigDialog.cCustomBaseUrl]
     * 其次获取配置的地址 [com.angcyo.core.CoreApplication.getHostBaseUrl]
     * */
    var onGetBaseUrl: () -> String = { "http://api.angcyo.com/" }

    /**默认构造器*/
    fun defaultOkHttpClientBuilder(
        timeout: Long? = null,
        unit: TimeUnit? = null,
    ): OkHttpClient.Builder {
        return OkHttpClient.Builder().apply {
            connectTimeout(timeout ?: TIME_OUT, unit ?: TimeUnit.SECONDS)
            readTimeout(timeout ?: TIME_OUT, unit ?: TimeUnit.SECONDS)
            writeTimeout(timeout ?: TIME_OUT, unit ?: TimeUnit.SECONDS)
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
            //https://www.jianshu.com/p/385780e78861

                addInterceptor（应用拦截器）：
                1，不需要担心中间过程的响应,如重定向和重试.
                2，总是只调用一次,即使HTTP响应是从缓存中获取.
                3，观察应用程序的初衷. 不关心OkHttp注入的头信息如: If-None-Match.
                4，允许短路而不调用 Chain.proceed(),即中止调用.
                5，允许重试,使 Chain.proceed()调用多次.

                有无网络都会被调用到。
                拦截器只会被调用一次，调用chain.proceed()得到的是重定向之后最终的响应信息，
                不会通过chain.connection() 获得中间过程的响应信息。
                允许短路，并且允许不去调用chain.proceed()请求服务器数据，可通过缓存来返回数据。

                addNetworkInterceptor（网络拦截器）：
                1，能够操作中间过程的响应,如重定向和重试.
                2，当网络短路而返回缓存响应时不被调用.
                3，只观察在网络上传输的数据.
                4，携带请求来访问连接.

                无网络时不会被调用。
                可以显示更多的信息，比如OkHttp为了减少数据的传输时间以及传输流量而自动添加的
                请求头Accept-Encoding: gzip，从而希望服务器能返回经过压缩过的响应数据。
                chain.connection()返回不为空的Connection对象，可以查询到客户端所连接的服务器的IP地址以及TLS配置信息。
            * */
        }
    }

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

/**移除所有满足条件的拦截器*/
fun OkHttpClient.Builder.removeInterceptor(predicate: (Interceptor) -> Boolean) {
    interceptors().removeAll(predicate)
    networkInterceptors().removeAll(predicate)
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