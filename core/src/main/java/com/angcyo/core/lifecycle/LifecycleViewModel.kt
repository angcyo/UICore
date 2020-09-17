package com.angcyo.core.lifecycle

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModel
import com.angcyo.http.rx.BaseObserver
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.CoroutineScope

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/16
 */
open class LifecycleViewModel : ViewModel(), LifecycleOwner {

    //生命周期发射器
    private val lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)

    init {
        setCurrentState(Lifecycle.State.CREATED)
    }

    fun setCurrentState(state: Lifecycle.State) {
        lifecycleRegistry.currentState = state
    }

    /**clear*/
    override fun onCleared() {
        super.onCleared()
        setCurrentState(Lifecycle.State.DESTROYED)
    }

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }

    /**取消所有Rx 和 协程 / 恢复初始状态*/
    open fun reset() {
        if (lifecycleRegistry.currentState == Lifecycle.State.DESTROYED) {
            setCurrentState(Lifecycle.State.CREATED)
        } else {
            cancel()
        }
    }

    /**取消所有Rx 和 协程*/
    open fun cancel() {
        onCleared()
    }

    //<editor-fold desc="Rx 协程">

    //Rx调度管理
    val compositeDisposableLifecycle: CompositeDisposableLifecycle by lazy {
        CompositeDisposableLifecycle(this)
    }

    //协程域管理
    val coroutineScopeLifecycle: CoroutineScopeLifecycle by lazy {
        CoroutineScopeLifecycle(this)
    }

    /**取消所有异步调度*/
    fun cancelSchedule(reason: Int = -1) {
        cancelAllDisposable(reason)
        cancelCoroutineScope(reason)
    }

    /**管理[Disposable]*/
    fun Disposable.attach(): Disposable {
        compositeDisposableLifecycle.add(this)
        return this
    }

    /**取消所有[Disposable]*/
    fun cancelAllDisposable(reason: Int = -1) {
        compositeDisposableLifecycle.onCancelCallback(reason)
    }

    /**取消协程域*/
    fun cancelCoroutineScope(reason: Int = -1) {
        coroutineScopeLifecycle.onCancelCallback(reason)
    }

    /**启动一个具有生命周期的协程域*/
    fun launchLifecycle(block: suspend CoroutineScope.() -> Unit) =
        coroutineScopeLifecycle.launch(block)

    /**快速订阅和管理[Observable]*/
    fun <T> Observable<T>.observer(observer: BaseObserver<T>): Disposable {
        subscribe(observer)
        observer.attach()
        return observer
    }

    //</editor-fold desc="Rx 协程">
}