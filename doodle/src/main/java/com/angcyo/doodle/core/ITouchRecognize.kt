package com.angcyo.doodle.core

import android.view.MotionEvent

/**
 * 手势识别
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/07/25
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
interface ITouchRecognize {

    fun onTouchRecognize(manager: DoodleTouchManager, event: MotionEvent): Boolean

}