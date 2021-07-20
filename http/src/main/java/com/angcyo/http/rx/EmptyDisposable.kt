package com.angcyo.http.rx

import io.reactivex.disposables.Disposable

/**
 * 空实现
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/07/20
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class EmptyDisposable : Disposable {

    var isDispose = false

    override fun dispose() {
        isDispose = true
    }

    override fun isDisposed(): Boolean {
        return isDispose
    }
}