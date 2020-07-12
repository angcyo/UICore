package com.angcyo.http.rx

import com.angcyo.http.RequestConfig
import com.angcyo.http.base.getString
import com.angcyo.http.exception.HttpDataException
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import io.reactivex.FlowableTransformer
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Response

/**
 * Rx 转换器
 * [observeOn] 指定下面的调度, 在那个调度器
 * [subscribeOn] 指定订阅在那个调度器
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/25
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

fun <T> observableToBack(): ObservableTransformer<T, T> {
    return ObservableTransformer { upstream ->
        upstream.subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
    }
}

fun <T> observableToMain(): ObservableTransformer<T, T> {
    return ObservableTransformer { upstream ->
        upstream.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }
}

fun <T> flowableToMain(): FlowableTransformer<T, T> {
    return FlowableTransformer { upstream ->
        upstream.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }
}

fun <T> flowableToBack(): FlowableTransformer<T, T> {
    return FlowableTransformer { upstream ->
        upstream.subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
    }
}

fun requestConfigTransformer(requestConfig: RequestConfig): ObservableTransformer<Response<JsonElement>, Response<JsonElement>> {
    return ObservableTransformer { upstream ->
        upstream.apply {
            observeOn(Schedulers.io())
            doOnSubscribe {
                requestConfig.onStart(it)
            }
            doOnNext {
                val body = it.body()
                when {
                    requestConfig.isSuccessful(it) -> {
                        requestConfig.onSuccess(it)
                    }
                    body is JsonObject -> {
                        throw HttpDataException(body.getString(requestConfig.msgKey) ?: "数据异常")
                    }
                    else -> {
                        requestConfig.onSuccess(it)
                    }
                }
            }
            doOnComplete {
                requestConfig.onComplete()
            }
            doOnError {
                requestConfig.onError(it)
            }
            subscribeOn(Schedulers.io())
            observeOn(AndroidSchedulers.mainThread())
        }
    }
}
