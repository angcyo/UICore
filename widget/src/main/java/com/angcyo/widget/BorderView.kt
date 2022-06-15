package com.angcyo.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.angcyo.library.ex.paint

/**
 * 简单的Border边框控件
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/04/16
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class BorderView(context: Context, attributeSet: AttributeSet? = null) :
    View(context, attributeSet) {

    /**是否绘制边框*/
    var drawBorder: Boolean = true

    /**边框的颜色*/
    var borderColor: Int = Color.DKGRAY

    /**边框的宽度*/
    var borderWidth: Int = 1

    /**边框的圆角*/
    var borderRound: Int = 0

    val paint = paint().apply {
        style = Paint.Style.STROKE
    }

    init {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.BorderView)
        drawBorder = typedArray.getBoolean(R.styleable.BorderView_r_draw_border, drawBorder)
        borderWidth =
            typedArray.getDimensionPixelOffset(R.styleable.BorderView_r_border_width, borderWidth)
        borderRound =
            typedArray.getDimensionPixelOffset(R.styleable.BorderView_r_border_round, borderRound)
        borderColor = typedArray.getColor(R.styleable.BorderView_r_border_color, borderColor)
        typedArray.recycle()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        paint.strokeWidth = borderWidth.toFloat()
        paint.color = borderColor

        val width = measuredWidth.toFloat()
        val height = measuredHeight.toFloat()
        val strokeWidth: Float = paint.strokeWidth
        canvas.drawRoundRect(
            strokeWidth / 2,
            strokeWidth / 2,
            width - strokeWidth / 2,
            height - strokeWidth / 2,
            borderRound.toFloat(), borderRound.toFloat(),
            paint
        )
    }
}