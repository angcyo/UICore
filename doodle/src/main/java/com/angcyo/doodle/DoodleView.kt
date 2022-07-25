package com.angcyo.doodle

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.angcyo.library.ex.disableParentInterceptTouchEvent

/**
 * 涂鸦绘制板
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/07/25
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class DoodleView(context: Context, attributeSet: AttributeSet? = null) :
    View(context, attributeSet) {

    val doodleDelegate: DoodleDelegate = DoodleDelegate(this)

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        doodleDelegate.onSizeChanged(w, h, oldw, oldh)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            disableParentInterceptTouchEvent()
        } else if (event.actionMasked == MotionEvent.ACTION_UP || event.actionMasked == MotionEvent.ACTION_CANCEL) {
            disableParentInterceptTouchEvent(false)
        }
        return doodleDelegate.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        doodleDelegate.onDraw(canvas)
    }

}