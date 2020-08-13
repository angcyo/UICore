package com.angcyo.http.interceptor

import com.angcyo.http.base.isPlaintext
import com.angcyo.http.base.readString
import com.angcyo.library.L
import com.angcyo.library.ex.isDebug
import okhttp3.*
import okio.Buffer
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * 网络请求日志输出
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

        //request
        requestBuilder.appendln().append("->").append(uuid)
        logRequest(chain, request, requestBuilder)
        printRequestLog(requestBuilder)

        //response
        val startTime = System.nanoTime()
        responseBuilder.appendln().append("<-").append(uuid)
        val response: Response
        response = try {
            chain.proceed(request)
        } catch (e: Exception) {
            val requestTime = toMillis(System.nanoTime() - startTime)
            responseBuilder.appendln()
                .append("HTTP FAILED(").append(requestTime).append("ms):")
                .append(e)

            printResponseLog(responseBuilder)
            throw e
        }

        //请求耗时[毫秒]
        val requestTime = toMillis(System.nanoTime() - startTime)
        responseBuilder.append(" (").append(requestTime).append("ms)")
        logResponse(response, responseBuilder)

        printResponseLog(responseBuilder)

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
                appendln().appendln("Body:")

                if (request.headers.hasEncoded()) {
                    //加密了数据
                    appendln("(encoded body omitted)")
                } else {
                    val buffer = Buffer()
                    writeTo(buffer)
                    if (buffer.isPlaintext()) {
                        appendln(readString())
                    } else {
                        appendln("binary request body")
                    }
                }
            } ?: appendln(" no request body!")
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
                val contentLength: Long = contentLength()
                val bodySize: String =
                    if (contentLength != -1L) "$contentLength-byte" else "unknown-length"
                appendln().append("Body")
                append("(").append(bodySize).appendln("):")

                if (response.headers.hasEncoded()) {
                    //加密了数据
                    appendln("(encoded body omitted)")
                } else {
                    try {
                        if (source().buffer.isPlaintext()) {
                            appendln(readString())
                        } else {
                            appendln("binary response body.")
                        }
                    } catch (e: Exception) {
                        appendln(e.message)
                    }
                }
            } ?: appendln("no response body!")
        }
    }

    /**头, 是否包含加密信息[gzip]*/
    fun Headers.hasEncoded(): Boolean {
        val contentEncoding = this["Content-Encoding"]
        return contentEncoding != null && !contentEncoding.equals("identity", ignoreCase = true)
    }

    fun StringBuilder.appends(str: Any?): StringBuilder {
        return append(str).append(' ')
    }

    fun toMillis(ms: Long): Long {
        return TimeUnit.NANOSECONDS.toMillis(ms)
    }

    open fun printRequestLog(builder: StringBuilder) {
        L.d(builder.toString())
    }

    open fun printResponseLog(builder: StringBuilder) {
        L.d(builder.toString())
    }
}