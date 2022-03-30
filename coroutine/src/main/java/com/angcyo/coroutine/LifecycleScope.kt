package com.angcyo.coroutine

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

/**
 * 具有生命周期感知的协程作用域[CoroutineScope]
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/14
 */

open class LifecycleScope(
    lifecycleOwner: LifecycleOwner,
    cancelLifeEvent: Lifecycle.Event = Lifecycle.Event.ON_DESTROY
) : AndroidScope(lifecycleOwner, cancelLifeEvent) {

    override fun onHandleException(context: CoroutineContext, exception: Throwable) {
        super.onHandleException(context, exception)
    }
}
