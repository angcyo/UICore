package com.angcyo.drawable.base

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/04/16
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
abstract class BaseProgressDrawable : AbsDslDrawable() {

    /**不确定的进度*/
    var isIndeterminate: Boolean = true
        set(value) {
            field = value
            invalidateSelf()
        }

    /**当前的进度
     * [0~100]*/
    var progress: Int = 0
        set(value) {
            field = value
            invalidateSelf()
        }

}