package com.angcyo.viewmodel

import android.os.Looper
import androidx.annotation.AnyThread
import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/09
 */

//<editor-fold desc="observe">

/**永久[LiveData], 直到手动移除观察者
 * [autoClear] 收到有效数据后, 是否自动清除数据
 * [allowBackward] 是否允许数据倒灌, 接收到旧数据
 * */
@MainThread
fun <T> LiveData<T>.observeForever(
    autoClear: Boolean = false,
    allowBackward: Boolean = true,
    action: (data: T?) -> Unit
): Observer<T?> {
    val result: Observer<T?>
    var isFirst = if (allowBackward) value != null else true
    observeForever(Observer<T?> {
        if (allowBackward) {
            action(it)
        } else {
            //不允许数据倒灌
            if (!isFirst) {
                action(it)
            }
            isFirst = false
        }
        if (it != null && autoClear && this is MutableLiveData) {
            postValue(null)
        }
    }.apply {
        result = this
    })
    return result
}

/**[observeForever]*/
@MainThread
fun <T> LiveData<T>.watchForever(
    autoClear: Boolean = false,
    allowBackward: Boolean = true,
    action: (data: T?) -> Unit
): Observer<T?> = observeForever(autoClear, allowBackward, action)

/**快速观察[LiveData]
 * [autoClear] 收到数据后, 是否要情况数据
 * [allowBackward] 是否允许数据倒灌, 接收到旧数据
 * */
@MainThread
fun <T> LiveData<T>.observe(
    owner: LifecycleOwner,
    autoClear: Boolean = false,
    allowBackward: Boolean = true,
    action: (data: T?) -> Unit
): Observer<T?> {
    val result: Observer<T?>
    var isFirst = if (allowBackward) value != null else true
    observe(owner, Observer<T?> {
        if (allowBackward) {
            action(it)
        } else {
            //不允许数据倒灌
            if (!isFirst) {
                action(it)
            }
            isFirst = false
        }
        if (it != null && autoClear && this is MutableLiveData) {
            postValue(null)
        }
    }.apply {
        result = this
    })
    return result
}

/**[observe]*/
@MainThread
fun <T> LiveData<T>.watch(
    owner: LifecycleOwner,
    autoClear: Boolean = false,
    allowBackward: Boolean = true,
    action: (data: T?) -> Unit
): Observer<T?> = observe(owner, autoClear, allowBackward, action)

/**快速观察[LiveData]一次, 确保不收到null数据
 * [action] 返回值表示是否处理了数据, 如果没有处理, 则不会remove
 * [allowBackward] 是否允许数据倒灌, 接收到旧数据
 * */
@MainThread
fun <T> LiveData<T>.observeOnce(
    owner: LifecycleOwner? = null,
    allowBackward: Boolean = true,
    action: (data: T?) -> Boolean
): Observer<T?> {
    var result: Observer<T?>? = null
    var isFirst = if (allowBackward) value != null else true
    var isNotify = false
    if (owner == null) {
        observeForever(Observer<T?> {
            if (allowBackward) {
                isNotify = action(it)
            } else {
                //不允许数据倒灌
                if (!isFirst) {
                    isNotify = action(it)
                }
                isFirst = false
            }
            if (isNotify) {
                if (it is List<*>) {
                    if (it.isNotEmpty()) {
                        removeObserver(result!!)
                    }
                } else if (it != null) {
                    removeObserver(result!!)
                }
            }
        }.apply {
            result = this
        })
    } else {
        observe(owner, Observer<T?> {
            if (allowBackward) {
                isNotify = action(it)
            } else {
                //不允许数据倒灌
                if (!isFirst) {
                    isNotify = action(it)
                }
                isFirst = false
            }
            if (isNotify) {
                if (it is List<*>) {
                    if (it.isNotEmpty()) {
                        removeObserver(result!!)
                    }
                } else if (it != null) {
                    removeObserver(result!!)
                }
            }
        }.apply {
            result = this
        })
    }
    return result!!
}

/**[observeOnce]*/
@MainThread
fun <T> LiveData<T>.watchOnce(
    owner: LifecycleOwner? = null,
    allowBackward: Boolean = true,
    action: (data: T?) -> Boolean
): Observer<T?> = observeOnce(owner, allowBackward, action)

//</editor-fold desc="observe">

//<editor-fold desc="LiveData">

/**快速创建一个可以修改的[MutableLiveData]*/
fun <T> vmData(data: T) = MutableErrorLiveData(data)

fun <T> vmDataNull(data: T? = null) = MutableErrorLiveData(data)

fun <T> vmHoldDataNull(data: T? = null) = MutableHoldLiveData(data)

/**更新自己
 * [updateValue]*/
@AnyThread
fun <T> MutableLiveData<T>.updateThis() {
    updateValue(value)
}

/**[MutableLiveData]在主线程更新值*/
//@AnyThread
fun <T> MutableLiveData<T>.updateValue(value: T?) {
    if (Looper.getMainLooper() == Looper.myLooper()) {
        this.value = value
    } else {
        postValue(value)
    }
}

/**[MutableLiveData]在主线程通知更新值*/
//@AnyThread
fun <T> MutableLiveData<T>.notify() {
    if (Looper.getMainLooper() == Looper.myLooper()) {
        this.value = value
    } else {
        notifyPost()
    }
}

@AnyThread
fun <T> MutableLiveData<T>.notifyPost() {
    postValue(value)
}

/**数据通知专用[LiveData]*/
fun <T> vmDataOnce(data: T? = null) = MutableOnceLiveData(data)

//</editor-fold desc="LiveData">
