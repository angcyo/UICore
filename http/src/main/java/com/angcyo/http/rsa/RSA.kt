package com.angcyo.http.rsa

import com.angcyo.http.DslHttp

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/02/27
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
object RSA {

    /**公钥, 用于加密*/
    var publicKey: String? = null

    fun init(publicKey: String) {
        RSA.publicKey = publicKey

        DslHttp.config {
            val rsaInterceptor = RSAInterceptor()
            configHttpBuilder {
                if (!it.interceptors().contains(rsaInterceptor)) {
                    it.addInterceptor(rsaInterceptor)
                }
            }
        }
    }
}