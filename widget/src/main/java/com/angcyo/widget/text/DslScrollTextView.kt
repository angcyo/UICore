package com.angcyo.widget.text

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import androidx.core.view.ViewCompat
import com.angcyo.library.ex.dpi
import com.angcyo.library.ex.isVisible
import com.angcyo.widget.R
import com.angcyo.widget.base.atMost
import kotlin.math.absoluteValue
import kotlin.math.max

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/08
 */
open class DslScrollTextView : DslSpanTextView {

    /**滚动文本*/
    var scrollText: Boolean = false
        set(value) {
            field = value
            if (value) {
                isSingleLine = true
                ellipsize = null
            } else {
                postInvalidate()
            }
        }

    /**开始滚动*/
    var scrollStart: Boolean = false
        set(value) {
            field = value
            if (value) {
                _scrollTextX = scrollFirstOffset.offset()
            } else {
                //恢复默认位置
                _scrollTextX = 0f
                _scrollNextTextX = 0f
            }
            postInvalidate()
        }

    /**滚动循环开始时偏移的大小, 负数表示[View]宽度的倍数*/
    var scrollOffset: Float = -0.5f
    var scrollFirstOffset: Float = 0f

    /**每次滚动的偏移*/
    var scrollTextStep = 1 * dpi

    /**滚动循环*/
    var onScrollLoop: () -> Unit = {

    }

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

    private fun initAttribute(context: Context, attributeSet: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(
            attributeSet,
            R.styleable.DslScrollTextView
        )
        scrollText = typedArray.getBoolean(R.styleable.DslScrollTextView_r_text_scroll, scrollText)

        //偏移
        scrollTextStep = typedArray.getDimensionPixelOffset(
            R.styleable.DslScrollTextView_r_text_scroll_step,
            scrollTextStep
        )
        val offset =
            typedArray.getDimensionPixelOffset(
                R.styleable.DslScrollTextView_r_text_scroll_offset,
                0
            )
        scrollOffset = if (offset <= 0) {
            typedArray.getFloat(R.styleable.DslScrollTextView_r_text_scroll_offset, scrollOffset)
        } else {
            offset.toFloat()
        }

        //首次偏移
        val firstOffset =
            typedArray.getDimensionPixelOffset(
                R.styleable.DslScrollTextView_r_text_scroll_first_offset,
                0
            )
        scrollFirstOffset = if (firstOffset <= 0) {
            typedArray.getFloat(
                R.styleable.DslScrollTextView_r_text_scroll_first_offset,
                scrollFirstOffset
            )
        } else {
            firstOffset.toFloat()
        }

        scrollStart =
            typedArray.getBoolean(R.styleable.DslScrollTextView_r_text_scroll_start, scrollStart)

        typedArray.recycle()
    }

    var _scrollTextWidth = 0
    val _scrollWidth get() = max(_scrollTextWidth, measuredWidth)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        //测量滚动文本的宽度
        if (scrollText) {
            super.onMeasure(atMost(Int.MAX_VALUE), heightMeasureSpec)
            _scrollTextWidth = measuredWidth
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (scrollText) {
            setRight(getLeft() + _scrollWidth)
        }
    }

    //文本滚动的距离
    var _scrollTextX = 0f
    var _scrollNextTextX = 0f

    override fun draw(canvas: Canvas) {
        if (scrollText) {
            canvas.save()
            canvas.clipRect(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat())
            super.draw(canvas)
            canvas.restore()
        } else {
            super.draw(canvas)
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (scrollText) {
            canvas.save()
            canvas.translate(_scrollTextX, 0f)
            super.onDraw(canvas)
            canvas.restore()

            _scrollNextTextX = _scrollTextX + _scrollTextWidth + scrollOffset.offset()

            canvas.save()
            canvas.translate(_scrollNextTextX, 0f)
            super.onDraw(canvas)
            canvas.restore()

            if (ViewCompat.isAttachedToWindow(this) && isVisible() && scrollStart) {
                if (_scrollTextX < -_scrollTextWidth) {
                    _scrollTextX = _scrollNextTextX
                    onScrollLoop()
                }

                _scrollTextX -= scrollTextStep

                postInvalidateOnAnimation()
            }
        } else {
            super.onDraw(canvas)
        }
    }

    //<editor-fold desc="方法区">

    fun Float.offset(): Float {
        return if (this >= 0) this else this.absoluteValue * measuredWidth
    }

    //</editor-fold desc="方法区">
}