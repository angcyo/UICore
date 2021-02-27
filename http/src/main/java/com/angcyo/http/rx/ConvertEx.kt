package com.angcyo.http.rx

import com.angcyo.http.exception.HttpDataException
import com.angcyo.http.toBean
import com.angcyo.library.ex.isDebug
import com.angcyo.library.ex.orString
import com.google.gson.JsonElement
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.Response
import java.lang.reflect.Type

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/02/26
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */

fun <T> Observable<Response<JsonElement>>.mapTo(
    type: Type,
    parseError: Boolean = false,
    exception: Boolean = true
): Observable<T?> {
    return observeOn(Schedulers.io()).map {
        if (it.isSuccessful) {
            it.toBean<T>(type, parseError, exception)
        } else {
            val originMessage = "${it.message().orString("接口异常")}[${it.code()}]"
            val message = if (isDebug()) {
                "[${it.raw().request.url}]$originMessage"
            } else {
                originMessage
            }
            throw HttpDataException(message, it.code())
        }
    }
}

fun <T> Observable<Response<JsonElement>>.mapObserve(
    type: Type,
    parseError: Boolean = false,
    exception: Boolean = true,
    config: BaseObserver<T?> .() -> Unit = {},
    end: (data: T?, error: Throwable?) -> Unit
): Disposable {
    return mapTo<T>(type, parseError, exception)
        .observeOn(AndroidSchedulers.mainThread())
        .observe(config, end)
}

