package com.angcyo.widget.base

import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.angcyo.widget.base.DslTouch.Companion.METHOD_DISPATCH

/**
 * [TouchEvent] 事件模拟
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/21
 */

class DslTouch {
    companion object {
        const val METHOD_DISPATCH = 1
        const val METHOD_INTERCEPT = 2
        const val METHOD_TOUCH = 3
    }

    val handler = Handler(Looper.getMainLooper())

    val touchList = mutableListOf<TouchConfig>()

    fun add(
        touchAction: Int = MotionEvent.ACTION_DOWN,
        touchX: Float = 0f,
        touchY: Float = 0f,
        delay: Long = 16
    ) {
        add(TouchConfig(touchAction, touchX, touchY, delay))
    }

    fun add(config: TouchConfig) {
        touchList.add(config)
    }

    fun doIt(view: View) {
        val downTime = SystemClock.uptimeMillis()
        _do(view, 0, downTime)
    }

    private fun _do(view: View, index: Int, downTime: Long) {
        touchList.getOrNull(index)?.run {
            _doTouch(view, this, downTime) {
                _do(view, index + 1, downTime)
            }
        }
    }

    private fun _doTouch(view: View, config: TouchConfig, downTime: Long, end: () -> Unit) {
        val event = MotionEvent.obtain(
            downTime,
            SystemClock.uptimeMillis(),
            config.touchAction,
            config.touchX,
            config.touchY,
            0
        )

        when (config.touchMethod) {
            METHOD_DISPATCH -> view.dispatchTouchEvent(event)
            METHOD_INTERCEPT -> if (view is ViewGroup) {
                view.onInterceptTouchEvent(event)
            } else {
                view.onTouchEvent(event)
            }
            METHOD_TOUCH -> view.onTouchEvent(event)
        }

        event.recycle()

        handler.postDelayed({
            end()
        }, config.touchDelay)
    }
}

data class TouchConfig(
    var touchAction: Int = MotionEvent.ACTION_DOWN,
    var touchX: Float = 0f,
    var touchY: Float = 0f,
    var touchDelay: Long = 16,
    var touchMethod: Int = METHOD_DISPATCH
)

fun dslTouch(view: View, action: DslTouch.() -> Unit) {
    DslTouch().apply {
        action()
        doIt(view)
    }
}