package com.angcyo.http.interceptor

import com.angcyo.http.base.isSuccess
import okhttp3.Interceptor
import okhttp3.Response

/**
 *
 * 授权拦截器, 用来处理被踢.
 *
 * 用来处理任意一个接口的返回值.
 *
 * ```
 * val authInterceptor = AuthInterceptor {
 *   if (it.code == 401) {
 *     toast("您已下线!")
 *     vmApp<LoginModel>().isTokenInvalidData.postValue(true)
 *   }
 * }
 * ```
 *
 * [com.angcyo.http.interceptor.TokenInterceptor]
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/12/08
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class AuthInterceptor(val failureAction: (Response) -> Unit) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originRequest = chain.request()
        val response = chain.proceed(originRequest)
        if (!response.code.isSuccess()) {
            failureAction(response)
        }
        return response
    }

}