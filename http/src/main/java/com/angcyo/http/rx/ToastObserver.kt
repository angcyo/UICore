package com.angcyo.http.rx

import com.angcyo.library.toastQQ

/**
 * 带有错误Toast提示
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/07/29
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class ToastObserver<T> : BaseObserver<T>() {
    init {
        onError = {
            toastQQ(it.message ?: "接口异常")
        }
    }
}