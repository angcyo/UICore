package com.angcyo.http.interceptor

import com.angcyo.http.base.isPlaintext
import com.angcyo.http.base.readString
import com.angcyo.library.L
import com.angcyo.library.ex.*
import okhttp3.*
import okhttp3.internal.http.promisesBody
import okio.Buffer
import okio.GzipSource
import java.io.EOFException
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

        const val HEADER_LOG_REQUEST_BODY = "header_log_request_body"
        const val HEADER_LOG_RESPONSE_BODY = "header_log_response_body"

        const val INTERVAL = "interval"

        private val lastLogUrlTimeMap = hashMapOf<String, Long>()

        /**关闭日志*/
        fun closeLog(close: Boolean = !isDebugType()) = HEADER_LOG to "${!close}"

        /**间隔多长时间, 才输出日志. 默认1小时输出一次*/
        fun intervalLog(mill: Long = 1 * 60 * 60 * 1000L /*毫秒*/) = HEADER_LOG to "${INTERVAL}:$mill"
    }

    val uuidKey = "uuid"

    /**是否打印 请求体和响应体*/
    var logRequestBody = true
    var logResponseBody = true

    var enable: Boolean = L.debug

    override fun intercept(chain: Interceptor.Chain): Response {
        val originRequest = chain.request()

        val header = originRequest.header(HEADER_LOG)

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
                        val url = originRequest.url.toString()//默认的url可能会带?号
                        val index = url.indexOf("?")

                        //key
                        val key = originRequest.header(HEADER_LOG_KEY) ?: url.substring(
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
            return chain.proceed(originRequest)
        }

        val uuid = UUID.randomUUID().toString()

        //new request
        val request = if (originRequest.header(uuidKey) == null) {
            originRequest.newBuilder().header(uuidKey, uuid).build()
        } else {
            originRequest
        }

        //result
        val requestBuilder = StringBuilder()
        val responseBuilder = StringBuilder()

        //request
        requestBuilder.append("->").append(uuid)
        logRequest(chain, originRequest, requestBuilder)
        try {
            printRequestLog(requestBuilder)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        //response
        val startTime = System.nanoTime()
        responseBuilder.append("<-").append(uuid)
        val response: Response = try {
            chain.proceed(request)
        } catch (e: Exception) {
            val requestTime = toMillis(System.nanoTime() - startTime)
            responseBuilder.appendln()
                .append("HTTP FAILED(").append(requestTime).append("ms):")
                .append(e)

            try {
                printResponseLog(responseBuilder)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            throw e
        }

        //请求耗时[毫秒]
        val requestTime = toMillis(System.nanoTime() - startTime)
        responseBuilder.append(" (").append(requestTime).append("ms)")
        logResponse(originRequest, response, responseBuilder)

        try {
            printResponseLog(responseBuilder)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return response
    }

    open fun logRequest(chain: Interceptor.Chain, request: Request, builder: StringBuilder) {
        builder.apply {
            appendln().appends(request.method).appends(request.url)
            val connection: Connection? = chain.connection()
            val protocol = connection?.protocol() ?: Protocol.HTTP_1_1
            append(protocol)

            //打印请求头
            for (pair in request.headers) {
                appendln().append(pair.first).append(":").append(pair.second)
            }

            if (logRequestBody && request.logRequestBody(logRequestBody)) {
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
                } ?: appendln("\nno request body!")
            } else {
                appendln()
            }
        }
    }

    open fun logResponse(request: Request, response: Response, builder: StringBuilder) {
        builder.apply {
            appendln().append(response.request.url)
            append(" ${response.message}").append("(${response.code})")

            //返回头
            for (pair in response.headers) {
                appendln().append(pair.first).append(":").append(pair.second)
            }

            if (logResponseBody && request.logResponseBody(logResponseBody)) {

                //返回体
                var bodyLength = -1L //body长度
                var bodyString: String? = "no response body!" //body字符串

                response.body?.run {
                    bodyLength = contentLength()

                    bodyString = if (!response.promisesBody()) {
                        "(no body)"
                    } else if (bodyHasUnknownEncoding(response.headers) || response.headers.hasEncoded()) {
                        //加密了数据
                        "(encoded body omitted)"
                    } else {
                        try {
                            val source = source()
                            source.request(Long.MAX_VALUE) // Buffer the entire body.
                            var buffer = source.buffer

                            var gzippedLength: Long? = null
                            if ("gzip".equals(
                                    response.headers["Content-Encoding"],
                                    ignoreCase = true
                                )
                            ) {
                                gzippedLength = buffer.size
                                GzipSource(buffer.clone()).use { gzippedResponseBody ->
                                    buffer = Buffer()
                                    buffer.writeAll(gzippedResponseBody)
                                }
                            }

                            when {
                                !buffer.isProbablyUtf8() -> "(binary ${buffer.size}-byte body omitted)"
                                gzippedLength != null -> "(${buffer.size}-byte, $gzippedLength-gzipped-byte body)"
                                buffer.size <= 0 -> "buffer is empty."
                                buffer.isPlaintext() -> readString()
                                else -> "binary response body."
                            }
                        } catch (e: Exception) {
                            e.message
                        }
                    }
                }

                if (bodyLength == -1L) {
                    bodyLength = (bodyString?.byteSize() ?: -1).toLong()
                }

                val bodyLengthString: String =
                    if (bodyLength != -1L) "$bodyLength-byte" else "unknown-length"

                appendln().appendln("Body(${bodyLengthString}):")
                appendln(bodyString)

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

    private fun bodyHasUnknownEncoding(headers: Headers): Boolean {
        val contentEncoding = headers["Content-Encoding"] ?: return false
        return !contentEncoding.equals("identity", ignoreCase = true) &&
                !contentEncoding.equals("gzip", ignoreCase = true)
    }

    /**输出日志*/
    open fun printRequestLog(builder: StringBuilder) {
        L.i(builder.toString())
    }

    /**输出日志*/
    open fun printResponseLog(builder: StringBuilder) {
        L.i(builder.toString())
    }
}

internal fun Buffer.isProbablyUtf8(): Boolean {
    try {
        val prefix = Buffer()
        val byteCount = size.coerceAtMost(64)
        copyTo(prefix, 0, byteCount)
        for (i in 0 until 16) {
            if (prefix.exhausted()) {
                break
            }
            val codePoint = prefix.readUtf8CodePoint()
            if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                return false
            }
        }
        return true
    } catch (_: EOFException) {
        return false // Truncated UTF-8 sequence.
    }
}

/**关闭日志*/
fun Request.Builder.closeLog(close: Boolean = !isDebugType()): Request.Builder {
    header(LogInterceptor.HEADER_LOG, "${!close}")
    return this
}

fun Request.Builder.logRequestBody(log: Boolean = true): Request.Builder {
    header(LogInterceptor.HEADER_LOG_REQUEST_BODY, "$log")
    return this
}

fun Request.Builder.logResponseBody(log: Boolean = true): Request.Builder {
    header(LogInterceptor.HEADER_LOG_RESPONSE_BODY, "$log")
    return this
}

/**间隔多长时间, 才输出日志. 默认1小时输出一次*/
fun Request.Builder.intervalLog(mill: Long = 1 * 60 * 60 * 1000L /*毫秒*/): Request.Builder {
    header(LogInterceptor.HEADER_LOG, "${LogInterceptor.INTERVAL}:$mill")
    return this
}

fun Request.logRequestBody(def: Boolean = true) =
    header(LogInterceptor.HEADER_LOG_REQUEST_BODY) ?: "true" == def.toString()

fun Request.logResponseBody(def: Boolean = true) =
    header(LogInterceptor.HEADER_LOG_RESPONSE_BODY) ?: "true" == def.toString()