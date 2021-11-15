package com.angcyo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/**
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

    override fun setValue(value: T?) {
        lastError = _postlastError
        isSetValue = true
        _postlastError = null
        super.setValue(value)
    }

    fun setValue(value: T?, error: Throwable?) {
        lastError = error
        _postlastError = null
        isSetValue = true
        super.setValue(value)
    }

    //-----

    override fun postValue(value: T?) {
        _postlastError = null
        super.postValue(value)
    }

    fun postValue(value: T?, error: Throwable?) {
        _postlastError = error
        super.postValue(value)
    }
}