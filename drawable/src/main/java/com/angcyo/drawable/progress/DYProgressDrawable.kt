package com.angcyo.drawable.progress

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF
import android.util.AttributeSet
import androidx.core.math.MathUtils.clamp
import com.angcyo.drawable.R
import com.angcyo.drawable.base.AbsDslDrawable
import com.angcyo.library.ex.alpha
import com.angcyo.library.ex.dp
import com.angcyo.library.ex.dpi
import com.angcyo.library.ex.evaluateColor

/**
 * 从中间开始, 像两端拉长
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

    /**当前进度[0-100]*/
    var progress = 0
        set(value) {
            field = clamp(value, 0, 100)
            invalidateSelf()
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

    //绘制的进度颜色
    val _progressDrawColor: Int
        get() = evaluateColor(progress * 1f / 100, progressColor, bgLineColor /*Color.TRANSPARENT*/)

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
        startProgress =
            typedArray.getFloat(R.styleable.DYProgressDrawable_r_min_start_progress, startProgress)
        typedArray.recycle()

        if (isInEditMode) {
            progress = 50
            drawType = DRAW_TYPE_ON_DRAW_AFTER
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

        textPaint.color = _progressDrawColor
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