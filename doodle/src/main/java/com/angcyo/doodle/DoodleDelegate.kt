package com.angcyo.doodle

import android.graphics.Canvas
import android.view.MotionEvent
import android.view.View
import com.angcyo.doodle.core.DoodleLayerManager
import com.angcyo.doodle.core.DoodleTouchManager
import com.angcyo.doodle.core.DoodleViewBox
import com.angcyo.doodle.core.IDoodleView
import com.angcyo.library.annotation.CallPoint
import com.angcyo.library.ex.longFeedback

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/07/25
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class DoodleDelegate(val view: View) : IDoodleView {

    //region ---核心成员---

    /**视口*/
    var viewBox = DoodleViewBox(this)

    /**手势管理*/
    var doodleTouchManager = DoodleTouchManager(this)

    /**图层管理*/
    var doodleLayerManager = DoodleLayerManager(this)

    //endregion ---核心成员---

    @CallPoint
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        viewBox.onSizeChanged(w, h, oldw, oldh)
    }

    @CallPoint
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return doodleTouchManager.onTouchEvent(event)
    }

    @CallPoint
    override fun onDraw(canvas: Canvas) {
        doodleLayerManager.onDraw(canvas)
    }

    //region ---operate---

    /**刷新界面*/
    override fun refresh() {
        view.postInvalidateOnAnimation()
    }

    /**长按事件反馈提示*/
    fun longFeedback() {
        view.longFeedback()
    }

    //endregion ---operate---
}