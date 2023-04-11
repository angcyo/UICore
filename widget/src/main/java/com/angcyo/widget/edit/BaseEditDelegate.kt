package com.angcyo.widget.edit

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.text.TextPaint
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.widget.EditText
import com.angcyo.drawable.isGravityBottom
import com.angcyo.drawable.isGravityTop
import com.angcyo.library.ex.*
import com.angcyo.widget.R

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/08/03
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */

/**文本改变监听*/
typealias TextChangedAction = (editText: EditText?, text: CharSequence?, start: Int, lengthBefore: Int, lengthAfter: Int) -> Unit

abstract class BaseEditDelegate(val editText: EditText) {

    companion object {

        /**全局监听文本改变
         * [com.angcyo.core.Debug.onDebugTextChanged]*/
        val textChangedActionList = mutableListOf<TextChangedAction>()
    }

    /**绘制在输入框左边的文本*/
    var drawLeftText: String? = null
    var drawLeftColor: Int = undefined_color
    var drawLeftSize: Int = undefined_size

    var drawLeftOffsetLeft: String? = null
    var drawLeftOffsetRight: String? = null
    var drawLeftOffsetTop: String? = null
    var drawLeftOffsetBottom: String? = null

    open fun initAttribute(context: Context, attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.BaseEditDelegate)
        drawLeftText = typedArray.getString(R.styleable.BaseEditDelegate_r_draw_left)
        drawLeftColor =
            typedArray.getColor(R.styleable.BaseEditDelegate_r_draw_left_color, undefined_color)
        drawLeftSize = typedArray.getDimensionPixelOffset(
            R.styleable.BaseEditDelegate_r_draw_left_size,
            undefined_size
        )

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

    open fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        return false
    }

    open fun onTextChanged(text: CharSequence?, start: Int, lengthBefore: Int, lengthAfter: Int) {
        textChangedActionList.forEach {
            it.invoke(editText, text, start, lengthBefore, lengthAfter)
        }
    }

    var _drawLeftOffsetLeft = 0
    var _drawLeftOffsetTop = 0
    var _drawLeftOffsetBottom = 0
    var _drawLeftOffsetRight = 0

    var drawLeftPaint = TextPaint()

    val viewRect = Rect()
    open fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        viewRect.set(0, 0, w, h)
        updateDrawLeftText(drawLeftText)
    }

    /**更新左边绘制的文本*/
    fun updateDrawLeftText(text: String?) {
        drawLeftText = text

        drawLeftText?.let {
            val w = viewRect.width()
            val h = viewRect.height()

            val view = editText
            drawLeftPaint.set(view.paint)
            if (drawLeftSize != undefined_size) {
                drawLeftPaint.textSize = drawLeftSize.toFloat()
            }
            val textPaint: TextPaint = drawLeftPaint
            val textWidth = textPaint.textWidth(text)

            _drawLeftOffsetLeft = editText.calcSize(drawLeftOffsetLeft, w, h, 0, 0)
            _drawLeftOffsetRight = editText.calcSize(drawLeftOffsetRight, w, h, 0, 0)
            _drawLeftOffsetTop = editText.calcSize(drawLeftOffsetTop, w, h, 0, 0)
            _drawLeftOffsetBottom = editText.calcSize(drawLeftOffsetBottom, w, h, 0, 0)

            val needWidth = textWidth + _drawLeftOffsetLeft + _drawLeftOffsetRight
            if (editText.paddingLeft < needWidth) {
                editText.paddingLeft(needWidth.toInt())
            }

            editText.invalidate()
        }
    }

    /**绘制边界*/
    val viewDrawRect = Rect()
    open fun onDraw(canvas: Canvas) {
        canvas.getClipBounds(viewDrawRect)
        viewRect.set(0, 0, editText.mW(), editText.mH())

        val textPaint: TextPaint = drawLeftPaint

        drawLeftText?.let { text ->
            val textHeight = textPaint.textHeight()

            //color
            if (drawLeftColor == undefined_color) {
                textPaint.color = editText.currentHintTextColor
            } else {
                textPaint.color = drawLeftColor
            }

            val gravity = editText.gravity
            val y = when {
                gravity.isGravityTop() -> textHeight + _drawLeftOffsetTop
                gravity.isGravityBottom() -> viewRect.height() - _drawLeftOffsetBottom
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