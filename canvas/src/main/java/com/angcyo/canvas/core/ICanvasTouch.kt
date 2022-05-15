package com.angcyo.canvas.core

import android.view.MotionEvent
import com.angcyo.canvas.CanvasDelegate

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/05/06
 */
interface ICanvasTouch {

    /**是否要拦截处理当前的手势, 拦截后, 后续的手势都会被拦截处理*/
    fun onCanvasInterceptTouchEvent(canvasDelegate: CanvasDelegate, event: MotionEvent): Boolean {
        return false
    }

    /**手势处理*/
    fun onCanvasTouchEvent(canvasDelegate: CanvasDelegate, event: MotionEvent): Boolean {
        return false
    }

}