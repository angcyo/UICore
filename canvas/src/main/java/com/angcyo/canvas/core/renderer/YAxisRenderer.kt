package com.angcyo.canvas.core.renderer

import android.graphics.Canvas
import com.angcyo.canvas.CanvasView
import com.angcyo.canvas.core.Transformer
import com.angcyo.canvas.core.CanvasViewBox
import com.angcyo.canvas.core.component.YAxis
import com.angcyo.drawable.textHeight

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/01
 */
class YAxisRenderer(val yAxis: YAxis, canvasViewBox: CanvasViewBox, transformer: Transformer) :
    BaseAxisRenderer(canvasViewBox, transformer) {

    override fun updateRenderBounds(canvasView: CanvasView) {
        super.updateRenderBounds(canvasView)
        bounds.set(
            0f,
            0f,
            yAxis.axisSize,
            canvasView.measuredHeight.toFloat(),
        )
    }

    override fun render(canvas: Canvas) {
        val right = bounds.right
        canvas.drawLine(right, bounds.top, right, bounds.bottom, linePaint)

        //绘制刻度
        yAxis.getLinePointList(canvasViewBox).forEachIndexed { index, top ->
            val size = when {
                index % 10 == 0 -> yAxis.lineProtrudeSize
                index % 5 == 0 -> yAxis.lineSecondarySize
                else -> yAxis.lineSize
            }
            canvas.drawLine(right - size, top, right, top, linePaint)

            if (index % 10 == 0) {

                //绘制刻度文本
                val value = canvasViewBox.convertPixelToValue(top - canvasViewBox.getContentTop())
                val valueStr = canvasViewBox.formattedValue(value)
                canvas.drawText(
                    valueStr,
                    yAxis.labelXOffset,
                    top + labelPaint.textHeight() + yAxis.labelYOffset,
                    labelPaint
                )
            }
        }
    }
}