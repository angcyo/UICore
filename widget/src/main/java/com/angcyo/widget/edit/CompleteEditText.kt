package com.angcyo.widget.edit

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.angcyo.widget.R

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/08
 */

open class CompleteEditText : ClearCompleteEditText {

    /**
     * 是否是不可编辑模式
     */
    var isNoEditMode = false

    /**
     * 是否只有在 touch 事件的时候, 才可以请求焦点. 防止在列表中,自动获取焦点的情况
     */
    var requestFocusOnTouch = false

    /**
     * 当失去焦点时, 是否隐藏键盘
     */
    var hideSoftInputOnLostFocus = false
    /**
     * 当onDetachedFromWindow时, 是否隐藏键盘
     */
    var hideSoftInputOnDetached = false

    /**
     * 当视图不可见时, 是否隐藏键盘
     */
    var hideSoftInputOnInvisible = false

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
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CompleteEditText)

        isNoEditMode =
            typedArray.getBoolean(R.styleable.CompleteEditText_r_is_no_edit_mode, isNoEditMode)
        requestFocusOnTouch = typedArray.getBoolean(
            R.styleable.CompleteEditText_r_request_focus_on_touch,
            requestFocusOnTouch
        )
        hideSoftInputOnLostFocus = typedArray.getBoolean(
            R.styleable.CompleteEditText_r_hide_soft_input_on_lost_focus,
            hideSoftInputOnLostFocus
        )
        hideSoftInputOnDetached = typedArray.getBoolean(
            R.styleable.CompleteEditText_r_hide_soft_input_on_detached,
            hideSoftInputOnDetached
        )
        hideSoftInputOnInvisible = typedArray.getBoolean(
            R.styleable.CompleteEditText_r_hide_soft_input_on_invisible,
            hideSoftInputOnInvisible
        )

        typedArray.recycle()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isNoEditMode || !isEnabled) {
            return false
        }
        return super.onTouchEvent(event)
    }

    override fun requestFocus(direction: Int, previouslyFocusedRect: Rect?): Boolean {
        if (isNoEditMode) {
            return false
        }
        if (requestFocusOnTouch) {
            if (System.currentTimeMillis() - _downTime > 160) {
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

            if (hideSoftInputOnLostFocus) {
                HideSoftInputRunnable.doIt(this)
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (hideSoftInputOnDetached) {
            HideSoftInputRunnable.doIt(this)
        }
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility != View.VISIBLE && !isInEditMode && hideSoftInputOnInvisible) {
            HideSoftInputRunnable.doIt(this)
        }
    }
}