package com.angcyo.http.rx

import com.angcyo.http.toBean
import com.google.gson.JsonElement
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
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
    parseError: Boolean = false
): Observable<T?> {
    return map {
        it.toBean<T>(type, parseError)
    }
}

fun <T> Observable<Response<JsonElement>>.mapObserve(
    type: Type,
    parseError: Boolean = false,
    config: BaseObserver<T?>.() -> Unit = {},
    end: (data: T?, error: Throwable?) -> Unit
): Disposable {
    return mapTo<T>(type, parseError).observe(config, end)
}

