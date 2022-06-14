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
import com.angcyo.library.ex.paint

/**
 * 圆形放大并且伴随透明度的动画
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/06/14
 */
class CircleScaleLoadingDrawable : BaseSectionDrawable() {

    /**圆的半径*/
    var circleRadius: Int = 14 * dpi

    /**颜色*/
    var circleColor = _color(R.color.colorAccent)

    /**圆的宽度*/
    var circleWidth: Int = 2 * dpi

    var paint = paint().apply {
        style = Paint.Style.STROKE
    }

    init {
        sections = floatArrayOf(1f)
        loadingStep = 2
    }

    override fun initAttribute(context: Context, attributeSet: AttributeSet?) {
        super.initAttribute(context, attributeSet)
        val typedArray =
            context.obtainStyledAttributes(attributeSet, R.styleable.CircleScaleLoadingDrawable)
        circleColor = typedArray.getColor(
            R.styleable.CircleScaleLoadingDrawable_r_loading_circle_color,
            circleColor
        )
        circleRadius = typedArray.getDimensionPixelOffset(
            R.styleable.CircleScaleLoadingDrawable_r_loading_circle_radius,
            circleRadius
        )
        loadingStep =
            typedArray.getInt(R.styleable.CircleScaleLoadingDrawable_r_loading_step, loadingStep)
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

        val color = circleColor.alphaRatio(1 - progress + 0.3f)
        paint.color = color
        paint.strokeWidth = circleWidth.toFloat()
        canvas.drawCircle(cx.toFloat(), cy.toFloat(), radius, paint)
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