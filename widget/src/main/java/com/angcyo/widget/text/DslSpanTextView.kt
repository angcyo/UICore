package com.angcyo.widget.text

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import com.angcyo.widget.R
import com.angcyo.widget.base.spans
import com.angcyo.widget.span.IDrawableSpan
import com.angcyo.widget.span.IWeightSpan

/**
 * 自定义Span支持类
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/08
 */
open class DslSpanTextView : PaintTextView {

    //drawable 额外的状态
    val _extraState = mutableListOf<Int>()

    var isInitExtraState: Boolean = false

    var maxLineDelegate = MaxLineDelegate()

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
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.DslSpanTextView)
        maxLineDelegate.apply {
            maxShowLine = typedArray.getInt(R.styleable.DslSpanTextView_r_max_line, maxShowLine)
            moreText = typedArray.getString(R.styleable.DslSpanTextView_r_more_text) ?: moreText
            moreTextColor =
                typedArray.getColor(R.styleable.DslSpanTextView_r_more_text_color, moreTextColor)
            foldTextColor =
                typedArray.getColor(R.styleable.DslSpanTextView_r_fold_text_color, foldTextColor)
            foldText = typedArray.getString(R.styleable.DslSpanTextView_r_fold_text) ?: foldText
            installSpanClickMethod = typedArray.getBoolean(
                R.styleable.DslSpanTextView_r_install_span_click_method,
                installSpanClickMethod
            )
            if (isEnableMaxShowLine) {
                setMaxShowLine(this@DslSpanTextView, maxShowLine)
            }
        }
        typedArray.recycle()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        maxLineDelegate.checkMaxShowLine(this)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
    }

    /**设置最大显示行数*/
    fun setMaxShowLine(line: Int) {
        maxLineDelegate.setMaxShowLine(this, line)
    }

    /**原始文本*/
    fun getOriginText(): CharSequence? =
        if (isEnableFoldLine()) maxLineDelegate._originText else text

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        return super.dispatchTouchEvent(event)
    }

    /**此方法, 会在系统[TextView]初始化时, 就触发. 此时此类的成员并未初始化, 所以NPE*/
    override fun setText(text: CharSequence?, type: BufferType?) {
        val bufferType = if (isEnableFoldLine()) {
            if (type == null || type == BufferType.NORMAL) {
                BufferType.SPANNABLE
            } else {
                type
            }
        } else {
            type
        }
        super.setText(text, bufferType)
    }

    /**是否激活了折叠行显示*/
    fun isEnableFoldLine() = maxLineDelegate != null && maxLineDelegate.isEnableMaxShowLine

    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        if (!isInitExtraState) {
            return super.onCreateDrawableState(extraSpace)
        }

        val state = super.onCreateDrawableState(extraSpace + _extraState.size)

        if (_extraState.isNotEmpty()) {
            View.mergeDrawableStates(state, _extraState.toIntArray())
        }

        return state
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        val state = onCreateDrawableState(0)

        //设置内置span的状态
        spans { _, span ->
            if (span is IDrawableSpan) {
                span.setDrawableState(state)
            }
        }
    }

    fun setDrawableColor(@ColorInt color: Int) {
        //设置内置span的颜色
        spans { _, span ->
            if (span is IDrawableSpan) {
                span.setDrawableColor(color)
            }
        }
        invalidate()
    }

    /**添加额外的状态*/
    fun addDrawableState(state: Int) {
        isInitExtraState = true
        _extraState.add(state)
        refreshDrawableState()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)

        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        _measureWeightSpan(widthSize, heightSize)

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    fun _measureWeightSpan(widthSize: Int, heightSize: Int) {
        //设置内置span的weight支持
        spans { _, span ->
            if (span is IWeightSpan) {
                val width = widthSize - paddingLeft - paddingRight
                val height = heightSize - paddingTop - paddingBottom
                span.onMeasure(width, height)
            }
        }
    }

}