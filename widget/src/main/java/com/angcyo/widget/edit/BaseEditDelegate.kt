package com.angcyo.widget.edit

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.text.TextPaint
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.EditText
import com.angcyo.drawable.isBottom
import com.angcyo.drawable.isTop
import com.angcyo.drawable.textHeight
import com.angcyo.drawable.textWidth
import com.angcyo.library.ex.calcSize
import com.angcyo.widget.R
import com.angcyo.widget.base.mH
import com.angcyo.widget.base.mW
import com.angcyo.widget.base.paddingLeft

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/08/03
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
abstract class BaseEditDelegate(val editText: EditText) {

    /**绘制在输入框左边的文本*/
    var drawLeftText: String? = null
    var drawLeftOffsetLeft: String? = null
    var drawLeftOffsetRight: String? = null
    var drawLeftOffsetTop: String? = null
    var drawLeftOffsetBottom: String? = null

    open fun initAttribute(context: Context, attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.BaseEditDelegate)
        drawLeftText = typedArray.getString(R.styleable.BaseEditDelegate_r_draw_left)

        drawLeftOffsetLeft =
            typedArray.getString(R.styleable.BaseEditDelegate_r_draw_left_offset_left)
        drawLeftOffsetRight =
            typedArray.getString(R.styleable.BaseEditDelegate_r_draw_left_offset_right)
        drawLeftOffsetTop =
            typedArray.getString(R.styleable.BaseEditDelegate_r_draw_left_offset_top)
        drawLeftOffsetBottom =
            typedArray.getString(R.styleable.BaseEditDelegate_r_draw_left_offset_bottom)

        typedArray.recycle()
    }

    open fun onFocusChanged(focused: Boolean) {

    }

    open fun onTextChanged(text: CharSequence?, start: Int, lengthBefore: Int, lengthAfter: Int) {

    }

    var _drawLeftOffsetLeft = 0
    var _drawLeftOffsetTop = 0
    var _drawLeftOffsetBottom = 0
    var _drawLeftOffsetRight = 0

    val viewRect = Rect()
    open fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        viewRect.set(0, 0, w, h)

        drawLeftText?.let { text ->
            val view = editText
            val textPaint: TextPaint = view.paint
            val textWidth = textPaint.textWidth(text)

            _drawLeftOffsetLeft = editText.calcSize(drawLeftOffsetLeft, w, h, 0, 0)
            _drawLeftOffsetRight = editText.calcSize(drawLeftOffsetRight, w, h, 0, 0)
            _drawLeftOffsetTop = editText.calcSize(drawLeftOffsetTop, w, h, 0, 0)
            _drawLeftOffsetBottom = editText.calcSize(drawLeftOffsetBottom, w, h, 0, 0)

            val needWidth = textWidth + _drawLeftOffsetLeft + _drawLeftOffsetRight
            if (editText.paddingLeft < needWidth) {
                editText.paddingLeft(needWidth.toInt())
            }
        }
    }

    /**绘制边界*/
    val viewDrawRect = Rect()
    open fun onDraw(canvas: Canvas) {
        canvas.getClipBounds(viewDrawRect)
        viewRect.set(0, 0, editText.mW(), editText.mH())

        val view = editText
        val textPaint: TextPaint = view.paint

        drawLeftText?.let { text ->
            val textHeight = textPaint.textHeight()

            //color
            textPaint.color = editText.currentHintTextColor

            val gravity = editText.gravity
            val y = when {
                gravity.isTop() -> textHeight + _drawLeftOffsetTop
                gravity.isBottom() -> viewRect.height() - _drawLeftOffsetBottom
                else -> viewRect.height() / 2 + textHeight / 2 + _drawLeftOffsetBottom - _drawLeftOffsetBottom
            }

            canvas.drawText(
                text,
                _drawLeftOffsetLeft + viewDrawRect.left.toFloat(),
                y.toFloat() - textPaint.descent(),
                textPaint
            )
        }
    }

    open fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {

    }

    open fun drawableStateChanged() {

    }

    open fun onTouchEvent(event: MotionEvent): Boolean {
        return false
    }
}