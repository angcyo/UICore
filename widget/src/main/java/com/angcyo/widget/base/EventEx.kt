package com.angcyo.widget.base

import android.view.MotionEvent

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/20
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

fun MotionEvent.isTouchDown(): Boolean {
    return actionMasked == MotionEvent.ACTION_DOWN
}

fun MotionEvent.isTouchFinish(): Boolean {
    return actionMasked == MotionEvent.ACTION_UP || actionMasked == MotionEvent.ACTION_CANCEL
}