package com.angcyo.core.lifecycle

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.angcyo.library.ICancelCallback
import com.angcyo.library.L
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.internal.disposables.DisposableContainer

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/09
 */

class CompositeDisposableLifecycle(lifecycleOwner: LifecycleOwner) : Disposable,
    DisposableContainer, LifecycleEventObserver, ICancelCallback {

    private var compositeDisposable: CompositeDisposable = CompositeDisposable()
        get() {
            val value = field
            if (value.isDisposed) {
                field = CompositeDisposable()
            }
            return field
        }

    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    override fun isDisposed(): Boolean = compositeDisposable.isDisposed

    override fun dispose() {
        L.w("取消Rx调度!")
        compositeDisposable.dispose()
    }

    override fun add(disposable: Disposable): Boolean = compositeDisposable.add(disposable)

    override fun remove(disposable: Disposable): Boolean = compositeDisposable.remove(disposable)

    override fun delete(disposable: Disposable): Boolean = compositeDisposable.delete(disposable)

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_DESTROY) {
            //销毁的时候, 自动取消所有Rx2
            dispose()
        }
    }

    override fun onCancelCallback(reason: Int) {
        dispose()
    }
}