package com.angcyo.lifecycle

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner

/**
 * [androidx.lifecycle.LifecycleObserver]
 * [androidx.lifecycle.DefaultLifecycleObserver]
 * [androidx.lifecycle.LifecycleEventObserver]
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/07/15
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */

/**生命周期快速监听
 * [action] 返回true, 表示调用完之后移除观察*/
fun Lifecycle.on(
    event: Lifecycle.Event,
    forever: Boolean = false,
    action: () -> Boolean
): LifecycleEventObserver {
    return onStateChanged(forever) { source, en, observer ->
        if (en == event || event == Lifecycle.Event.ON_ANY) {
            if (action()) {
                removeObserver(observer)
            }
        }
    }
}

fun Lifecycle.onDestroy(forever: Boolean = false, action: () -> Boolean) =
    on(Lifecycle.Event.ON_DESTROY, forever, action)

fun Lifecycle.onPause(forever: Boolean = false, action: () -> Boolean) =
    on(Lifecycle.Event.ON_PAUSE, forever, action)

fun Lifecycle.onCreate(forever: Boolean = false, action: () -> Boolean) =
    on(Lifecycle.Event.ON_CREATE, forever, action)

fun Lifecycle.onStop(forever: Boolean = false, action: () -> Boolean) =
    on(Lifecycle.Event.ON_STOP, forever, action)

fun Lifecycle.onStart(forever: Boolean = false, action: () -> Boolean) =
    on(Lifecycle.Event.ON_START, forever, action)

fun Lifecycle.onAny(forever: Boolean = false, action: () -> Boolean) =
    on(Lifecycle.Event.ON_ANY, forever, action)

/**监听生命周期改变*/
fun Lifecycle.onStateChanged(
    forever: Boolean = false,
    action: (source: LifecycleOwner, event: Lifecycle.Event, observer: LifecycleObserver) -> Unit
): LifecycleEventObserver {
    val observer = object : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            action(source, event, this)
            if (!forever && event == Lifecycle.Event.ON_DESTROY) {
                //销毁之后, 自动移除
                removeObserver(this)
            }
        }
    }
    addObserver(observer)
    return observer
}

/*-----------------------------------LifecycleOwner------------------------------------*/

fun LifecycleOwner.on(
    event: Lifecycle.Event,
    forever: Boolean = false,
    action: () -> Boolean
): LifecycleEventObserver {
    return lifecycle.on(event, forever, action)
}

fun LifecycleOwner.onDestroy(forever: Boolean = false, action: () -> Boolean) =
    on(Lifecycle.Event.ON_DESTROY, forever, action)

fun LifecycleOwner.onPause(forever: Boolean = false, action: () -> Boolean) =
    on(Lifecycle.Event.ON_PAUSE, forever, action)

fun LifecycleOwner.onCreate(forever: Boolean = false, action: () -> Boolean) =
    on(Lifecycle.Event.ON_CREATE, forever, action)

fun LifecycleOwner.onStop(forever: Boolean = false, action: () -> Boolean) =
    on(Lifecycle.Event.ON_STOP, forever, action)

fun LifecycleOwner.onStart(forever: Boolean = false, action: () -> Boolean) =
    on(Lifecycle.Event.ON_START, forever, action)

fun LifecycleOwner.onAny(forever: Boolean = false, action: () -> Boolean) =
    on(Lifecycle.Event.ON_ANY, forever, action)