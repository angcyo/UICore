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

fun LifecycleOwner.on(onEvent: Lifecycle.Event, action: () -> Unit): LifecycleEventObserver {

    val observer = object : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            if (event == onEvent) {
                action()
                lifecycle.removeObserver(this)
            }
        }
    }

    lifecycle.addObserver(observer)

    return observer
}

fun LifecycleOwner.onDestroy(action: () -> Unit) = on(Lifecycle.Event.ON_DESTROY, action)
fun LifecycleOwner.onPause(action: () -> Unit) = on(Lifecycle.Event.ON_PAUSE, action)
fun LifecycleOwner.onCreate(action: () -> Unit) = on(Lifecycle.Event.ON_CREATE, action)
fun LifecycleOwner.onStop(action: () -> Unit) = on(Lifecycle.Event.ON_STOP, action)
fun LifecycleOwner.onStart(action: () -> Unit) = on(Lifecycle.Event.ON_START, action)
fun LifecycleOwner.onAny(action: () -> Unit) = on(Lifecycle.Event.ON_ANY, action)