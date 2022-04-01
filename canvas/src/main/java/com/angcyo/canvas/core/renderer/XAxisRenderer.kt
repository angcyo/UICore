package com.angcyo.canvas.core.renderer

import android.graphics.Canvas
import com.angcyo.canvas.CanvasView
import com.angcyo.canvas.core.Transformer
import com.angcyo.canvas.core.ViewBox
import com.angcyo.canvas.core.component.XAxis

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/01
 */
class XAxisRenderer(val xAxis: XAxis, viewBox: ViewBox, transformer: Transformer) :
    BaseAxisRenderer(viewBox, transformer) {

    override fun updateRenderBounds(canvasView: CanvasView) {
        super.updateRenderBounds(canvasView)
        bounds.set(
            0f,
            0f,
            canvasView.measuredWidth.toFloat(),
            xAxis.axisSize
        )
    }

    override fun render(canvas: Canvas) {
        val bottom = bounds.bottom
        canvas.drawLine(bounds.left, bottom, bounds.right, bottom, linePaint)

        //绘制刻度
        xAxis.getLinePointList(viewBox).forEachIndexed { index, left ->
            val size = when {
                index % 10 == 0 -> xAxis.lineProtrudeSize
                index % 5 == 0 -> xAxis.lineSecondarySize
                else -> xAxis.lineSize
            }
            canvas.drawLine(left, bottom, left, bottom - size, linePaint)

            if (index % 10 == 0) {

                //绘制刻度文本
                val value = viewBox.convertPixelToValue(left - viewBox.getContentLeft())
                val valueStr = viewBox.formattedValue(value)
                canvas.drawText(
                    valueStr,
                    left + xAxis.labelXOffset,
                    xAxis.labelYOffset - labelPaint.ascent(),
                    labelPaint
                )
            }
        }
    }
}