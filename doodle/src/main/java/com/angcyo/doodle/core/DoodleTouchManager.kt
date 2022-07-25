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

    /**手势识别, 比如画笔, 橡皮擦等*/
    var touchRecognize: ITouchRecognize? = null

    @CallPoint
    fun onTouchEvent(event: MotionEvent): Boolean {
        touchRecognize?.onTouchRecognize(this, event)
        return true
    }

    //region ---recognize---

    /**更新操作的笔刷, 橡皮擦等*/
    fun updateTouchRecognize(recognize: ITouchRecognize?) {
        val old = touchRecognize
        touchRecognize = recognize
        if (old != recognize) {
            doodleDelegate.dispatchTouchRecognizeChanged(old, touchRecognize)
        }
    }

    //endregion ---recognize---
}