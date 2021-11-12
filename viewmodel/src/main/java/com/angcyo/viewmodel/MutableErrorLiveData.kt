package com.angcyo.viewmodel

import androidx.lifecycle.MutableLiveData

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/11/11
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class MutableErrorLiveData<T>(value: T? = null) : MutableLiveData<T>(value) {

    /**最后一次是否有错误*/
    var lastError: Throwable? = null

    /**是否设置过值*/
    var isSetValue: Boolean = false

    override fun setValue(value: T?) {
        lastError = null
        isSetValue = true
        super.setValue(value)
    }

    override fun postValue(value: T?) {
        lastError = null
        isSetValue = true
        super.postValue(value)
    }

    fun setValue(value: T?, error: Throwable?) {
        lastError = error
        isSetValue = true
        super.setValue(value)
    }

    fun postValue(value: T?, error: Throwable?) {
        lastError = error
        isSetValue = true
        super.postValue(value)
    }
}