package com.angcyo.lifecycle

import androidx.lifecycle.*
import com.angcyo.library.component.ICancel

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

//<editor-fold desc="Lifecycle">

/**生命周期快速监听
 * [action] 返回true, 表示调用完之后移除观察*/
fun Lifecycle.on(
    event: Lifecycle.Event,
    forever: Boolean = false,
    action: () -> Boolean
): LifecycleEventObserver {
    return onStateChanged(forever) { source, en, observer ->
        var remove = false
        if (en == event || event == Lifecycle.Event.ON_ANY) {
            remove = action()
            if (remove) {
                removeObserver(observer)
            }
        }
        remove
    }
}

/**监听生命周期回调, 并且支持自动移除*/
fun Lifecycle.onCreate(forever: Boolean = false, action: () -> Boolean) =
    on(Lifecycle.Event.ON_CREATE, forever, action)

fun Lifecycle.onStart(forever: Boolean = false, action: () -> Boolean) =
    on(Lifecycle.Event.ON_START, forever, action)

fun Lifecycle.onResume(forever: Boolean = false, action: () -> Boolean) =
    on(Lifecycle.Event.ON_RESUME, forever, action)

fun Lifecycle.onPause(forever: Boolean = false, action: () -> Boolean) =
    on(Lifecycle.Event.ON_PAUSE, forever, action)

fun Lifecycle.onStop(forever: Boolean = false, action: () -> Boolean) =
    on(Lifecycle.Event.ON_STOP, forever, action)

fun Lifecycle.onDestroy(forever: Boolean = false, action: () -> Boolean) =
    on(Lifecycle.Event.ON_DESTROY, forever, action)

fun Lifecycle.onAny(forever: Boolean = false, action: () -> Boolean) =
    on(Lifecycle.Event.ON_ANY, forever, action)

/**监听生命周期改变*/
fun Lifecycle.onStateChanged(
    forever: Boolean = false,
    action: (source: LifecycleOwner, event: Lifecycle.Event, observer: LifecycleObserver) -> Boolean
): LifecycleEventObserver {
    val observer = object : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            var remove = action(source, event, this)
            if (remove && !forever && event == Lifecycle.Event.ON_DESTROY) {
                //销毁之后, 自动移除
                removeObserver(this)
            }
        }
    }
    addObserver(observer)
    return observer
}

/**当前的声明周期是否销毁了*/
fun Lifecycle.isDestroy() = currentState == Lifecycle.State.DESTROYED

//</editor-fold desc="Lifecycle">

//<editor-fold desc="LifecycleOwner">

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

//</editor-fold desc="LifecycleOwner">

//<editor-fold desc="Other">

/**销毁的时候, 自动取消*/
fun ICancel.cancelOnDestroy(lifecycleOwner: LifecycleOwner) {
    lifecycleOwner.onDestroy {
        cancel()
        true
    }
}

//</editor-fold desc="Other">
