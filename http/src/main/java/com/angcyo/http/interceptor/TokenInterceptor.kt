package com.angcyo.http.interceptor

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.nio.charset.Charset
import java.util.concurrent.CountDownLatch

/**
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2019/02/20
 * Copyright (c) 2019 Shenzhen O&M Cloud Co., Ltd. All rights reserved.
 */
class TokenInterceptor : Interceptor {

    /**解析字符编码*/
    var charset: Charset

    /**token回调监听*/
    var tokenListener: OnTokenListener?

    /** token无效后, 重试的次数 */
    var tryCount = 1

    /** body 允许读取的最大值 64kb */
    var maxReadSize = 64 * 1024.toLong()

    constructor(tokenListener: OnTokenListener?) {
        this.tokenListener = tokenListener
        charset = Charset.defaultCharset()
    }

    constructor(tryCount: Int, charset: Charset, tokenListener: OnTokenListener?) {
        this.charset = charset
        this.tokenListener = tokenListener
        this.tryCount = tryCount
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originRequest = chain.request()
        if (tokenListener == null || tokenListener!!.ignoreRequest(originRequest)) {
            return chain.proceed(originRequest)
        }

        //拿到正常接口请求的数据
        val tokenRequest = setToken(originRequest)
        val originResponse = chain.proceed(tokenRequest)
        var resultResponse = originResponse
        if (tokenListener != null) {
            var index = 0
            while (index++ < tryCount) {
                try {
                    val oldResponse = resultResponse
                    resultResponse = doIt(chain, originRequest, resultResponse)
                    if (oldResponse != resultResponse && tryCount > 1) {
                        //返回的结果不一样, 并且重试次数大于1
                        if (isTokenInvalid(resultResponse)) {
                            //再次判断返回结果是否是token失效
                        } else {
                            //token有效, 退出循环
                            break
                        }
                    }
                } catch (e: Exception) {
                    resultResponse = originResponse
                    break
                }
            }
        }
        return resultResponse
    }

    @Throws(IOException::class)
    private fun getResponseBodyString(response: Response?): String {
        if (response == null) {
            return ""
        }
        val responseBodyBuilder = StringBuilder()
        val responseBody = response.body
        if (responseBody?.contentType() != null &&
            responseBody.contentLength() > 0 &&
            responseBody.contentLength() < maxReadSize
        ) {
            val source = responseBody.source()
            source.request(Long.MAX_VALUE)
            val buffer = source.buffer
            responseBodyBuilder.append(buffer.clone().readString(charset))
        }
        return responseBodyBuilder.toString()
    }

    /**
     * 通过返回结果, 判断token是否过期
     */
    @Throws(IOException::class)
    private fun isTokenInvalid(resultResponse: Response): Boolean {
        return tokenListener?.isTokenInvalid(
            resultResponse,
            getResponseBodyString(resultResponse)
        ) ?: false
    }

    /**
     * 获取token, 并且重新请求接口
     */
    @Throws(IOException::class)
    private fun doIt(
        chain: Interceptor.Chain,
        originRequest: Request,
        resultResponse: Response
    ): Response {
        var result = resultResponse
        //判断token是否过期
        if (isTokenInvalid(resultResponse)) {
            val countDownLatch =
                CountDownLatch(1)

            //Token失效
            tokenListener!!.tryGetToken(countDownLatch)
            try {
                countDownLatch.await()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            //重新请求本次接口
            val tokenRequest = setToken(originRequest)
            result = chain.proceed(tokenRequest)
        }
        return result
    }

    /**
     * 为这个请求, 设置token信息
     */
    private fun setToken(request: Request): Request {
        var result = request
        if (tokenListener != null) {
            result = tokenListener!!.initToken(request)
        }
        return result
    }

    interface OnTokenListener {
        /** 是否要忽略这个请求 */
        fun ignoreRequest(originRequest: Request): Boolean

        /** 设置token */
        fun initToken(originRequest: Request): Request

        /**
         * 根据接口返回值, 判断token是否失效
         *
         * @return true token失效
         */
        fun isTokenInvalid(
            response: Response,
            bodyString: String
        ): Boolean

        /**
         * 重新获取token
         * 获取token成功之后, 请调用 [CountDownLatch.countDown]
         */
        fun tryGetToken(latch: CountDownLatch)
    }

    open class TokenListenerAdapter : OnTokenListener {
        override fun ignoreRequest(originRequest: Request): Boolean {
            return false
        }

        override fun initToken(originRequest: Request): Request {
            return originRequest
        }

        override fun isTokenInvalid(
            response: Response,
            bodyString: String
        ): Boolean {
            return false
        }

        override fun tryGetToken(latch: CountDownLatch) {
            latch.countDown()
        }
    }
}