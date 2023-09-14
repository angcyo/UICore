package com.angcyo.widget.base

import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration

/**
 * 长按/短按事件监听
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/09/05
 */
class LongTouchListener(val block: (view: View, event: MotionEvent?, eventType: Int?, longPressHappened: Boolean) -> Boolean) :
    View.OnTouchListener, Runnable {

    companion object {
        /**[MotionEvent] 事件类型, 点击*/
        const val EVENT_TYPE_CLICK = 1

        /**[MotionEvent] 事件类型, 长按*/
        const val EVENT_TYPE_LONG_PRESS = 2
    }

    /**是否循环触发长按事件*/
    var loopLongPress: Boolean = false

    /**长按检测时长*/
    var longPressTimeout = ViewConfiguration.getLongPressTimeout().toLong()

    private var eventType: Int? = null

    private var longPressHappened = false
    private var view: View? = null

    override fun onTouch(view: View?, event: MotionEvent?): Boolean {
        this.view = view
        view ?: return false
        event ?: return false

        val actionMasked = event.actionMasked
        when (actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                eventType = null
                longPressHappened = false
                view.isPressed = true //按下的状态
                //长按检测
                view.postDelayed(this, longPressTimeout)
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                view.isPressed = false //恢复默认状态
                view.removeCallbacks(this)

                if (!longPressHappened && eventType == null) {
                    eventType = EVENT_TYPE_CLICK
                }
            }
        }
        if (eventType == EVENT_TYPE_CLICK) {
            //发送点击事件
            block(view, event, EVENT_TYPE_CLICK, longPressHappened)
        }
        //事件转发, 用于自定义处理
        block(view, event, null, longPressHappened)
        return true
    }

    override fun run() {
        val v = view

        if (v != null && v.isPressed) {
            if (eventType == null || eventType == EVENT_TYPE_LONG_PRESS) {

                eventType = EVENT_TYPE_LONG_PRESS
                longPressHappened = true

                //发送长按事件
                block(v, null, eventType, true)

                if (loopLongPress) {
                    v.postDelayed(this, longPressTimeout)
                }
            }
        }
    }
}