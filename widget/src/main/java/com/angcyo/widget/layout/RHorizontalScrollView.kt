package com.angcyo.widget.layout

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.HorizontalScrollView


/**
 *支持点击事件的 HorizontalScrollView
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2017/10/25 13:51
 */
class RHorizontalScrollView(context: Context, attributeSet: AttributeSet? = null) :
    HorizontalScrollView(context, attributeSet) {
    private val gestureDetector =
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                performClick()
                return super.onSingleTapUp(e)
            }

            override fun onDown(e: MotionEvent): Boolean {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    drawableHotspotChanged(e.x, e.y)
                }
                isPressed = true
                return super.onDown(e)
            }
        })

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(ev)
        when (ev.actionMasked) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> isPressed = false
        }
        return super.onTouchEvent(ev)
    }
}