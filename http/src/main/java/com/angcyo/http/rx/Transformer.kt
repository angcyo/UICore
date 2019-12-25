package com.angcyo.http.rx

import com.angcyo.http.RequestConfig
import io.reactivex.FlowableTransformer
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Rx 转换器
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/25
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

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

fun <Upstream> requestConfigTransformer(requestConfig: RequestConfig): ObservableTransformer<Upstream, Upstream> {
    return ObservableTransformer { upstream ->
        upstream.observeOn(Schedulers.io())
        upstream.flatMap {


            upstream
        }
    }
}
