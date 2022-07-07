package com.angcyo.viewmodel

import androidx.lifecycle.MutableLiveData

/**
 * 每次设置新的值时, 都会保存一份之前的值
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/07/06
 */
open class MutableHoldLiveData<T>(value: T? = null) : MutableLiveData<T>(value) {

    /**之前的值*/
    var beforeValue: T? = null

    override fun setValue(value: T) {
        //保存之前的值
        beforeValue = getValue()
        super.setValue(value)
    }

}