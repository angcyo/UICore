package com.angcyo.viewmodel

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

/**快速观察[LiveData]一次*/
fun <T> LiveData<T>.observeOnce(
    owner: LifecycleOwner? = null,
    allowBackward: Boolean = true,
    action: (data: T?) -> Unit
): Observer<T?> {
    var result: Observer<T?>? = null
    var isFirst = value != null
    if (owner == null) {
        observeForever(Observer<T?> {
            if (it is List<*>) {
                if (it.isNotEmpty()) {
                    removeObserver(result!!)
                }
            } else if (it != null) {
                removeObserver(result!!)
            }
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
    } else {
        observe(owner, Observer<T?> {
            if (it is List<*>) {
                if (it.isNotEmpty()) {
                    removeObserver(result!!)
                }
            } else if (it != null) {
                removeObserver(result!!)
            }
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
    }
    return result!!
}

/**快速创建一个可以修改的[MutableLiveData]*/
fun <T> vmData(data: T) = MutableErrorLiveData(data)

fun <T> vmDataNull(data: T? = null) = MutableErrorLiveData(data)

fun <T> vmHoldDataNull(data: T? = null) = MutableHoldLiveData(data)

fun <T> MutableLiveData<T>.notify() {
    value = value
}

fun <T> MutableLiveData<T>.notifyPost() {
    postValue(value)
}

/**数据通知专用[LiveData]*/
fun <T> vmDataOnce(data: T? = null) = MutableOnceLiveData(data)