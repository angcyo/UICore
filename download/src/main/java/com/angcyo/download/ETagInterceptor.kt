package com.angcyo.download

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.net.HttpURLConnection


/**
 * 忽略OkDownLoad的ETag请求嗅探, 412
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/08/26
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class ETagInterceptor : Interceptor {

    /**忽略ETag的主机地址*/
    val ignoreETagHostList = mutableListOf<String>()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var newRequest: Request? = null
        if (ignoreETagHostList.contains(request.url.host)) {
            newRequest = request.newRequest()
        }
        var response = chain.proceed(newRequest ?: request)

        if (response.code == HttpURLConnection.HTTP_PRECON_FAILED ||
            response.code == 416
        ) {
            //重新请求
            ignoreETagHostList.add("${request.url.host}${request.url.encodedPath}")
            newRequest = request.newRequest()
            response = chain.proceed(newRequest)
        }

        /*//new response
        if (ignoreETagHostList.contains(request.url.host)) {
            response = response.newResponse()
        }*/

        return response
    }

    fun Response.newResponse() = newBuilder()
        .apply {
            removeHeader("If-Match")
            removeHeader("Range")
            removeHeader("ETag")
        }
        .build()

    fun Request.newRequest() = newBuilder()
        .apply {
            removeHeader("If-Match")
            removeHeader("Range")
            removeHeader("ETag")
        }
        .build()
}