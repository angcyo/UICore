package com.angcyo.viewmodel

import android.os.Looper
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

/**快速观察[LiveData]*/
fun <T> LiveData<T>.observeForever(
    allowBackward: Boolean = true,
    action: (data: T?) -> Unit
): Observer<T> {
    val result: Observer<T>
    var isFirst = value != null
    observeForever(Observer<T> {
        if (allowBackward) {
            action(it)
        } else {
            //不允许数据倒灌
            if (!isFirst) {
                action(it)
            }
            isFirst = false
        }
    }.apply {
        result = this
    })
    return result
}

/**快速观察[LiveData]
 * [autoClear] 收到数据后, 是否要情况数据
 * [allowBackward] 是否允许数据倒灌, 接收到旧数据
 * */
fun <T> LiveData<T>.observe(
    owner: LifecycleOwner,
    autoClear: Boolean = false,
    allowBackward: Boolean = true,
    action: (data: T?) -> Unit
): Observer<T?> {
    val result: Observer<T?>
    var isFirst = value != null
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

/**快速观察[LiveData]一次
 * [action] 返回值表示是否处理了数据, 如果没有处理, 则不会remove
 * [allowBackward] 是否允许数据倒灌, 接收到旧数据
 * */
fun <T> LiveData<T>.observeOnce(
    owner: LifecycleOwner? = null,
    allowBackward: Boolean = true,
    action: (data: T?) -> Boolean
): Observer<T?> {
    var result: Observer<T?>? = null
    var isFirst = value != null
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

/**快速创建一个可以修改的[MutableLiveData]*/
fun <T> vmData(data: T) = MutableErrorLiveData(data)

fun <T> vmDataNull(data: T? = null) = MutableErrorLiveData(data)

fun <T> vmHoldDataNull(data: T? = null) = MutableHoldLiveData(data)


/**更新自己
 * [updateValue]*/
fun <T> MutableLiveData<T>.updateThis() {
    updateValue(value)
}

/**[MutableLiveData]在主线程更新值*/
fun <T> MutableLiveData<T>.updateValue(value: T?) {
    if (Looper.getMainLooper() == Looper.myLooper()) {
        this.value = value
    } else {
        postValue(value)
    }
}

/**[MutableLiveData]在主线程通知更新值*/
fun <T> MutableLiveData<T>.notify() {
    if (Looper.getMainLooper() == Looper.myLooper()) {
        this.value = value
    } else {
        notifyPost()
    }
}

fun <T> MutableLiveData<T>.notifyPost() {
    postValue(value)
}

/**数据通知专用[LiveData]*/
fun <T> vmDataOnce(data: T? = null) = MutableOnceLiveData(data)