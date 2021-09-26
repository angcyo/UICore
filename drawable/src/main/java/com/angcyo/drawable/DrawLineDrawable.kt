package com.angcyo.drawable

import android.content.Context
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import com.angcyo.drawable.base.AbsDslDrawable
import com.angcyo.library.ex.density
import com.angcyo.library.ex.dp
import com.angcyo.library.ex.loadColor

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/05/29
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
open class DrawLineDrawable : AbsDslDrawable() {
    companion object {
        const val DRAW_NONE = 0
        const val DRAW_LINE_LEFT = 1
        const val DRAW_LINE_TOP = 2
        const val DRAW_LINE_RIGHT = 3
        const val DRAW_LINE_BOTTOM = 4
        const val DRAW_LINE_BOTTOM_TOP = 5
    }

    /**绘制线的类型*/
    var drawLineType = DRAW_NONE //不绘制线

    var drawLineColor = 0

    var drawLineOffsetLeft = 0 //左偏移
    var drawLineOffsetRight = 0 //右偏移
    var drawLineOffsetTop = 0 //上偏移
    var drawLineOffsetBottom = 0 //下偏移

    var drawLineWidth: Float = 1 * dp

    /** 是否是虚线, 蚂蚁线 */
    var isDashLine = false

    var drawLineFront = true

    /** 横竖整体偏移 */
    var drawLineOffsetX = 0

    /** 横竖整体偏移 */
    var drawLineOffsetY = 0

    var lineDrawable: Drawable? = null

    override fun initAttribute(context: Context, attributeSet: AttributeSet?) {
        super.initAttribute(context, attributeSet)
        val typedArray =
            context.obtainStyledAttributes(attributeSet, R.styleable.DrawLineDrawable)

        drawLineType = typedArray.getInt(R.styleable.DrawLineDrawable_r_draw_line, drawLineType)
        drawLineOffsetLeft = typedArray.getDimensionPixelOffset(
            R.styleable.DrawLineDrawable_r_draw_line_offset_left,
            drawLineOffsetLeft
        )
        drawLineOffsetRight = typedArray.getDimensionPixelOffset(
            R.styleable.DrawLineDrawable_r_draw_line_offset_right,
            drawLineOffsetRight
        )
        drawLineOffsetTop = typedArray.getDimensionPixelOffset(
            R.styleable.DrawLineDrawable_r_draw_line_offset_top,
            drawLineOffsetTop
        )
        drawLineOffsetBottom = typedArray.getDimensionPixelOffset(
            R.styleable.DrawLineDrawable_r_draw_line_offset_bottom,
            drawLineOffsetBottom
        )
        drawLineColor =
            typedArray.getColor(
                R.styleable.DrawLineDrawable_r_draw_line_color,
                context.loadColor(R.color.colorPrimary)
            )
        drawLineWidth = typedArray.getDimensionPixelOffset(
            R.styleable.DrawLineDrawable_r_draw_line_width,
            drawLineWidth.toInt()
        ).toFloat()
        drawLineOffsetX = typedArray.getDimensionPixelOffset(
            R.styleable.DrawLineDrawable_r_draw_line_offset_x,
            drawLineOffsetX
        )
        drawLineOffsetY = typedArray.getDimensionPixelOffset(
            R.styleable.DrawLineDrawable_r_draw_line_offset_y,
            drawLineOffsetY
        )
        isDashLine =
            typedArray.getBoolean(R.styleable.DrawLineDrawable_r_draw_dash_line, isDashLine)
        drawLineFront =
            typedArray.getBoolean(R.styleable.DrawLineDrawable_r_draw_line_front, drawLineFront)
        lineDrawable = typedArray.getDrawable(R.styleable.DrawLineDrawable_r_draw_line_drawable)

        typedArray.recycle()
    }

    val _tempRect = Rect()

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        if (drawLineType <= DRAW_NONE) {
            return
        }

        when (drawLineType) {
            DRAW_LINE_LEFT -> {
                //左边的线
                val left = drawLineOffsetX + drawLineOffsetLeft
                _tempRect.set(
                    left,
                    drawLineOffsetY + drawLineOffsetTop,
                    (left + drawLineWidth).toInt(),
                    bounds.height() - drawLineOffsetBottom
                )
            }
            DRAW_LINE_TOP -> {
                //顶部的线
                val top = drawLineOffsetY + drawLineOffsetTop
                _tempRect.set(
                    drawLineOffsetX + drawLineOffsetLeft,
                    top,
                    viewWidth - drawLineOffsetRight,
                    (top + drawLineWidth).toInt()
                )
            }
            DRAW_LINE_RIGHT -> {
                //右边的线
                val right = bounds.width() - drawLineOffsetRight - drawLineOffsetX
                _tempRect.set(
                    (right - drawLineWidth).toInt(),
                    drawLineOffsetY + drawLineOffsetTop,
                    right,
                    bounds.height() - drawLineOffsetBottom
                )
            }
            DRAW_LINE_BOTTOM -> {
                //底部的线
                val bottom = bounds.height() - drawLineOffsetBottom - drawLineOffsetY
                _tempRect.set(
                    drawLineOffsetX + drawLineOffsetLeft,
                    (bottom - drawLineWidth).toInt(),
                    viewWidth - drawLineOffsetRight,
                    bottom
                )
            }
            DRAW_LINE_BOTTOM_TOP -> {
                //no op
            }
        }

        if (isDashLine) {
            attachView?.setLayerType(View.LAYER_TYPE_SOFTWARE, textPaint)
            textPaint.pathEffect =
                DashPathEffect(floatArrayOf(4 * density, 5 * density), 0f)
        } else {
            textPaint.pathEffect = null
        }

        textPaint.style = Paint.Style.FILL
        textPaint.strokeWidth = 1f
        textPaint.color = drawLineColor

        if (lineDrawable == null) {
            //未指定特殊的drawable
            canvas.drawRect(_tempRect, textPaint)
        } else {
            lineDrawable?.apply {
                bounds = _tempRect
                draw(canvas)
            }
        }
    }
}