package com.angcyo.http.rx

import com.angcyo.library.L
import com.angcyo.library.ex.simpleHash
import io.reactivex.Flowable
import io.reactivex.FlowableSubscriber
import io.reactivex.disposables.Disposable
import io.reactivex.exceptions.CompositeException
import io.reactivex.exceptions.Exceptions
import io.reactivex.internal.subscriptions.SubscriptionHelper
import io.reactivex.plugins.RxJavaPlugins
import org.reactivestreams.Subscription
import java.util.concurrent.atomic.AtomicReference

/**
 * [io.reactivex.internal.subscribers.LambdaSubscriber]
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/06/29
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */

open class BaseFlowableSubscriber<T> : AtomicReference<Subscription>(),
    FlowableSubscriber<T>, Subscription, Disposable {

    //<editor-fold desc="Dsl">

    var onSubscribe: (Subscription) -> Unit = {
        L.d("${this.javaClass.name}#onSubscribe")
        //设置下游数据消费的能力, 不设置将收不到[onNext]回调
        it.request(Long.MAX_VALUE)
    }

    /**订阅开始, 等同于[onSubscribe]*/
    var onStart: (Subscription) -> Unit = {

    }

    var onNext: (T) -> Unit = {
        L.d("${this.simpleHash()}#onNext:$it")
    }

    var onError: (Throwable) -> Unit = {
        L.e("${this.simpleHash()}#onError:$it")
    }

    var onComplete: () -> Unit = {
        L.d("${this.simpleHash()}#onComplete")
    }

    /**不管是成功, 还是失败, 都会触发的回调*/
    var onObserverEnd: (data: T?, error: Throwable?) -> Unit = { data, error ->
        val string = buildString {
            appendln().appendln("订阅结束#onObserverEnd:")
            append("data->").appendln(data)
            append("error->").appendln(error)
        }
        error?.run { L.e(string);printStackTrace() } ?: L.i(string)
    }

    //</editor-fold desc="Dsl">

    //<editor-fold desc="Subscriber 回调">

    override fun onSubscribe(s: Subscription) {
        if (SubscriptionHelper.setOnce(this, s)) {
            try {
                onSubscribe.invoke(this)
                onStart.invoke(this)
            } catch (ex: Throwable) {
                Exceptions.throwIfFatal(ex)
                s.cancel()
                onError(ex)
            }
        }
    }

    val _lastData: T? get() = observerDataList.lastOrNull()
    val observerDataList = mutableListOf<T>()
    override fun onNext(t: T) {
        if (!isDisposed) {
            try {
                observerDataList.add(t)
                onNext.invoke(t)
            } catch (e: Throwable) {
                Exceptions.throwIfFatal(e)
                get().cancel()
                onError(e)
            }
        }
    }

    var _lastError: Throwable? = null
    override fun onError(t: Throwable) {
        if (get() !== SubscriptionHelper.CANCELLED) {
            lazySet(SubscriptionHelper.CANCELLED)
            try {
                _lastError = t
                onError.invoke(t)

                onEnd()
            } catch (e: Throwable) {
                Exceptions.throwIfFatal(e)
                RxJavaPlugins.onError(CompositeException(t, e))
            }
        } else {
            RxJavaPlugins.onError(t)
        }
    }

    override fun onComplete() {
        if (get() !== SubscriptionHelper.CANCELLED) {
            lazySet(SubscriptionHelper.CANCELLED)
            try {
                onComplete.invoke()

                onEnd()
            } catch (e: Throwable) {
                Exceptions.throwIfFatal(e)
                RxJavaPlugins.onError(e)
            }
        }
    }

    open fun onEnd() {
        onObserverEnd.invoke(_lastData, _lastError)
    }

    //</editor-fold desc="Subscriber 回调">

    //<editor-fold desc="支持 Subscription">

    override fun dispose() {
        cancel()
    }

    override fun isDisposed(): Boolean {
        return get() === SubscriptionHelper.CANCELLED
    }

    override fun request(n: Long) {
        get().request(n)
    }

    override fun cancel() {
        SubscriptionHelper.cancel(this)
    }

    //</editor-fold desc="支持 Subscription">
}

/**返回一个带有[dispose]方法的对象*/
fun <T> Flowable<T>.observer(observer: BaseFlowableSubscriber<T>): Disposable {
    subscribe(observer)
    return observer
}