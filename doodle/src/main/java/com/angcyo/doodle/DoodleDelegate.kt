package com.angcyo.doodle

import android.graphics.Canvas
import android.graphics.Color
import android.view.MotionEvent
import android.view.View
import com.angcyo.doodle.core.IDoodleView

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/07/25
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class DoodleDelegate(val view: View) : IDoodleView {

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return true
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(Color.MAGENTA)
    }
}