package com.angcyo.widget.edit

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.EditText

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/08/03
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
abstract class BaseEditDelegate(val editText: EditText) {
    open fun initAttribute(context: Context, attrs: AttributeSet?) {

    }

    open fun onFocusChanged(focused: Boolean) {

    }

    open fun onTextChanged(
        text: CharSequence?,
        start: Int,
        lengthBefore: Int,
        lengthAfter: Int
    ) {

    }

    open fun onDraw(canvas: Canvas) {

    }

    open fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {

    }

    open fun drawableStateChanged() {

    }

    open fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {

    }

    open fun onTouchEvent(event: MotionEvent): Boolean {
        return false
    }
}