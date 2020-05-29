package com.angcyo.viewmodel

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/09
 */

/**快速观察[LiveData]*/
fun <T> LiveData<T>.observeForever(action: (data: T?) -> Unit): Observer<T> {
    val result: Observer<T>
    observeForever(Observer<T> { action(it) }.apply {
        result = this
    })
    return result
}

/**快速观察[LiveData]*/
fun <T> LiveData<T>.observe(owner: LifecycleOwner, action: (data: T?) -> Unit): Observer<T> {
    val result: Observer<T>
    observe(owner, Observer<T> { action(it) }.apply {
        result = this
    })
    return result
}

/**快速观察[LiveData]一次*/
fun <T> LiveData<T>.observeOnce(owner: LifecycleOwner, action: (data: T?) -> Unit): Observer<T> {
    var result: Observer<T>? = null
    observe(owner, Observer<T> {
        removeObserver(result!!)
        action(it)
    }.apply {
        result = this
    })
    return result!!
}
