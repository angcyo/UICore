package com.angcyo.coroutine

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.angcyo.library.L
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlin.coroutines.CoroutineContext

/**
 * Android协程域
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/16
 */

open class AndroidScope(
    val lifecycleOwner: LifecycleOwner? = null,
    val cancelLifeEvent: Lifecycle.Event = Lifecycle.Event.ON_DESTROY
) : CoroutineScope, LifecycleEventObserver {

    /**协程异常处理*/
    val exceptionHandler = CoroutineErrorHandler { context, throwable ->
        onHandleException(context, throwable)
    }

    /**协程上下文*/
    override val coroutineContext: CoroutineContext =
        Dispatchers.Main + exceptionHandler + SupervisorJob()

    /**异常回调*/
    var onException: (context: CoroutineContext, exception: Throwable) -> Unit =
        { context, exception ->
            L.e("$context $exception")
        }

    init {
        lifecycleOwner?.lifecycle?.addObserver(this)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (cancelLifeEvent == event) {
            onCancel()
        }
    }

    /**处理协程内的异常回调*/
    open fun onHandleException(context: CoroutineContext, exception: Throwable) {
        onException(context, exception)
    }

    /**需要取消协程*/
    open fun onCancel() {
        lifecycleOwner?.lifecycle?.removeObserver(this)
        cancel()
    }

}