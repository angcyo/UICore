package com.angcyo.http.rx

import com.angcyo.library.L
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
    
    var onSubscribe: (Disposable) -> Unit = {}
    var onNext: (T) -> Unit = {}
    var onError: (Throwable) -> Unit = {}
    var onComplete: () -> Unit = {}

    /**不管是成功, 还是失败, 都会触发的回调*/
    var onObserverEnd: (data: T?, error: Throwable?) -> Unit = { data, error ->
        L.i(buildString {
            appendln().appendln("订阅结束#onObserverEnd:")
            append("data->").appendln(data)
            append("error->").appendln(error)
        })
        error?.printStackTrace()
    }

    override fun onSubscribe(d: Disposable) {
        L.d("${this.javaClass.name}#onSubscribe")
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
    override fun onNext(t: T) {
        L.i("${this.javaClass.name}#onNext")
        if (!isDisposed) {
            try {
                _lastData = t
                onNext.invoke(t)
            } catch (e: Throwable) {
                Exceptions.throwIfFatal(e)
                get()!!.dispose()
                onError(e)
            }
        }
    }

    var _lastError: Throwable? = null
    override fun onError(t: Throwable) {
        L.e("${this.javaClass.name}#onError")
        if (!isDisposed) {
            lazySet(DisposableHelper.DISPOSED)
            try {
                _lastError = t
                onError.invoke(t)

                onObserverEnd.invoke(_lastData, _lastError)
            } catch (e: Throwable) {
                Exceptions.throwIfFatal(e)
                RxJavaPlugins.onError(CompositeException(t, e))
            }
        } else {
            RxJavaPlugins.onError(t)
        }
    }

    override fun onComplete() {
        L.d("${this.javaClass.name}#onComplete")
        if (!isDisposed) {
            lazySet(DisposableHelper.DISPOSED)
            try {
                onComplete.invoke()

                onObserverEnd.invoke(_lastData, _lastError)
            } catch (e: Throwable) {
                Exceptions.throwIfFatal(e)
                RxJavaPlugins.onError(e)
            }
        }
    }

    override fun dispose() {
        DisposableHelper.dispose(this)
    }

    override fun isDisposed(): Boolean {
        return get() === DisposableHelper.DISPOSED
    }

    companion object {
        private const val serialVersionUID = -7251123623727029452L
    }
}