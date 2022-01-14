package com.angcyo.ilayer

import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import com.angcyo.ilayer.container.BaseContainer
import com.angcyo.ilayer.container.IContainer
import com.angcyo.library.L
import com.angcyo.library._screenHeight
import com.angcyo.library._screenWidth
import com.angcyo.library.ex.abs
import com.angcyo.library.ex.simpleHash

/**
 * 容器元素自身拖拽事件监听
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/01/14
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class ContainerDragTouchListener(
    val view: View,
    val container: IContainer,
    val layer: ILayer
) : View.OnTouchListener {

    /**是否只在长按下激活拖拽*/
    var enableLongPressDrag = false

    /**检查是否滚动的阈值*/
    var touchSlop = 0

    /**是否长按*/
    var _isLongPress = false

    var _downX = 0f
    var _downY = 0f

    var _lastX = 0f
    var _lastY = 0f

    var handle = false

    /**长按检测*/
    var longPressRunnable = Runnable {
        _isLongPress = true
        if (!view.performLongClick()) {
            //手动触发长按反馈
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    }

    /**拖拽回调, 本身不处理布局, 只负责回调*/
    var dragAction: ((distanceX: Float, distanceY: Float, end: Boolean) -> Unit)? =
        { distanceX, distanceY, end ->
            if (container is BaseContainer) {
                container.onDragBy(layer, distanceX, distanceY, end)
            }
        }

    init {
        val configuration = ViewConfiguration.get(view.context)
        touchSlop = configuration.scaledTouchSlop
    }

    fun removeLongPressRunnable() {
        view.removeCallbacks(longPressRunnable)
    }

    override fun onTouch(view: View?, event: MotionEvent): Boolean {
        L.w("...")
        return handleTouchEvent(event)
    }

    fun handleTouchEvent(event: MotionEvent): Boolean {

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
                    view.postDelayed(
                        longPressRunnable,
                        ViewConfiguration.getLongPressTimeout().toLong()
                    )
                }
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = _lastX - eventX
                val dy = _lastY - eventY

                _lastX = eventX
                _lastY = eventY

                if ((_downX - eventX).abs() > touchSlop || (_downY - eventY).abs() > touchSlop) {
                    handle = true
                    view.parent?.requestDisallowInterceptTouchEvent(true)
                    removeLongPressRunnable()
                }

                if (handle) {
                    if (!enableLongPressDrag || (enableLongPressDrag && _isLongPress)) {
                        dragAction?.invoke(dx, dy, false)
                    }
                }

                L.v("${simpleHash()} ${view.parent?.simpleHash()} [$eventX,$eventY](${eventX * 1f / _screenWidth},${eventY * 1f / _screenHeight}) [${event.x},${event.y}]")
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                removeLongPressRunnable()
                _isLongPress = false
                view.parent?.requestDisallowInterceptTouchEvent(false)
            }
        }

        return handle
    }
}