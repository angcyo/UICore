package com.angcyo.http.rx

import com.angcyo.http.base.getString
import com.angcyo.http.base.isJson
import com.angcyo.http.base.readString
import com.angcyo.http.base.toJsonObject
import com.angcyo.http.exception.HttpDataException
import com.angcyo.http.toBean
import com.angcyo.library.ex.isShowDebug
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

/**从[Response]中获取错误的提示信息*/
fun Response<*>.errorMessage(def: String = "接口异常"): String {
    var errorString = errorBody()?.readString()
    if (errorString.isNullOrEmpty()) {
        errorString = message()
    }

    if (!errorString.isNullOrEmpty()) {
        if (errorString.isJson()) {
            errorString.toJsonObject()?.let {
                val e1 = it.getString("error")
                if (!e1.isNullOrEmpty()) {
                    errorString = e1
                } else {
                    val e2 = it.getString("msg")
                    if (!e2.isNullOrEmpty()) {
                        errorString = e2
                    }
                }
            }
        }
    }
    return if (errorString.isNullOrEmpty()) {
        def
    } else {
        errorString!!
    }
}

fun <T> Observable<Response<JsonElement>>.mapTo(
    type: Type,
    parseError: Boolean = false,
    exception: Boolean = true
): Observable<T?> {
    return observeOn(Schedulers.io()).map {
        if (it.isSuccessful) {
            it.toBean<T>(type, parseError, exception)
        } else {
            val originMessage = "${it.errorMessage()}[${it.code()}]"
            val message = if (isShowDebug()) {
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

