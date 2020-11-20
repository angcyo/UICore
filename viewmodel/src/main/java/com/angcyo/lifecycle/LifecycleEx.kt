package com.angcyo.lifecycle

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/07/15
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */

/**生命周期快速监听
 * [action] 返回true, 表示调用完之后移除观察*/
fun Lifecycle.on(event: Lifecycle.Event, action: () -> Boolean): LifecycleEventObserver {

    val observer = object : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, en: Lifecycle.Event) {
            if (en == event) {
                if (action()) {
                    removeObserver(this)
                }
            }
        }
    }

    addObserver(observer)

    return observer
}

fun Lifecycle.onDestroy(action: () -> Boolean) = on(Lifecycle.Event.ON_DESTROY, action)
fun Lifecycle.onPause(action: () -> Boolean) = on(Lifecycle.Event.ON_PAUSE, action)
fun Lifecycle.onCreate(action: () -> Boolean) = on(Lifecycle.Event.ON_CREATE, action)
fun Lifecycle.onStop(action: () -> Boolean) = on(Lifecycle.Event.ON_STOP, action)
fun Lifecycle.onStart(action: () -> Boolean) = on(Lifecycle.Event.ON_START, action)
fun Lifecycle.onAny(action: () -> Boolean) = on(Lifecycle.Event.ON_ANY, action)

/*-----------------------------------LifecycleOwner------------------------------------*/

fun LifecycleOwner.on(event: Lifecycle.Event, action: () -> Boolean): LifecycleEventObserver {
    return lifecycle.on(event, action)
}

fun LifecycleOwner.onDestroy(action: () -> Boolean) = on(Lifecycle.Event.ON_DESTROY, action)
fun LifecycleOwner.onPause(action: () -> Boolean) = on(Lifecycle.Event.ON_PAUSE, action)
fun LifecycleOwner.onCreate(action: () -> Boolean) = on(Lifecycle.Event.ON_CREATE, action)
fun LifecycleOwner.onStop(action: () -> Boolean) = on(Lifecycle.Event.ON_STOP, action)
fun LifecycleOwner.onStart(action: () -> Boolean) = on(Lifecycle.Event.ON_START, action)
fun LifecycleOwner.onAny(action: () -> Boolean) = on(Lifecycle.Event.ON_ANY, action)