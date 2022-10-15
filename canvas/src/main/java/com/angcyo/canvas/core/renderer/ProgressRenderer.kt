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
    var progressColor: Int = _color(R.color.canvas_progress_color, canvasDelegate.view.context)

    /**进度文本颜色, 不带透明*/
    var progressTextColor: Int =
        _color(R.color.canvas_progress_text_color, canvasDelegate.view.context)

    /**边框的颜色*/
    var borderColor: Int = _color(R.color.canvas_progress_color, canvasDelegate.view.context)

    //---

    /**直接绘制的边框矩形, 和[borderRenderer]属性互斥
     * 这个矩形是可以直接绘制的矩形, 所有如果是坐标系的矩形还需要进行一次转换.
     * [com.angcyo.canvas.core.CanvasViewBox.coordinateSystemRectToViewRect]*/
    var borderRect: RectF? = null

    /**[borderRect]旋转的角度, 和[drawRotateBorder]互斥*/
    var borderRectRotate: Float? = null

    //---

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

    val _clipPath = Path()
    val _borderPath = Path()
    val _clipMatrix = Matrix()
    val _drawRect = RectF()
    val _tempRect = RectF()
    val _tempRotateRect = RectF()

    override fun render(canvas: Canvas) {
        if (!isVisible()) {
            return
        }

        /*if (BuildConfig.DEBUG) {
            targetRenderer = canvasDelegate.getSelectedRenderer()
        }*/

        //模式
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
        //直接绘制矩形
        borderRect?.let {
            drawBorder(canvas, it, borderRectRotate)
            if (drawProgressMode && progress >= 0) {
                drawProgress(canvas, it, borderRectRotate)
            }
        }
    }

    //---

    /**绘制边框*/
    fun _drawBorderMode(canvas: Canvas, renderer: BaseItemRenderer<*>) {
        if (drawRotateBorder) {
            val bounds = renderer.getVisualBounds()
            drawBorder(canvas, bounds, renderer.rotate)
        } else {
            val visualRotateBounds =
                renderer.getVisualRotateBounds().adjustFlipRect(_tempRotateRect)
            drawBorder(canvas, visualRotateBounds)
        }
    }

    /**绘制进度模式*/
    fun _drawProgressMode(canvas: Canvas, renderer: BaseItemRenderer<*>) {
        val visualBounds = renderer.getVisualBounds().adjustFlipRect(_tempRect)
        val rotate = renderer.rotate
        drawProgress(canvas, visualBounds, rotate)
    }

    //---

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

    /**绘制矩形[rect], 支持旋转属性
     * [rect] 需要绘制的矩形, 不带旋转
     * [rotate] 需要旋转的角度
     * */
    fun drawBorder(canvas: Canvas, rect: RectF, rotate: Float? = null) {
        if (rotate == null || rotate == 0f) {
            //不需要转换
            _borderPath.rewind()
            _borderPath.addRect(rect, Path.Direction.CW)
        } else {
            //需要旋转
            rect.rotateToPath(rotate, result = _borderPath)
        }

        //开始绘制
        paint.shader = null
        paint.style = Paint.Style.STROKE
        paint.color = borderColor
        paint.pathEffect = DashPathEffect(intervals, phase)
        canvas.drawPath(_borderPath, paint)

        //动画
        phase += phaseStep
        if (phaseStep < 0 && phase < -intervals.sum()) {
            phase = 0f
        } else if (phaseStep > 0 && phase > intervals.sum()) {
            phase = 0f
        }
        refresh()
    }

    /**绘制进度文本和clip的rect*/
    fun drawProgress(canvas: Canvas, rect: RectF, rotate: Float? = null) {
        _tempRotateRect.set(rect)
        if (rotate == null || rotate == 0f) {
            //不需要转换
        } else {
            //需要旋转
            _tempRotateRect.rotate(rotate)
        }
        _drawRect.set(_tempRotateRect)

        _drawRect.bottom = _drawRect.top + _drawRect.height() * clamp(
            progress,
            0,
            100
        ) / 100f

        paint.style = Paint.Style.FILL
        paint.shader = linearVerticalGradientShader(
            _drawRect.top, _drawRect.bottom,
            intArrayOf(Color.TRANSPARENT, progressColor.alphaRatio(0.5f))
        )
        paint.pathEffect = null

        //clip
        _clipPath.rewind()
        _clipPath.addRect(_tempRotateRect, Path.Direction.CW)

        canvas.withClip(_clipPath) {
            canvas.drawRect(_drawRect, paint) //进度提示

            //绘制进度
            drawProgressText(canvas, _tempRotateRect) //进度文本提示
        }
    }

}