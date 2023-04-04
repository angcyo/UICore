package com.angcyo.canvas.render.renderer

import android.graphics.*
import androidx.core.graphics.withClip
import com.angcyo.canvas.render.R
import com.angcyo.canvas.render.annotation.CanvasInsideCoordinate
import com.angcyo.canvas.render.core.CanvasRenderDelegate
import com.angcyo.canvas.render.core.IRenderer
import com.angcyo.canvas.render.data.RenderParams
import com.angcyo.canvas.render.util.createRenderPaint
import com.angcyo.library._refreshRateRatio
import com.angcyo.library.annotation.Pixel
import com.angcyo.library.ex.*

/**
 * 边框, 雕刻进度渲染渲染
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/04/03
 */
class ProgressRenderer(val delegate: CanvasRenderDelegate?) : BaseRenderer() {

    init {
        renderFlags = renderFlags.remove(IRenderer.RENDERER_FLAG_ON_VIEW)
            .remove(IRenderer.RENDERER_FLAG_ON_INSIDE)
        //.remove(IRenderer.RENDERER_FLAG_ON_OUTSIDE)
    }

    /**画笔*/
    val paint = createRenderPaint(Color.RED).apply {
        //init
        textSize = 14 * dp
        strokeWidth = 1 * dp
        style = Paint.Style.FILL
    }

    //---

    /**是否要渲染进度
     * [progress]
     * [borderBounds]
     * */
    var renderProgress: Boolean = false

    /**雕刻进度*/
    var progress: Int = -1
        set(value) {
            field = value
            delegate?.refresh()
        }

    /**绘制进度的位置, 未旋转的坐标*/
    @Pixel
    @CanvasInsideCoordinate
    var progressRect: RectF? = null

    /**进度旋转的角度, 用来计算clipPath*/
    var progressRotate: Float? = null

    /**进度颜色, 不带透明*/
    var progressColor: Int = _color(R.color.canvas_render_select)

    /**进度文本颜色, 不带透明*/
    var progressTextColor: Int = _color(R.color.error)

    //---

    /**是否要渲染蚂蚁线边框
     * [borderBounds]*/
    var renderBorder: Boolean = false

    /**边框*/
    @Pixel
    @CanvasInsideCoordinate
    var borderBounds: RectF? = null

    /**边框的颜色*/
    var borderColor: Int = _color(R.color.canvas_render_select)

    /**蚂蚁线间隔*/
    var intervals = floatArrayOf(10 * dp, 20 * dp)

    /**偏移距离*/
    var phase = 0f

    /**
     * 正数是逆时针动画
     * 负数是顺时针动画
     * */
    var phaseStep = -2

    override fun isVisibleInRender(
        delegate: CanvasRenderDelegate?,
        fullIn: Boolean,
        def: Boolean
    ): Boolean {
        return isVisible
    }

    override fun renderOnOutside(canvas: Canvas, params: RenderParams) {
        if (renderProgress) {
            _drawProgressMode(canvas)
        }
        if (renderBorder) {
            _drawBorderMode(canvas)
        }
    }

    //---

    /**绘制边框*/
    private fun _drawBorderMode(canvas: Canvas) {
        borderBounds?.let {
            delegate?.renderViewBox?.transformToOutside(it, _tempRect)
            drawBorder(canvas, _tempRect)
        }
    }

    /**绘制进度模式*/
    private fun _drawProgressMode(canvas: Canvas) {
        progressRect?.let {
            delegate?.renderViewBox?.transformToOutside(it, _tempRect)
            drawProgress(canvas, _tempRect)
        }
    }

    //---

    /**在[rect]内, 绘制进度文本*/
    private fun drawProgressText(canvas: Canvas, rect: RectF) {
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

    private val _clipPath = Path()
    private val _borderPath = Path()
    private val _drawRect = RectF()
    private val _rotateMatrix = Matrix()

    /**绘制矩形[rect], 支持旋转属性
     * [rect] 需要绘制的矩形, 不带旋转
     * [rotate] 需要旋转的角度
     * */
    private fun drawBorder(canvas: Canvas, rect: RectF) {
        //不需要转换
        _borderPath.rewind()
        _borderPath.addRect(rect, Path.Direction.CW)

        //开始绘制
        paint.shader = null
        paint.style = Paint.Style.STROKE
        paint.color = borderColor
        paint.pathEffect = DashPathEffect(intervals, phase)
        canvas.drawPath(_borderPath, paint)

        //动画
        phase += phaseStep / _refreshRateRatio
        if (phaseStep < 0 && phase < -intervals.sum()) {
            phase = 0f
        } else if (phaseStep > 0 && phase > intervals.sum()) {
            phase = 0f
        }
        delegate?.refresh()
    }

    /**绘制进度文本和clip的rect*/
    private fun drawProgress(canvas: Canvas, rect: RectF) {
        val rotate = progressRotate ?: 0f
        _rotateMatrix.setRotate(rotate, rect.centerX(), rect.centerY())

        _drawRect.set(rect)
        _rotateMatrix.mapRect(_drawRect)

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
        _clipPath.addRect(rect, Path.Direction.CW)
        _clipPath.transform(_rotateMatrix)

        canvas.withClip(_clipPath) {
            canvas.drawRect(_drawRect, paint) //进度提示

            //绘制进度
            drawProgressText(canvas, _drawRect) //进度文本提示
        }
    }

}