package com.angcyo.http.rx

import com.angcyo.http.rx.Rx.rxErrorHandlerOnceList
import com.angcyo.library.ex.size
import com.angcyo.library.ex.writeLogTo
import com.angcyo.library.isMain
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers

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

object Rx {

    /**Rx异常单次通知*/
    val rxErrorHandlerOnceList = mutableListOf<Consumer<Throwable>>()

    /**初始化*/
    fun init(block: (Throwable) -> Unit = {}) {
        if (!RxJavaPlugins.isLockdown()) {
            RxJavaPlugins.setErrorHandler { error ->
                "Rx异常[${rxErrorHandlerOnceList.size()}]:${error.stackTraceToString()}".writeLogTo()
                error.printStackTrace()
                if (rxErrorHandlerOnceList.isNotEmpty()) {
                    try {
                        rxErrorHandlerOnceList.toList().forEach {
                            it.accept(error)
                        }
                    } finally {
                        rxErrorHandlerOnceList.clear()
                    }
                }

                //back
                block(error)
            }
        }
    }
}

fun Consumer<Throwable>.addRxErrorHandleOnce() {
    rxErrorHandlerOnceList.add(this)
}

fun Consumer<Throwable>.removeRxErrorHandleOnce() {
    rxErrorHandlerOnceList.remove(this)
}

/**使用Rx调度后台线程, 主线程切换*/
fun <T : Any> runRx(backAction: () -> T?, mainAction: (T?) -> Unit = {}): Disposable {
    return Single.create<T?> { emitter ->
        try {
            emitter.onSuccess(backAction()!!)
        } catch (e: Exception) {
            "Rx run异常:${e.stackTraceToString()}".writeLogTo()
            e.printStackTrace()
            emitter.onError(e)
        }
    }.subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(mainAction) {
            mainAction(null)
        }
}

/**在主线程回调*/
fun doMain(check: Boolean = true, action: () -> Unit) {
    if (check && isMain()) {
        action()
    } else {
        runRx({ true }, { action() })
    }
}

/**在子线程回调
 * [check] 是否检查线程情况, 如果已经是子线程, 则不开启新线程, 否则.*/
fun doBack(check: Boolean = false, action: () -> Unit) {
    if (check) {
        //已经在子线程
        if (isMain()) {
            runRx({
                try {
                    action()
                    true
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
            })
        } else {
            action()
        }
    } else {
        runRx({
            try {
                action()
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        })
    }
}