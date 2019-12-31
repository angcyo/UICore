package com.angcyo.http.rx

import com.angcyo.library.L
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.exceptions.CompositeException
import io.reactivex.exceptions.Exceptions
import io.reactivex.internal.disposables.DisposableHelper
import io.reactivex.plugins.RxJavaPlugins
import java.util.concurrent.atomic.AtomicReference

/**
 * 观察者
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/25
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

open class BaseObserver<T> : AtomicReference<Disposable>(), Observer<T>, Disposable {

    var onSubscribe: (Disposable) -> Unit = {
        L.d("${this.javaClass.name}#onSubscribe")
    }

    var onNext: (T) -> Unit = {
        L.d("${this.javaClass.name}#onNext:$it")
    }

    var onError: (Throwable) -> Unit = {
        L.e("${this.javaClass.name}#onError")
    }

    var onComplete: () -> Unit = {
        L.d("${this.javaClass.name}#onComplete")
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

    override fun onSubscribe(d: Disposable) {
        if (DisposableHelper.setOnce(this, d)) {
            try {
                onSubscribe.invoke(this)
            } catch (ex: Throwable) {
                Exceptions.throwIfFatal(ex)
                d.dispose()
                onError(ex)
            }
        }
    }

    var _lastData: T? = null
    val observerDataList = mutableListOf<T>()
    override fun onNext(t: T) {
        if (!isDisposed) {
            try {
                _lastData = t
                observerDataList.add(t)
                onNext.invoke(t)
            } catch (e: Throwable) {
                Exceptions.throwIfFatal(e)
                get()?.dispose()
                onError(e)
            }
        }
    }

    var _lastError: Throwable? = null
    override fun onError(t: Throwable) {
        if (!isDisposed) {
            lazySet(DisposableHelper.DISPOSED)
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
        if (!isDisposed) {
            lazySet(DisposableHelper.DISPOSED)
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

    override fun dispose() {
        DisposableHelper.dispose(this)
    }

    override fun isDisposed(): Boolean {
        return get() === DisposableHelper.DISPOSED
    }
}

/**返回一个带有[dispose]方法的对象*/
fun <T> Observable<T>.observer(observer: BaseObserver<T>): Disposable {
    subscribe(observer)
    return observer
}