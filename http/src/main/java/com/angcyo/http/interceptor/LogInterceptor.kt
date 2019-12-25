package com.angcyo.http.interceptor

import com.angcyo.http.base.readString
import com.angcyo.library.L
import com.angcyo.library.ex.isDebug
import okhttp3.*
import java.util.*
import java.util.concurrent.TimeUnit

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/25
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class LogInterceptor : Interceptor {

    var debug = isDebug()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (!debug) {
            return chain.proceed(request)
        }

        val uuid = UUID.randomUUID().toString()
        val requestBuilder = StringBuilder()
        val responseBuilder = StringBuilder()

        requestBuilder.appendln().append("-->").append(uuid)
        logRequest(chain, request, requestBuilder)
        L.i(requestBuilder)

        val startTime = System.nanoTime()

        val response: Response
        response = try {
            chain.proceed(request)
        } catch (e: Exception) {
            L.e("<--$uuid HTTP FAILED: $e")
            throw e
        }

        //请求耗时[毫秒]
        val requestTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime)

        responseBuilder.appendln().append("<--").append(uuid)
        responseBuilder.append(" (").append(requestTime).append("ms)")
        logResponse(response, responseBuilder)

        L.i(responseBuilder)

        return response
    }

    open fun logRequest(
        chain: Interceptor.Chain,
        request: Request,
        builder: StringBuilder
    ) {
        builder.apply {
            appendln().appends(request.method).appends(request.url)
            val connection: Connection? = chain.connection()
            val protocol = connection?.protocol() ?: Protocol.HTTP_1_1
            append(protocol)

            //打印请求头
            for (pair in request.headers) {
                appendln().append(pair.first).append(":").append(pair.second)
            }

            //打印请求提
            request.body?.run {
                val simpleName = javaClass.simpleName
                if (simpleName.isNotEmpty()) {
                    appendln().append(simpleName)
                }
                appendln().appends("Content-Type:").append(contentType())
                appendln().appends("Content-Length:").append(contentLength())
                appendln().appends("Body:").append(readString())
            } ?: append("no request body!")
        }
    }

    open fun logResponse(response: Response, builder: StringBuilder) {
        builder.apply {
            //返回头
            for (pair in response.headers) {
                appendln().append(pair.first).append(":").append(pair.second)
            }

            //返回体
            response.body?.run {
                appendln().appends("Body:").append(readString())
            } ?: append("no response body!")
        }
    }

    fun StringBuilder.appends(str: Any?): StringBuilder {
        return append(str).append(' ')
    }
}