package com.angcyo.http.exception

import retrofit2.Response

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/09/28
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class HttpResponseException(
    msg: String,
    code: Int = -1,
    val response: Response<*>? = null
) : HttpDataException(msg, code)

val Throwable.response: Response<*>?
    get() {
        if (this is HttpResponseException) {
            return response
        }
        return null
    }