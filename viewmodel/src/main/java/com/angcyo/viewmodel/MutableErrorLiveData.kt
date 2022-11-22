package com.angcyo.viewmodel

import androidx.annotation.AnyThread
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/**
 * 连续调用[postValue], 只有最后一次才会收到回调.
 * 如果想要及时收到回调, 请使用[setValue]
 *
 * 支持错误信息的[LiveData]
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/11/11
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
open class MutableErrorLiveData<T>(value: T? = null) : MutableLiveData<T>(value) {

    /**最后一次是否有错误*/
    var lastError: Throwable? = null
    var _postlastError: Throwable? = null

    /**是否设置过值*/
    var isSetValue: Boolean = false

    //-----

    @MainThread
    override fun setValue(value: T?) {
        lastError = _postlastError
        isSetValue = true
        _postlastError = null
        super.setValue(value)
    }

    @MainThread
    fun setValue(value: T?, error: Throwable?) {
        lastError = error
        _postlastError = null
        isSetValue = true
        super.setValue(value)
    }

    //-----

    @AnyThread
    override fun postValue(value: T?) {
        _postlastError = null
        super.postValue(value)
    }

    @AnyThread
    fun postValue(value: T?, error: Throwable?) {
        _postlastError = error
        super.postValue(value)
    }
}