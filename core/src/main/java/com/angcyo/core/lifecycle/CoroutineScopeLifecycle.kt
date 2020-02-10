package com.angcyo.core.lifecycle

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.angcyo.library.ICancelCallback
import com.angcyo.library.L
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/09
 */

class CoroutineScopeLifecycle(lifecycleOwner: LifecycleOwner) : LifecycleEventObserver,
    ICancelCallback {

    /**异常处理回调*/
    var onException: (context: CoroutineContext, exception: Throwable) -> Unit =
        { context, exception ->
            L.e("协程内异常:$context $exception")
        }

    //协程异常上下文捕捉
    private val exceptionHandler = CoroutineExceptionHandler { context, throwable ->
        onException(context, throwable)
    }

    //协程上下文
    private val coroutineContext: CoroutineContext =
        Dispatchers.Main + exceptionHandler + SupervisorJob()

    //协程域
    private var coroutineScope: CoroutineScope = CoroutineScope(coroutineContext)
        get() {
            val value = field
            if (!value.isActive) {
                field = CoroutineScope(coroutineContext)
            }
            return field
        }

    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_DESTROY) {
            //销毁的时候, 自动取消所有协程
            cancel()
        }
    }

    /**需要取消协程*/
    fun cancel() {
        coroutineScope.cancel()
    }

    override fun onCancelCallback(reason: Int) {
        cancel()
    }

    /**启动协程域*/
    fun launch(block: suspend CoroutineScope.() -> Unit) {
        coroutineScope.launch(block = block)
    }
}