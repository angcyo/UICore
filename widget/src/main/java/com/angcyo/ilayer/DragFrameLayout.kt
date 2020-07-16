package com.angcyo.ilayer

import android.content.Context
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.FrameLayout
import androidx.annotation.CallSuper
import com.angcyo.library.ex.abs
import com.angcyo.widget.base.isTouchFinish

/**
 * 拖动事件回调布局
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/07/15
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class DragFrameLayout(context: Context, attributeSet: AttributeSet? = null) :
    FrameLayout(context, attributeSet) {

    /**是否只在长按下激活拖拽*/
    var enableLongPressDrag = false

    var dragAction: ((distanceX: Float, distanceY: Float, end: Boolean) -> Unit)? = null

    var touchSlop = 0

    init {
        val configuration = ViewConfiguration.get(context)
        touchSlop = configuration.scaledTouchSlop
    }

    /**是否长按*/
    var _isLongPress = false

    var longPressRunnable = Runnable {
        _isLongPress = true
        if (!performLongClick()) {
            //手动触发长按反馈
            performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    }

    @CallSuper
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        handleTouchEvent(ev)
        if (ev.isTouchFinish()) {
            //通知0,0用于表示结束拖拽
            dragAction?.invoke(0f, 0f, true)
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return super.onInterceptTouchEvent(ev) || handleTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)
        //gestureDetector.onTouchEvent(event)
        handleTouchEvent(event)
        return true
    }

    fun removeLongPressRunnable() {
        removeCallbacks(longPressRunnable)
    }

    var _downX = 0f
    var _downY = 0f

    var _lastX = 0f
    var _lastY = 0f

    fun handleTouchEvent(event: MotionEvent): Boolean {
        var result = false

        val eventX = event.rawX
        val eventY = event.rawY

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                _downX = eventX
                _downY = eventY

                _lastX = _downX
                _lastY = _downY

                removeLongPressRunnable()
                if (enableLongPressDrag) {
                    postDelayed(longPressRunnable, ViewConfiguration.getLongPressTimeout().toLong())
                }
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = _lastX - eventX
                val dy = _lastY - eventY

                _lastX = eventX
                _lastY = eventY

                if ((_downX - eventX).abs() > touchSlop || (_downY - eventY).abs() > touchSlop) {
                    result = true
                    parent.requestDisallowInterceptTouchEvent(true)
                    removeLongPressRunnable()
                }

                if (!enableLongPressDrag || (enableLongPressDrag && _isLongPress)) {
                    dragAction?.invoke(dx, dy, false)
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                removeLongPressRunnable()
                _isLongPress = false
                parent.requestDisallowInterceptTouchEvent(false)
            }
        }

        return result
    }
}