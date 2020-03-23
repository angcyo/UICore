package com.angcyo.widget.edit

import android.content.Context
import android.graphics.Rect
import android.text.InputFilter.LengthFilter
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.angcyo.widget.R
import com.angcyo.widget.base.addFilter

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/08
 */

open class DslEditText : ClearEditText {

    /**设置max length时, 是否允许溢出.*/
    var allowInputOverflow: Boolean = false

    /** 过滤的最大长度 */
    var maxEditLength = -1

    /** 使用英文字符数过滤, 一个汉字等于2个英文, 一个emoji表情等于2个汉字 */
    var useCharLengthFilter = false

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
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.DslEditText)

        allowInputOverflow = typedArray.getBoolean(
            R.styleable.DslEditText_r_allow_input_overflow,
            allowInputOverflow
        )

        useCharLengthFilter = typedArray.getBoolean(
            R.styleable.DslEditText_r_use_char_length_filter,
            useCharLengthFilter
        )

        //获取系统xml属性,设置的length
        if (!useCharLengthFilter) {
            val filters = filters
            for (i in filters.indices) {
                val filter = filters[i]
                if (filter is LengthFilter) {
                    maxEditLength = filter.max
                    break
                }
            }
        }

        val maxLength = typedArray.getInteger(
            R.styleable.DslEditText_r_max_edit_length,
            maxEditLength
        )
        if (maxLength >= 0) {
            setMaxLength(maxLength)
        }
        typedArray.recycle()
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

    /**设置最大输入长度*/
    fun setMaxLength(length: Int) {
        var lengthInner = length
        maxEditLength = lengthInner
        val filters = filters
        if (allowInputOverflow) {
            lengthInner = Int.MAX_VALUE
        }
        if (useCharLengthFilter) {
            var have = false
            val lengthFilter = CharLengthFilter(lengthInner)
            for (i in filters.indices) {
                val filter = filters[i]
                if (filter is CharLengthFilter) {
                    have = true
                    filters[i] = lengthFilter
                    setFilters(filters)
                    break
                }
            }
            if (!have) {
                addFilter(lengthFilter)
            }
        } else {
            var have = false
            val lengthFilter = LengthFilter(lengthInner)
            for (i in filters.indices) {
                val filter = filters[i]
                if (filter is LengthFilter) {
                    have = true
                    filters[i] = lengthFilter
                    setFilters(filters)
                    break
                }
            }
            if (!have) {
                addFilter(lengthFilter)
            }
        }
    }
}