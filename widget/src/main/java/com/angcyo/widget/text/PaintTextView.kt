package com.angcyo.widget.text

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.angcyo.widget.R

/**
 * 用来修改文本控件[TextPaint]属性的控件
 *
 * 支持文本描边, 原理就是绘制两次
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/11/22
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
open class PaintTextView : AppCompatTextView {

    /**描边的宽度*/
    var textStrokeWidth: Float = 0f

    /**描边的颜色*/
    var textStrokeColor: Int = Color.WHITE

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
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.PaintTextView)

        textStrokeWidth = typedArray.getDimensionPixelOffset(
            R.styleable.PaintTextView_r_text_stroke_width,
            paint.strokeWidth.toInt()
        ).toFloat()

        textStrokeColor =
            typedArray.getColor(R.styleable.PaintTextView_r_text_stroke_color, textStrokeColor)

        typedArray.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (textStrokeWidth > 0) {
            //宽度测量之前, 需要设置描边属性
            paint.strokeWidth = textStrokeWidth
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    var _ignoreInvalidate = false

    override fun invalidate() {
        if (!_ignoreInvalidate) {
            super.invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {

        if (textStrokeWidth > 0) {
            //先绘制描边

            val oldTextColors = textColors
            _ignoreInvalidate = true
            setTextColor(textStrokeColor)

            val oldStyle = paint.style

            paint.strokeWidth = textStrokeWidth
            paint.style = Paint.Style.STROKE
            super.onDraw(canvas)

            //清空描边属性
            paint.strokeWidth = 0f
            paint.style = oldStyle
            setTextColor(oldTextColors)
            _ignoreInvalidate = false
        }

        //正常绘制
        super.onDraw(canvas)
    }
}