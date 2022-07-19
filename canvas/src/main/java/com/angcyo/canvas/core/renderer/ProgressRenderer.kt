package com.angcyo.canvas.core.renderer

import android.graphics.*
import androidx.core.graphics.withClip
import com.angcyo.canvas.CanvasDelegate
import com.angcyo.canvas.R
import com.angcyo.canvas.items.renderer.BaseItemRenderer
import com.angcyo.canvas.utils.clamp
import com.angcyo.canvas.utils.createPaint
import com.angcyo.library.ex.*

/**
 * 预览边框, 雕刻进度渲染
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/07/19
 */
class ProgressRenderer(val canvasDelegate: CanvasDelegate) : BaseRenderer(canvasDelegate) {

    /**画笔*/
    val paint = createPaint(Color.RED).apply {
        //init
        textSize = 14 * dp
        strokeWidth = 1 * dp
        style = Paint.Style.FILL
    }

    /**雕刻进度*/
    var progress: Int = -1
        set(value) {
            field = value
            canvasDelegate.refresh()
        }

    /**进度颜色, 不带透明*/
    var progressColor: Int = _color(R.color.canvas_progress_color)

    /**进度文本颜色, 不带透明*/
    var progressTextColor: Int = _color(R.color.canvas_progress_text_color)

    /**用来绘制在目标的渲染器*/
    var targetRenderer: BaseItemRenderer<*>? = null
        set(value) {
            field = value
            canvasDelegate.refresh()
        }

    val clipPath = Path()
    val clipMatrix = Matrix()
    val drawRect = RectF()
    val tempRect = RectF()
    val tempRotateRect = RectF()

    override fun render(canvas: Canvas) {
        if (!isVisible() || progress < 0) {
            return
        }

        targetRenderer?.let {
            val visualBounds = it.getVisualBounds().adjustFlipRect(tempRect)
            val visualRotateBounds = it.getVisualRotateBounds().adjustFlipRect(tempRotateRect)
            val rotate = it.rotate
            drawRect.set(visualRotateBounds)
            drawRect.bottom = visualRotateBounds.top + visualRotateBounds.height() * clamp(
                progress,
                0,
                100
            ) / 100f

            paint.style = Paint.Style.FILL
            paint.shader = linearVerticalGradientShader(
                drawRect.top, drawRect.bottom,
                intArrayOf(Color.TRANSPARENT, progressColor.alphaRatio(0.5f))
            )

            //clip
            clipPath.rewind()
            clipPath.addRect(visualBounds, Path.Direction.CW)
            clipMatrix.reset()
            clipMatrix.setRotate(rotate, visualBounds.centerX(), visualBounds.centerY())
            clipPath.transform(clipMatrix)

            canvas.withClip(clipPath) {
                canvas.drawRect(drawRect, paint)

                //绘制进度
                drawProgressText(canvas, visualRotateBounds)
            }
        }
    }

    /**在[rect]内, 绘制进度文本*/
    fun drawProgressText(canvas: Canvas, rect: RectF) {
        paint.shader = null
        paint.color = progressTextColor
        paint.style = Paint.Style.STROKE

        val cx = rect.centerX()
        val cy = rect.centerY()

        val text = "$progress%"
        val textWidth = paint.textWidth(text)
        val textHeight = paint.textHeight()
        canvas.drawText(
            text,
            cx - textWidth / 2,
            cy + textHeight / 2 - paint.descent(),
            paint
        )
    }
}