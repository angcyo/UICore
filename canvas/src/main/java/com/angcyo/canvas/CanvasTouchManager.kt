package com.angcyo.canvas

import android.view.MotionEvent
import com.angcyo.canvas.core.ICanvasTouch

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/05/06
 */
class CanvasTouchManager(val canvasView: CanvasView) {

    /**拦截手势处理*/
    var interceptCanvasTouch: ICanvasTouch? = null

    /**入口*/
    fun onTouchEvent(event: MotionEvent): Boolean {
        val touch = interceptCanvasTouch

        //
        canvasView.isTouchHold = when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> true
            else -> false
        }

        //
        when (event.actionMasked) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                interceptCanvasTouch = null
                if (touch != null) {
                    return touch.onCanvasTouchEvent(canvasView, event)
                }
            }
        }

        //被拦截
        if (touch != null) {
            return touch.onCanvasTouchEvent(canvasView, event)
        }

        //判断拦截
        canvasView.canvasListenerList.forEach {
            if (it.onCanvasInterceptTouchEvent(canvasView, event)) {
                interceptCanvasTouch = it
            }
        }
        if (interceptCanvasTouch != null) {
            return interceptCanvasTouch!!.onCanvasTouchEvent(canvasView, event)
        }

        //
        if (canvasView.controlHandler.enable) {
            if (canvasView.controlHandler.onCanvasInterceptTouchEvent(canvasView, event)) {
                interceptCanvasTouch = canvasView.controlHandler
            }
        }
        if (interceptCanvasTouch != null) {
            return interceptCanvasTouch!!.onCanvasTouchEvent(canvasView, event)
        }

        //
        if (canvasView.canvasTouchHandler.enable) {
            if (canvasView.canvasTouchHandler.onCanvasInterceptTouchEvent(canvasView, event)) {
                interceptCanvasTouch = canvasView.canvasTouchHandler
            }
        }
        if (interceptCanvasTouch != null) {
            return interceptCanvasTouch!!.onCanvasTouchEvent(canvasView, event)
        }

        //手势处理
        canvasView.canvasListenerList.forEach {
            if (it.onCanvasTouchEvent(canvasView, event)) {
                return true
            }
        }
        if (canvasView.controlHandler.enable) {
            if (canvasView.controlHandler.onCanvasTouchEvent(canvasView, event)) {
                return true
            }
        }
        if (canvasView.canvasTouchHandler.enable) {
            return canvasView.canvasTouchHandler.onCanvasTouchEvent(canvasView, event)
        }

        return true
    }

}