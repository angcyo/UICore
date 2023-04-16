package com.angcyo.canvas.render.renderer

import android.graphics.Canvas
import android.graphics.Paint
import com.angcyo.canvas.render.core.CanvasRenderDelegate
import com.angcyo.canvas.render.core.IRenderer
import com.angcyo.canvas.render.data.RenderParams
import com.angcyo.canvas.render.util.createRenderPaint
import com.angcyo.library.ex.*
import com.angcyo.library.utils.Device
import kotlin.math.roundToInt

/**画板缩放比例监测信息绘制
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/16
 */
class CanvasMonitorRenderer(val delegate: CanvasRenderDelegate) : IRenderer {

    val paint = createRenderPaint().apply {
        textSize = 9 * dp
    }

    /**是否绘制文本内存文本信息*/
    var drawMemoryTip: Boolean = isDebug()

    override var renderFlags: Int = 0xf

    override fun renderOnView(canvas: Canvas, params: RenderParams) {
        /*if (BuildConfig.DEBUG) {
            paint.style = Paint.Style.STROKE
            canvas.drawRect(renderViewBox.renderBounds, paint)
        }*/

        if (drawMemoryTip) {
            drawMemoryText(canvas, delegate.view.measuredHeight - paint.textHeight())
        }
        drawScaleText(canvas, delegate.view.measuredHeight.toFloat())
    }

    /**绘制缩放比例文本*/
    private fun drawScaleText(canvas: Canvas, bottom: Float) {
        val renderViewBox = delegate.renderViewBox
        val text = "${(renderViewBox.getScale() * 100).roundToInt()}%"
        paint.style = Paint.Style.FILL

        val x = if (drawMemoryTip) delegate.axisManager.yAxisBounds.right else
            delegate.view.measuredWidth - paint.textWidth(text)
        val y = bottom - paint.descent()
        canvas.drawText(text, x, y, paint)
    }

    /**绘制内存使用情况*/
    private fun drawMemoryText(canvas: Canvas, bottom: Float) {
        val text =
            "${Runtime.getRuntime().freeMemory().toSizeString()}/${Device.getMemoryUseInfo()}"
        paint.style = Paint.Style.FILL

        val x = if (drawMemoryTip) delegate.axisManager.yAxisBounds.right else
            delegate.view.measuredWidth - paint.textWidth(text)
        val y = bottom - paint.descent()
        canvas.drawText(text, x, y, paint)
    }
}