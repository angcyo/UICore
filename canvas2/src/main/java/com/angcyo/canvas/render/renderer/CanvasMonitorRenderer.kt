package com.angcyo.canvas.render.renderer

import android.graphics.Canvas
import android.graphics.Paint
import com.angcyo.canvas.render.core.CanvasRenderDelegate
import com.angcyo.canvas.render.core.IRenderer
import com.angcyo.canvas.render.data.RendererParams
import com.angcyo.canvas.render.util.createRenderPaint
import com.angcyo.library.ex.dp
import com.angcyo.library.ex.textWidth
import kotlin.math.roundToInt

/**画板缩放比例监测信息绘制
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/16
 */
class CanvasMonitorRenderer(val delegate: CanvasRenderDelegate) : IRenderer {

    val paint = createRenderPaint().apply {
        textSize = 9 * dp
    }

    override var renderFlags: Int = 0xf

    override fun renderOnView(canvas: Canvas, params: RendererParams) {
        val renderViewBox = delegate.renderViewBox

        /*if (BuildConfig.DEBUG) {
            paint.style = Paint.Style.STROKE
            canvas.drawRect(renderViewBox.renderBounds, paint)
        }*/

        val text = "${(renderViewBox.getScale() * 100).roundToInt()}%"
        paint.style = Paint.Style.FILL

        val x = delegate.view.measuredWidth - paint.textWidth(text)
        val y = delegate.view.measuredHeight - paint.descent()
        canvas.drawText(text, x, y, paint)
    }
}