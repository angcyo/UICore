package com.angcyo.http.rx

import com.angcyo.http.post

/**
 * io.reactivex.Flowable：发送0个N个的数据，支持Reactive-Streams和背压
 *
 * io.reactivex.Observable：发送0个N个的数据，不支持背压，
 *
 * io.reactivex.Single：只能发送单个数据或者一个错误
 *
 * io.reactivex.Completable：没有发送任何数据，但只处理 onComplete 和 onError 事件。
 *
 * io.reactivex.Maybe：能够发射0或者1个数据，要么成功，要么失败。
 *
 * 操作符:
 * https://juejin.im/post/5d1eeffe6fb9a07f0870b4e8
 *
 * 更全的操作符:
 * https://juejin.im/post/5b17560e6fb9a01e2862246f
 *
 * 开源api调用:
 *
 * https://github.com/insoxin/API
 * https://api.isoyu.com/
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/25
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

fun rxJavaTest() {
    post {
        url = "http://www.mxnzp.com/api/tools/system/time"
    }.subscribe(BaseObserver())
}