package com.angcyo.ilayer

import android.content.Context
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.FrameLayout
import androidx.annotation.CallSuper
import com.angcyo.library.L
import com.angcyo.library._screenHeight
import com.angcyo.library._screenWidth
import com.angcyo.library.ex.abs
import com.angcyo.library.ex.longFeedback
import com.angcyo.library.ex.simpleHash
import com.angcyo.library.ex.isTouchFinish

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

    /**拖拽回调, 本身不处理布局, 只负责回调*/
    var dragAction: ((distanceX: Float, distanceY: Float, end: Boolean) -> Unit)? = null

    /**检查是否滚动的阈值*/
    var touchSlop = 0

    init {
        val configuration = ViewConfiguration.get(context)
        touchSlop = configuration.scaledTouchSlop
    }

    /**是否长按*/
    var _isLongPress = false

    /**长按检测*/
    var longPressRunnable = Runnable {
        _isLongPress = true
        if (!performLongClick()) {
            //手动触发长按反馈
            longFeedback()
        }
    }

    /**不希望控件拦截Touch事件*/
    var _disallowIntercept = false

    override fun requestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        super.requestDisallowInterceptTouchEvent(disallowIntercept)
        _disallowIntercept = disallowIntercept
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
        if (!handleTouchEvent(event)) {
            super.onTouchEvent(event)
        }
        return true
    }

    fun removeLongPressRunnable() {
        removeCallbacks(longPressRunnable)
    }

    var _downX = 0f
    var _downY = 0f

    var _lastX = 0f
    var _lastY = 0f

    var handle = false

    fun handleTouchEvent(event: MotionEvent): Boolean {

        if (_disallowIntercept) {
            return false
        }

        val eventX = event.rawX
        val eventY = event.rawY

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                handle = false
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
                    handle = true
                    parent?.requestDisallowInterceptTouchEvent(true)
                    removeLongPressRunnable()
                }

                if (handle) {
                    if (!enableLongPressDrag || (enableLongPressDrag && _isLongPress)) {
                        dragAction?.invoke(dx, dy, false)
                    }
                }

                L.v("${simpleHash()} ${parent?.simpleHash()} [$eventX,$eventY](${eventX * 1f / _screenWidth},${eventY * 1f / _screenHeight}) [${event.x},${event.y}]")
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                removeLongPressRunnable()
                _isLongPress = false
                parent?.requestDisallowInterceptTouchEvent(false)
            }
        }

        return handle
    }
}