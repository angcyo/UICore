package com.angcyo.canvas.render.core.component

import android.graphics.RectF
import android.view.MotionEvent
import android.view.ViewConfiguration
import com.angcyo.canvas.render.core.CanvasRenderDelegate
import com.angcyo.library.annotation.CallPoint
import com.angcyo.library.component.MainExecutor
import com.angcyo.library.ex.emptyRectF
import com.angcyo.library.ex.nowTime

/**
 * 范围点位事件处理,
 * 比如视图左上角处, 点击/长按事件处理
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/03/25
 */
class PointTouchComponent(val delegate: CanvasRenderDelegate) : BaseTouchComponent() {

    companion object {
        /**事件类型, 点击*/
        const val TOUCH_TYPE_CLICK = 1

        /**事件类型, 长按*/
        const val TOUCH_TYPE_LONG_PRESS = 2

        const val TAG_INITIAL = "initial"
    }

    /**标识属性*/
    var pointTag: String? = null

    /**关键: 点位的范围
     * 左上角初始点位坐标*/
    val pointRect: RectF = emptyRectF()

    /**是否在目标区域按下*/
    private var isTouchDownInInitial = false

    /**按下的时间, 用来计算是否长按了*/
    private var _touchDownTime: Long = -1

    /**长按检测时间*/
    private val longPressTimeout = ViewConfiguration.getLongPressTimeout().toLong()//400

    /**长按事件*/
    private var _longRunnable: Runnable? = null

    /**[event]事件坐标应该是相当于[View]的*/
    @CallPoint
    override fun dispatchTouchEvent(event: MotionEvent) {
        super.dispatchTouchEvent(event)
    }

    override fun handleTouchEvent(event: MotionEvent) {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                isTouchDownInInitial = pointRect.contains(event.x, event.y)
                _touchDownTime = if (isTouchDownInInitial) {
                    _longRunnable = Runnable {
                        if (isTouchDownInInitial) {
                            //长按
                            isTouchDownInInitial = false
                            onLongPress()
                        }
                    }
                    MainExecutor.delay(_longRunnable!!, longPressTimeout)
                    nowTime()
                } else {
                    -1
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (isTouchDownInInitial) {
                    isTouchDownInInitial = pointRect.contains(event.x, event.y)
                }
            }
            MotionEvent.ACTION_UP -> {
                _longRunnable?.let {
                    MainExecutor.remove(it)
                    _longRunnable = null
                }
                if (isTouchDownInInitial) {
                    if (pointRect.contains(event.x, event.y)) {
                        onClick()
                    }
                }
            }
        }
    }

    /**点击事件*/
    private fun onClick(): Boolean {
        delegate.dispatchPointTouchEvent(this, TOUCH_TYPE_CLICK)
        return true
    }

    /**长按事件*/
    private fun onLongPress(): Boolean {
        delegate.dispatchPointTouchEvent(this, TOUCH_TYPE_LONG_PRESS)
        return true
    }

}