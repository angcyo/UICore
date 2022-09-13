package com.angcyo.viewmodel

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer

/**
 * 用来做数据通知, 不做数据存储
 * 数据被设置之后, 立马就会被清空
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/11/15
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class MutableOnceLiveData<T>(value: T? = null) : MutableErrorLiveData<T>(value) {

    /**存储最后一次不为空的值*/
    var lastValue: T? = null

    /**立马清空数据*/
    override fun setValue(value: T?) {
        if (getValue() == null && value == null) {
            return
        }
        super.setValue(value)
        if (value != null) {
            lastValue = value
            if (hasActiveObservers()) {
                //如果有观察者, 则清空数据
                postValue(null)
            }
        }
    }

    override fun observe(owner: LifecycleOwner, observer: Observer<in T?>) {
        super.observe(owner, observer)
        if (value != null && hasActiveObservers()) {
            //清空数据
            postValue(null)
        }
    }

    override fun observeForever(observer: Observer<in T?>) {
        super.observeForever(observer)
        if (value != null && hasActiveObservers()) {
            //清空数据
            postValue(null)
        }
    }

}