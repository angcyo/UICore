package com.angcyo.http.interceptor

import com.angcyo.http.base.isPlaintext
import com.angcyo.http.base.readString
import com.angcyo.library.L
import com.angcyo.library.ex.isDebug
import com.angcyo.library.ex.isDebugType
import com.angcyo.library.ex.nowTime
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

    companion object {

        /**log策略
         * 取值:
         * false 关闭log
         * interval:1000 间隔1秒以上再输出日志
         * */
        const val HEADER_LOG = "header_log"

        /**用于指定当前url需要对应的key, 默认就是url?号之前的字符串*/
        const val HEADER_LOG_KEY = "header_key"

        private const val INTERVAL = "interval"
        private val lastLogUrlTimeMap = hashMapOf<String, Long>()

        /**关闭日志*/
        fun closeLog(close: Boolean = !isDebugType()) = HEADER_LOG to "${!close}"

        /**间隔多长时间, 才输出日志. 默认1小时输出一次*/
        fun intervalLog(mill: Long = 1 * 60 * 60 * 1000L /*毫秒*/) = HEADER_LOG to "${INTERVAL}:$mill"
    }

    /**是否打印 请求体和响应体*/
    var logRequestBody = true
    var logResponseBody = true

    var enable: Boolean = isDebug()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val header = request.header(HEADER_LOG)

        var pass = !enable
        if (header.isNullOrEmpty()) {
            //no op
        } else {
            if (header == true.toString()) {
                //强制打开日志
                pass = false
            } else if (header == false.toString()) {
                //强制关闭日志
                pass = true
            } else if (header.startsWith(INTERVAL)) {
                try {
                    val mill =
                        header.subSequence(INTERVAL.length + 1, header.length).toString()
                            .toLongOrNull()
                    if (mill != null) {
                        //规定了间隔多长时间, 才输出日志
                        val url = request.url.toString()//默认的url可能会带?号
                        val index = url.indexOf("?")

                        //key
                        val key = request.header(HEADER_LOG_KEY) ?: url.substring(
                            0,
                            if (index != -1) index else url.length
                        )

                        //time
                        val lastTime = lastLogUrlTimeMap[key] ?: -1
                        val nowTime = nowTime()

                        if (nowTime - lastTime >= mill) {
                            pass = false
                            lastLogUrlTimeMap[key] = nowTime
                        } else {
                            pass = true
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    pass = true
                }
            }
        }

        if (pass) {
            return chain.proceed(request)
        }

        val uuid = UUID.randomUUID().toString()
        val requestBuilder = StringBuilder()
        val responseBuilder = StringBuilder()

        //request
        requestBuilder.append("->").append(uuid)
        logRequest(chain, request, requestBuilder)
        printRequestLog(requestBuilder)

        //response
        val startTime = System.nanoTime()
        responseBuilder.append("<-").append(uuid)
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

            if (logRequestBody) {
                //打印请求体
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
            } else {
                appendln()
            }
        }
    }

    open fun logResponse(response: Response, builder: StringBuilder) {
        builder.apply {
            appendln().append(response.request.url)
            append(" ${response.message}").append("(${response.code})")

            //返回头
            for (pair in response.headers) {
                appendln().append(pair.first).append(":").append(pair.second)
            }

            if (logResponseBody) {
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
            } else {
                appendln()
            }
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