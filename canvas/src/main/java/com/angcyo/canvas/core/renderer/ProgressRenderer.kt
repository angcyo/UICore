package com.angcyo.canvas.core.renderer

import android.graphics.*
import androidx.core.graphics.withClip
import com.angcyo.canvas.CanvasDelegate
import com.angcyo.canvas.R
import com.angcyo.canvas.items.renderer.BaseItemRenderer
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

    /**用来绘制进度目标的渲染器*/
    var progressRenderer: BaseItemRenderer<*>? = null
        set(value) {
            field = value
            canvasDelegate.refresh()
        }

    /**用来绘制边框目标的渲染器*/
    var borderRenderer: BaseItemRenderer<*>? = null
        set(value) {
            field = value
            canvasDelegate.refresh()
        }

    /**绘制进度*/
    var drawProgressMode = true
        set(value) {
            field = value
            canvasDelegate.refresh()
        }

    /**绘制边框*/
    var drawBorderMode = true
        set(value) {
            field = value
            canvasDelegate.refresh()
        }

    /**是否绘制旋转之后的边框*/
    var drawRotateBorder = false
        set(value) {
            field = value
            canvasDelegate.refresh()
        }

    /**蚂蚁线间隔*/
    var intervals = floatArrayOf(10 * dp, 20 * dp)

    /**偏移距离*/
    var phase = 0f

    /**
     * 正数是逆时针动画
     * 负数是顺时针动画
     * */
    var phaseStep = -2

    //---

    val clipPath = Path()
    val borderPath = Path()
    val clipMatrix = Matrix()
    val drawRect = RectF()
    val tempRect = RectF()
    val tempRotateRect = RectF()

    override fun render(canvas: Canvas) {
        if (!isVisible()) {
            return
        }

        /*if (BuildConfig.DEBUG) {
            targetRenderer = canvasDelegate.getSelectedRenderer()
        }*/

        progressRenderer?.let {
            if (drawProgressMode && progress >= 0) {
                _drawProgressMode(canvas, it)
            }
        }
        borderRenderer?.let {
            if (drawBorderMode) {
                _drawBorderMode(canvas, it)
            }
        }
    }

    /**绘制边框*/
    fun _drawBorderMode(canvas: Canvas, renderer: BaseItemRenderer<*>) {
        if (drawRotateBorder) {
            val bounds = renderer.getVisualBounds()
            bounds.rotateToPath(renderer.rotate, result = borderPath)
        } else {
            val visualRotateBounds = renderer.getVisualRotateBounds().adjustFlipRect(tempRotateRect)
            borderPath.rewind()
            borderPath.addRect(visualRotateBounds, Path.Direction.CW)
        }

        paint.shader = null
        paint.style = Paint.Style.STROKE
        paint.color = progressTextColor
        paint.pathEffect = DashPathEffect(intervals, phase)
        canvas.drawPath(borderPath, paint)

        //动画
        phase += phaseStep
        if (phaseStep < 0 && phase < -intervals.sum()) {
            phase = 0f
        } else if (phaseStep > 0 && phase > intervals.sum()) {
            phase = 0f
        }
        refresh()
    }

    /**绘制进度模式*/
    fun _drawProgressMode(canvas: Canvas, renderer: BaseItemRenderer<*>) {
        val visualBounds = renderer.getVisualBounds().adjustFlipRect(tempRect)
        val visualRotateBounds = renderer.getVisualRotateBounds().adjustFlipRect(tempRotateRect)
        val rotate = renderer.rotate
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
        paint.pathEffect = null

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

    /**在[rect]内, 绘制进度文本*/
    fun drawProgressText(canvas: Canvas, rect: RectF) {
        paint.shader = null
        paint.color = progressTextColor
        paint.style = Paint.Style.FILL_AND_STROKE

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