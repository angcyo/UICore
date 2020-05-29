package com.angcyo.widget.text

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.Gravity
import com.angcyo.library.ex.have
import com.angcyo.widget.R
import com.angcyo.widget.base.exactly
import com.angcyo.widget.drawable.DslAttrBadgeDrawable
import java.util.*

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/08
 */
open class DslTextView : DslScrollTextView {

    /**角标绘制*/
    var dslBadeDrawable = DslAttrBadgeDrawable()

    /**支持设置format,[java.lang.String.format] [%1$s]*/
    var textFormat: String? = null
        set(value) {
            field = value
            text = _originText
        }

    /** 宽高是否相等, 取其中最大值计算 */
    var aeqWidth = false

    /**未处理过的原始[text]*/
    var _originText: CharSequence? = null

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
        dslBadeDrawable.initAttribute(context, attributeSet)
        dslBadeDrawable.callback = this

        val typedArray =
            context.obtainStyledAttributes(attributeSet, R.styleable.DslTextView)
        textFormat = typedArray.getString(R.styleable.DslTextView_r_text_format)
        aeqWidth = typedArray.getBoolean(R.styleable.DslTextView_r_is_aeq_width, aeqWidth)

        typedArray.recycle()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        dslBadeDrawable.apply {
            setBounds(0, 0, measuredWidth, measuredHeight)
            draw(canvas)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (aeqWidth) {
            val size = measuredWidth.coerceAtLeast(measuredHeight)
            if (gravity.have(Gravity.CENTER) ||
                gravity.have(Gravity.CENTER_HORIZONTAL) ||
                gravity.have(Gravity.CENTER_VERTICAL)
            ) {
                super.onMeasure(exactly(size), exactly(size))
            } else {
                setMeasuredDimension(size, size)
            }
        }
    }

    override fun setText(text: CharSequence?, type: BufferType?) {
        _originText = text
        super.setText(wrapText(text), type)
    }

    open fun wrapText(text: CharSequence?): CharSequence? {
        return _originText?.run {
            val format = textFormat
            if (format.isNullOrBlank()) {
                this
            } else {
                String.format(Locale.CHINA, format, text)
            }
        }
    }

    /**角标的文本, 空字符串会绘制成小圆点*/
    fun updateBadge(text: String? = null, action: DslAttrBadgeDrawable.() -> Unit = {}) {
        dslBadeDrawable.apply {
            drawBadge = true
            //badgeGravity = Gravity.TOP or Gravity.RIGHT
            badgeText = text
            //badgeCircleRadius
            //badgeOffsetY = 4 * dpi
            //cornerRadius(25 * dp)
            action()
        }
    }
}