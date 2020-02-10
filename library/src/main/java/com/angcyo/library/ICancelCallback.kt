package com.angcyo.library

/**
 * 定义一个可以被取消的操作
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/09
 */
interface ICancelCallback {

    /**
     * 需要取消操作
     * @param reason 取消的原因
     * */
    fun onCancelCallback(reason: Int)
}