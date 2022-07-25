package com.angcyo.doodle.core

import android.graphics.Canvas
import android.view.MotionEvent

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/07/25
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
interface IDoodleView {

    //<editor-fold desc="core">

    fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int)

    fun onTouchEvent(event: MotionEvent): Boolean

    fun onDraw(canvas: Canvas)

    //</editor-fold desc="core">

    //<editor-fold desc="operate">

    /**刷新界面*/
    fun refresh()

    //</editor-fold desc="operate">

}