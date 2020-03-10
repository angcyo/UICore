package com.angcyo.widget.progress

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF
import android.util.AttributeSet
import com.angcyo.drawable.base.AbsDslDrawable
import com.angcyo.library.ex.alpha
import com.angcyo.library.ex.dp
import com.angcyo.library.ex.dpi
import com.angcyo.widget.R

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/10
 */
class DYProgressDrawable : AbsDslDrawable() {
    /**进度条颜色*/
    var progressColor = Color.WHITE
        set(value) {
            field = value
            bgLineColor = value.alpha(0x80)
        }

    /**保底进度*/
    var startProgress = 0.1f

    var bgLineHeight = 0 * dpi
    var bgLineColor = progressColor.alpha(0x80)

    /**圆角大小*/
    var roundSize = 5 * dp

    val drawProgressRect by lazy {
        RectF()
    }

    val drawBgLineRect by lazy {
        RectF()
    }

    var drawProgressColor = progressColor

    var progress = 100

    override fun initAttribute(context: Context, attributeSet: AttributeSet?) {
        val typedArray =
            context.obtainStyledAttributes(attributeSet, R.styleable.DYProgressDrawable)
        progressColor =
            typedArray.getColor(R.styleable.DYProgressDrawable_r_progress_color, progressColor)
        roundSize = typedArray.getDimensionPixelOffset(
            R.styleable.DYProgressDrawable_r_progress_round_size,
            roundSize.toInt()
        ).toFloat()
        bgLineHeight = typedArray.getDimensionPixelOffset(
            R.styleable.DYProgressDrawable_r_progress_bg_line_height,
            bgLineHeight
        )
        typedArray.recycle()

        if (isInEditMode) {
            progress = 50
        }
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        _updateProgressRect(progress / 100f)

        textPaint.color = bgLineColor
        val top = paddingTop + viewDrawHeight / 2 - bgLineHeight / 2f
        drawBgLineRect.set(
            paddingLeft.toFloat(),
            top,
            (viewWidth - paddingRight).toFloat(),
            top + bgLineHeight
        )
        canvas.drawRoundRect(drawBgLineRect, roundSize, roundSize, textPaint)

        textPaint.color = drawProgressColor
        canvas.drawRoundRect(drawProgressRect, roundSize, roundSize, textPaint)
    }

    fun _updateProgressRect(progress: Float) {
        val width = (progress + startProgress) * (viewDrawWidth / 2)
        val left = paddingLeft + viewDrawWidth / 2 - width
        val right = paddingLeft + viewDrawWidth / 2 + width
        drawProgressRect.set(
            left,
            paddingTop.toFloat(),
            right,
            viewHeight - paddingBottom.toFloat()
        )
    }
}