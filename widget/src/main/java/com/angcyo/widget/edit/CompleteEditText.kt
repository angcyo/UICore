package com.angcyo.widget.edit

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/08
 */

open class CompleteEditText : ClearCompleteEditText {

    constructor(context: Context) : super(context) {
        initAttribute(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initAttribute(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initAttribute(context, attrs)
    }

    private fun initAttribute(context: Context, attrs: AttributeSet?) {

    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (editDelegate?.isNoEditMode == true || !isEnabled) {
            return false
        }
        return super.onTouchEvent(event)
    }

    override fun requestFocus(direction: Int, previouslyFocusedRect: Rect?): Boolean {
        if (editDelegate?.isNoEditMode == true) {
            return false
        }
        if (editDelegate?.requestFocusOnTouch == true) {
            if (System.currentTimeMillis() - (editDelegate?._downTime ?: 0) > 160) {
                return false
            }
        }
        return super.requestFocus(direction, previouslyFocusedRect)
    }

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)

        if (focused) {
            HideSoftInputRunnable.cancel()
        } else {

            if (editDelegate?.hideSoftInputOnLostFocus == true) {
                HideSoftInputRunnable.doIt(this)
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (editDelegate?.hideSoftInputOnDetached == true) {
            HideSoftInputRunnable.doIt(this)
        }
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility != View.VISIBLE && !isInEditMode && editDelegate?.hideSoftInputOnInvisible == true) {
            HideSoftInputRunnable.doIt(this)
        }
    }
}