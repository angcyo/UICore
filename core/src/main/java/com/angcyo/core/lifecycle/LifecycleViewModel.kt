package com.angcyo.core.lifecycle

import android.annotation.SuppressLint
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModel
import com.angcyo.http.rx.BaseObserver
import com.angcyo.http.rx.doMain
import com.angcyo.viewmodel.IViewModel
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.CoroutineScope

/**
 * [LiveData]需要在主线程setValue, 子线程请使用postValue
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/16
 */
open class LifecycleViewModel : ViewModel(), IViewModel, LifecycleOwner {

    //生命周期发射器
    @SuppressLint("StaticFieldLeak")
    private val lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)

    init {
        setCurrentState(Lifecycle.State.CREATED)
    }

    fun setCurrentState(state: Lifecycle.State) {
        doMain(true) {
            lifecycleRegistry.currentState = state
        }
    }

    /**clear*/
    override fun onCleared() {
        super.onCleared()
        setCurrentState(Lifecycle.State.DESTROYED)
        release()
    }

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }

    /**取消所有Rx 和 协程 / 恢复初始状态*/
    override fun reset() {
        if (lifecycleRegistry.currentState == Lifecycle.State.DESTROYED) {
            setCurrentState(Lifecycle.State.CREATED)
        } else {
            cancel()
        }
    }

    /**取消所有Rx 和 协程*/
    override fun cancel() {
        cancelSchedule()
    }

    /**释放资源, 比如切换了登录的账号等*/
    override fun release() {
        cancel()
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