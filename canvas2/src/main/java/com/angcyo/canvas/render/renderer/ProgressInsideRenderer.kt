package com.angcyo.canvas.render.renderer

import android.graphics.*
import androidx.core.graphics.withClip
import com.angcyo.canvas.render.R
import com.angcyo.canvas.render.core.CanvasRenderDelegate
import com.angcyo.canvas.render.core.IRenderer
import com.angcyo.canvas.render.data.RenderParams
import com.angcyo.canvas.render.util.createRenderPaint
import com.angcyo.library._refreshRateRatio
import com.angcyo.library.ex.*

/**
 * 边框, 雕刻进度渲染渲染
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/04/03
 */
class ProgressInsideRenderer(val delegate: CanvasRenderDelegate?) : BaseRenderer() {

    init {
        renderFlags = renderFlags.remove(IRenderer.RENDERER_FLAG_ON_OUTSIDE)
            .remove(IRenderer.RENDERER_FLAG_ON_VIEW)
    }

    /**画笔*/
    val paint = createRenderPaint(Color.RED).apply {
        //init
        textSize = 14 * dp
        strokeWidth = 1 * dp
        style = Paint.Style.FILL
    }

    /**雕刻进度*/
    var progress: Int = -1
        set(value) {
            field = value
            delegate?.refresh()
        }

    /**进度颜色, 不带透明*/
    var progressColor: Int = _color(R.color.canvas_render_select)

    /**进度文本颜色, 不带透明*/
    var progressTextColor: Int = _color(R.color.error)

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
        return super.isVisibleInRender(delegate, fullIn, def)
    }

    override fun renderOnInside(canvas: Canvas, params: RenderParams) {
        //模式
        /*progressRenderer?.let {
            if (drawProgressMode && progress >= 0) {
                _drawProgressMode(canvas, it)
            }
        }
        borderRenderer?.let {
            if (drawBorderMode) {
                _drawBorderMode(canvas, it)
            }
        }*/
    }

    //---

    /**绘制边框*/
    private fun _drawBorderMode(canvas: Canvas, renderer: BaseRenderer) {
        val visualBounds = renderer.getRendererBounds() ?: return
        drawBorder(canvas, visualBounds)
    }

    /**绘制进度模式*/
    private fun _drawProgressMode(canvas: Canvas, renderer: BaseRenderer) {
        val visualBounds = renderer.getRendererBounds() ?: return
        drawProgress(canvas, visualBounds)
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
        _drawRect.set(rect)

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
        _clipPath.addRect(_drawRect, Path.Direction.CW)

        canvas.withClip(_clipPath) {
            canvas.drawRect(_drawRect, paint) //进度提示

            //绘制进度
            drawProgressText(canvas, _drawRect) //进度文本提示
        }
    }

}