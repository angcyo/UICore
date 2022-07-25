package com.angcyo.doodle.core

import android.view.MotionEvent
import com.angcyo.doodle.DoodleDelegate
import com.angcyo.library.annotation.CallPoint

/**
 * 涂鸦手势管理, 所有手势由此派发
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/07/25
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class DoodleTouchManager(val doodleDelegate: DoodleDelegate) {

    @CallPoint
    fun onTouchEvent(event: MotionEvent): Boolean {
        return true
    }
}