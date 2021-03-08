package com.angcyo.http.rsa

import com.angcyo.http.base.isJsonType
import com.angcyo.http.base.isTextType
import com.angcyo.http.base.readString
import com.angcyo.library.L
import com.angcyo.library.ex.md5
import com.angcyo.library.ex.nowTime
import com.angcyo.library.ex.orString
import okhttp3.Interceptor
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/02/27
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class RSAInterceptor : Interceptor {

    companion object {
        const val KEY_SIGN = "sign"

        const val KEY_RSA_ENCRYPT = "rsa_encrypt"

        /**当前接口是否要加密请求*/
        fun encrypt(value: Boolean = true) = KEY_RSA_ENCRYPT to value.toString()
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originRequest = chain.request()

        val publicKey = RSA.publicKey

        if (!publicKey.isNullOrBlank() && originRequest.header(KEY_RSA_ENCRYPT) == "true") {
            //需要加密请求
            val contentType = originRequest.body?.contentType()
            if (contentType.isJsonType() || contentType.isTextType()) {
                val originBody = originRequest.body.readString()
                if (originBody.isNotEmpty()) {
                    val rsaBody = RSAUtil.encrypt(originBody, publicKey)
                    val sign = SecurityCodeUtil.encode(
                        rsaBody.md5().orString(),
                        RSA.securityCode.orString(),
                        nowTime()
                    )
                    L.i("\n签名:$sign\n原文:$originBody\n密文:$rsaBody")
                    val rsaRequest = originRequest.newBuilder()
                        .header(KEY_SIGN, sign)
                        .method(originRequest.method, rsaBody.toRequestBody(contentType))
                        .build()
                    return chain.proceed(rsaRequest)
                }
            }
        }

        return chain.proceed(originRequest)
    }
}