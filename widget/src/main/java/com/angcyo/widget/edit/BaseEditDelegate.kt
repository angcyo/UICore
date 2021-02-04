package com.angcyo.widget.edit

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.text.TextPaint
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.EditText
import com.angcyo.drawable.*
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

    open fun initAttribute(context: Context, attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.BaseEditDelegate)
        drawLeftText = typedArray.getString(R.styleable.BaseEditDelegate_r_draw_left)
        typedArray.recycle()
    }

    open fun onFocusChanged(focused: Boolean) {

    }

    open fun onTextChanged(text: CharSequence?, start: Int, lengthBefore: Int, lengthAfter: Int) {

    }

    /**绘制边界*/
    val viewDrawRect = Rect()
    open fun onDraw(canvas: Canvas) {
        canvas.getClipBounds(viewDrawRect)
        viewRect.set(0, 0, editText.mW(), editText.mH())

        val view = editText
        val textPaint: TextPaint = view.paint

        drawLeftText?.let { text ->
            val textWidth = textPaint.textWidth(text)
            val textHeight = textPaint.textHeight()

            if (editText.paddingLeft < textWidth) {
                editText.paddingLeft(textWidth.toInt())
                return@let
            }

            //color
            textPaint.color = editText.currentHintTextColor

            val gravity = editText.gravity
            val y = when {
                gravity.isTop() -> textHeight
                gravity.isBottom() -> viewRect.height()
                else -> viewRect.height() / 2 + textHeight / 2
            }

            canvas.drawText(
                text,
                viewDrawRect.left.toFloat(),
                y.toFloat() - textPaint.descent(),
                textPaint
            )
        }
    }

    open fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {

    }

    open fun drawableStateChanged() {

    }

    val viewRect = Rect()
    open fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        viewRect.set(0, 0, w, h)
    }

    open fun onTouchEvent(event: MotionEvent): Boolean {
        return false
    }
}