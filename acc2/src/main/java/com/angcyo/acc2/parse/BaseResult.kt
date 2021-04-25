package com.angcyo.acc2.parse

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/01/30
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
open class BaseResult {
    /**是否处理成功*/
    var success: Boolean = false

    /**将自己的值, 赋值给[target]*/
    open fun copyTo(target: BaseResult) {
        target.success = success
    }

    open fun isSuccessResult() = success
}