package com.angcyo.viewmodel

/**
 * 用来做数据通知, 不做数据存储
 * 数据被设置之后, 立马就会被清空
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/11/15
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class MutableOnceLiveData<T>(value: T? = null) : MutableErrorLiveData<T>(value) {

    /**立马清空数据*/
    override fun setValue(value: T?) {
        super.setValue(value)
        if (value != null) {
            postValue(null)
        }
    }

}