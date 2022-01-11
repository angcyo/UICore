package com.angcyo.http.rx

import com.angcyo.library.L
import com.angcyo.library.ex.simpleHash
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.exceptions.CompositeException
import io.reactivex.exceptions.Exceptions
import io.reactivex.internal.disposables.DisposableHelper
import io.reactivex.plugins.RxJavaPlugins
import java.util.concurrent.atomic.AtomicReference

/**
 * 观察者 [io.reactivex.internal.observers.LambdaObserver]
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/25
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

open class BaseObserver<T> : AtomicReference<Disposable>(),
    Observer<T>, Disposable {

    /**自动处置*/
    var autoDispose = true

    //<editor-fold desc="Dsl">

    var onSubscribe: (Disposable) -> Unit = {
        L.d("${this.simpleHash()}#onSubscribe")
    }

    var onNext: (T) -> Unit = {
        L.d("${this.simpleHash()}#onNext:$it")
    }

    var onError: (Throwable) -> Unit = {
        L.e("${this.simpleHash()}#onError:$it")
        it.printStackTrace()
    }

    var onComplete: () -> Unit = {
        L.d("${this.simpleHash()}#onComplete")
    }

    /**不管是成功, 还是失败, 都会触发的回调*/
    var onObserverEnd: ((data: T?, error: Throwable?) -> Unit)? = { data, error ->
        val string = buildString {
            appendln().appendln("订阅结束#onObserverEnd:")
            append("data->").appendln(data)
            append("error->").appendln(error)
        }
        error?.run { L.e(string);printStackTrace() } ?: L.i(string)
    }

    //</editor-fold desc="Dsl">

    //<editor-fold desc="Observer 回调">

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

    val _lastData: T? get() = observerDataList.lastOrNull()
    val observerDataList = mutableListOf<T>()
    override fun onNext(data: T) {
        if (!isDisposed) {
            try {
                observerDataList.add(data)
                onNext.invoke(data)
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
                e.printStackTrace()
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
                e.printStackTrace()
                Exceptions.throwIfFatal(e)
                RxJavaPlugins.onError(e)
            }
        }
    }

    //</editor-fold desc="Observer 回调">

    open fun onEnd() {
        try {
            onObserverEnd?.invoke(_lastData, _lastError)
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                onError.invoke(e)
                onObserverEnd?.invoke(null, e)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } finally {
            if (autoDispose) {
                dispose()
            }
        }
    }

    //<editor-fold desc="支持 Disposable">

    override fun dispose() {
        DisposableHelper.dispose(this)
    }

    override fun isDisposed(): Boolean {
        return get() === DisposableHelper.DISPOSED
    }

    //</editor-fold desc="支持 Disposable">
}

/**返回一个带有[dispose]方法的对象*/
fun <T> Observable<T>.observer(observer: BaseObserver<T>): Disposable {
    subscribe(observer)
    return observer
}

/**[observer] DSl*/
fun <T> Observable<T>.observer(action: BaseObserver<T>.() -> Unit = {}): Disposable {
    val observer = BaseObserver<T>()
    observer.action()
    subscribe(observer)
    return observer
}

/**快速监听*/
fun <T> Observable<T>.observe(
    config: BaseObserver<T>.() -> Unit = {},
    end: (data: T?, error: Throwable?) -> Unit
): Disposable {
    return observer(BaseObserver<T>().apply {
        config()
        onObserverEnd = end
    })
}