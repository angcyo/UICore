package com.angcyo.widget.base

import android.graphics.drawable.Drawable
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.view.GestureDetectorCompat

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/20
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

fun View.getColor(@ColorRes id: Int): Int {
    return ContextCompat.getColor(context, id)
}

fun View.getDrawable(id: Int): Drawable? {
    if (id <= 0) {
        return null
    }
    return ContextCompat.getDrawable(context, id)
}

fun View.getStatusBarHeight(): Int {
    return context.getStatusBarHeight()
}

/**双击事件*/
fun View.onDoubleTap(action: (View) -> Boolean) {
    val view = this
    val gestureDetector = GestureDetectorCompat(context,
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent?): Boolean {
                return action(view)
            }
        })
    setOnTouchListener { _, event ->
        gestureDetector.onTouchEvent(event)
    }
}