package com.angcyo.canvas.core.renderer

import android.graphics.Canvas
import androidx.core.graphics.withTranslation
import com.angcyo.canvas.CanvasView
import com.angcyo.canvas.core.CanvasViewBox
import com.angcyo.canvas.core.Transformer
import com.angcyo.canvas.core.component.XAxis
import com.angcyo.canvas.utils.getTranslateX

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/01
 */
class XAxisRenderer(val xAxis: XAxis, canvasViewBox: CanvasViewBox, transformer: Transformer) :
    BaseAxisRenderer(canvasViewBox, transformer) {

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
        val bounds = getRenderBounds()
        val bottom = bounds.bottom
        canvas.drawLine(bounds.left, bottom, bounds.right, bottom, linePaint)

        val translateX = canvasViewBox.matrix.getTranslateX()
        canvas.withTranslation(x = translateX) {

            //先/后 clip, 都有效果
            val clipRight = bounds.right - translateX
            val clipLeft = clipRight - bounds.width() + canvasViewBox.getContentLeft()
            clipRect(clipLeft, bounds.top, clipRight, bottom)

            //绘制刻度
            val originLeft = canvasViewBox.getContentLeft()
            xAxis.getLinePointList(canvasViewBox).forEachIndexed { index, left ->
                //相对于原点的像素距离点数值
                val distance = left - originLeft + translateX
                //绘制刻度文本
                val value = canvasViewBox.convertPixelToValue(distance)
                val valueStr = canvasViewBox.formattedValue(value)

                val size = when {
                    value.toInt() % 10 == 0 -> xAxis.lineProtrudeSize
                    value.toInt() % 5 == 0 -> xAxis.lineSecondarySize
                    else -> xAxis.lineSize
                }
                canvas.drawLine(left, bottom, left, bottom - size, linePaint)

                if (value.toInt() % 10 == 0) {
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
}