package com.angcyo.drawable.loading

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import com.angcyo.drawable.R
import com.angcyo.drawable.base.BaseSectionDrawable
import com.angcyo.library.ex._color
import com.angcyo.library.ex.alphaRatio
import com.angcyo.library.ex.dpi
import com.angcyo.library.ex.progressColor

/**
 * 圆形放大并且伴随透明度的动画
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/06/14
 */
class CircleScaleLoadingDrawable : BaseSectionDrawable() {

    /**圆的半径*/
    var circleRadius: Int = 14 * dpi

    /**渐变开始的颜色*/
    var circleFromColor = _color(R.color.colorAccent)

    /**渐变结束的颜色*/
    var circleToColor = circleFromColor

    /**圆的宽度*/
    var circleWidth: Int = 2 * dpi

    /**是否是填充的样式*/
    var fillStyle: Boolean = false

    init {
        sections = floatArrayOf(1f)
        loadingStep = 2
        textPaint.style = Paint.Style.STROKE
    }

    override fun initAttribute(context: Context, attributeSet: AttributeSet?) {
        super.initAttribute(context, attributeSet)
        val typedArray =
            context.obtainStyledAttributes(attributeSet, R.styleable.CircleScaleLoadingDrawable)
        circleFromColor = typedArray.getColor(
            R.styleable.CircleScaleLoadingDrawable_r_loading_circle_from_color,
            circleFromColor
        )
        circleToColor = typedArray.getColor(
            R.styleable.CircleScaleLoadingDrawable_r_loading_circle_to_color,
            circleFromColor.alphaRatio(0.3f)
        )
        circleRadius = typedArray.getDimensionPixelOffset(
            R.styleable.CircleScaleLoadingDrawable_r_loading_circle_radius,
            circleRadius
        )
        loadingStep =
            typedArray.getInt(R.styleable.CircleScaleLoadingDrawable_r_loading_step, loadingStep)
        fillStyle =
            typedArray.getBoolean(
                R.styleable.CircleScaleLoadingDrawable_r_loading_circle_fill_style,
                fillStyle
            )
        typedArray.recycle()
    }

    override fun getIntrinsicWidth(): Int {
        return circleRadius * 2
    }

    override fun getIntrinsicHeight(): Int {
        return circleRadius * 2
    }

    fun drawCircle(canvas: Canvas, progress: Float) {
        val radius = circleRadius * progress

        val cx = bounds.centerX()
        val cy = bounds.centerY()

        val color = circleFromColor.progressColor(progress, circleToColor)
        textPaint.color = color
        textPaint.strokeWidth = circleWidth.toFloat()
        textPaint.style = if (fillStyle) Paint.Style.FILL else Paint.Style.STROKE
        canvas.drawCircle(cx.toFloat(), cy.toFloat(), radius, textPaint)
    }

    override fun onDrawProgressSection(
        canvas: Canvas,
        index: Int,
        startProgress: Float,
        endProgress: Float,
        totalProgress: Float,
        sectionProgress: Float
    ) {
        drawCircle(canvas, sectionProgress)
    }
}