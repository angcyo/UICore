package com.angcyo.widget.image

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatImageView

/**
 * 单独回调[onTouchEvent]事件
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/06/01
 */
open class TouchCompatImageView(context: Context, attributeSet: AttributeSet? = null) :
    AppCompatImageView(context, attributeSet) {

    /**手势回调, 不影响click事件的处理*/
    var touchAction: (View, MotionEvent) -> Unit = { _, _ -> }

    init {
        isClickable = true
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        return super.dispatchTouchEvent(event)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)
        touchAction(this, event)
        return true
    }
}