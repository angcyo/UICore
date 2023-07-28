package com.angcyo.library.component

/**
 * 可取消的接口
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2023/04/09
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
interface ICancel {

    /**[data] 取消携带的数据*/
    fun cancel(data: Any? = null)

}