package com.angcyo.canvas

import android.view.MotionEvent
import com.angcyo.canvas.core.CanvasEntryPoint
import com.angcyo.canvas.core.ICanvasTouch

/**
 * [CanvasDelegate] 手势控制管理
 * [com.angcyo.canvas.CanvasDelegate.controlHandler]
 * [com.angcyo.canvas.CanvasDelegate.canvasTouchHandler]
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/05/06
 */
class CanvasTouchManager(val canvasView: CanvasDelegate) {

    /**手指是否按下*/
    var isTouchHold: Boolean = false

    /**拦截手势处理*/
    var interceptCanvasTouch: ICanvasTouch? = null

    /**入口*/
    @CanvasEntryPoint
    fun onTouchEvent(event: MotionEvent): Boolean {
        val touch = interceptCanvasTouch

        //手指是否按下
        isTouchHold = when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> true
            else -> false
        }

        //是否取消了手势
        when (event.actionMasked) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                interceptCanvasTouch = null

                //取消手势处理
                canvasView.canvasListenerList.forEach {
                    if (it != touch) {
                        it.onCanvasTouchEvent(canvasView, event)
                    }
                }

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
            if (interceptCanvasTouch == null && it.onCanvasInterceptTouchEvent(canvasView, event)) {
                interceptCanvasTouch = it

                _checkInterceptTouch(event)?.let {
                    return it
                }
            }
        }

        //controlHandler 拦截
        if (canvasView.controlHandler.enable) {
            if (canvasView.controlHandler.onCanvasInterceptTouchEvent(canvasView, event)) {
                interceptCanvasTouch = canvasView.controlHandler
            }
        }
        _checkInterceptTouch(event)?.let {
            return it
        }

        //canvasTouchHandler 拦截
        if (canvasView.canvasTouchHandler.enable) {
            if (canvasView.canvasTouchHandler.onCanvasInterceptTouchEvent(canvasView, event)) {
                interceptCanvasTouch = canvasView.canvasTouchHandler
            }
        }
        _checkInterceptTouch(event)?.let {
            return it
        }

        //手势处理
        canvasView.canvasListenerList.forEach {
            if (it.onCanvasTouchEvent(canvasView, event)) {
                return true
            }
        }
        //控制点,手势
        if (canvasView.controlHandler.enable) {
            if (canvasView.controlHandler.onCanvasTouchEvent(canvasView, event)) {
                return true
            }
        }
        //画布,手势
        if (canvasView.canvasTouchHandler.enable) {
            return canvasView.canvasTouchHandler.onCanvasTouchEvent(canvasView, event)
        }

        return true
    }

    /**检查是否需要拦截事件*/
    fun _checkInterceptTouch(event: MotionEvent): Boolean? {
        if (interceptCanvasTouch != null) {
            val result = interceptCanvasTouch!!.onCanvasTouchEvent(canvasView, event)
            _dispatchCancelTouchEvent(event)
            return result
        }
        return null
    }

    /**分发取消手势*/
    fun _dispatchCancelTouchEvent(event: MotionEvent) {
        val cancel = MotionEvent.obtain(event)
        cancel.action = MotionEvent.ACTION_CANCEL

        canvasView.canvasListenerList.forEach {
            if (it != interceptCanvasTouch) {
                it.onCanvasTouchEvent(canvasView, cancel)
            }
        }

        if (canvasView.controlHandler.enable) {
            if (canvasView.controlHandler != interceptCanvasTouch) {
                canvasView.controlHandler.onCanvasTouchEvent(canvasView, cancel)
            }
        }

        if (canvasView.canvasTouchHandler.enable) {
            if (canvasView.canvasTouchHandler != interceptCanvasTouch) {
                canvasView.canvasTouchHandler.onCanvasTouchEvent(canvasView, cancel)
            }
        }

        cancel.recycle()
    }

}