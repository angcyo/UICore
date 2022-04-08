package com.angcyo.canvas.core.renderer

import android.graphics.Canvas
import androidx.core.graphics.withSave
import androidx.core.graphics.withTranslation
import com.angcyo.canvas.CanvasView
import com.angcyo.canvas.core.CanvasViewBox
import com.angcyo.canvas.core.component.BaseAxis
import com.angcyo.canvas.core.component.YAxis
import com.angcyo.canvas.utils.getScaleY
import com.angcyo.canvas.utils.getTranslateY
import com.angcyo.drawable.textHeight

/**
 * [YAxis]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/01
 */
class YAxisRenderer(val axis: YAxis, canvasViewBox: CanvasViewBox) :
    BaseAxisRenderer(canvasViewBox) {

    override fun updateRenderBounds(canvasView: CanvasView) {
        super.updateRenderBounds(canvasView)
        bounds.set(
            0f,
            0f,
            axis.axisSize,
            canvasView.measuredHeight.toFloat(),
        )
    }

    override fun render(canvas: Canvas) {
        val right = bounds.right
        canvas.drawLine(right, bounds.top, right, bounds.bottom, linePaint)

        val plusList = axis.getPlusPixelList(canvasViewBox)
        val minusList = axis.getMinusPixelList(canvasViewBox)

        val translateY = canvasViewBox.matrix.getTranslateY()
        val scaleY = canvasViewBox.matrix.getScaleY()

        val contentLeft = canvasViewBox.getContentLeft()
        val contentRight = canvasViewBox.getContentRight()
        val contentTop = canvasViewBox.getContentTop()
        val contentBottom = canvasViewBox.getContentBottom()

        //绘制刻度
        canvas.withTranslation(y = translateY) {

            //先/后 clip, 都有效果
            val clipBottom = contentBottom - translateY
            val clipTop = clipBottom - bounds.height() + contentTop
            clipRect(bounds.left, clipTop, bounds.right, clipBottom)

            plusList.forEachIndexed { index, top ->
                val _top = top * scaleY
                drawLineAndLabel(canvas, index, _top, right, scaleY)
            }

            minusList.forEachIndexed { index, top ->
                val _top = top * scaleY
                drawLineAndLabel(canvas, -index, _top, right, scaleY)
            }
        }

        //网格线的绘制
        canvas.withSave {
            clipRect(canvasViewBox.contentRect)
            canvas.withTranslation(y = translateY) {

                minusList.forEachIndexed { index, top ->
                    val _top = top * scaleY
                    drawGridLine(canvas, -index, contentLeft, _top, contentRight, scaleY)
                }

                plusList.forEachIndexed { index, top ->
                    val _top = top * scaleY
                    drawGridLine(canvas, -index, contentLeft, _top, contentRight, scaleY)
                }
            }
        }
    }

    fun drawLineAndLabel(
        canvas: Canvas,
        index: Int,
        top: Float,
        right: Float,
        scale: Float
    ) {
        val valueStr = "$index"

        when (axis.getAxisLineType(index, scale)) {
            BaseAxis.LINE_TYPE_PROTRUDE -> {
                val size = axis.lineProtrudeSize
                canvas.drawLine(right - size, top, right, top, lineProtrudePaint)

                canvas.drawText(
                    valueStr,
                    axis.labelXOffset,
                    top + labelPaint.textHeight() + axis.labelYOffset,
                    labelPaint
                )
            }
            BaseAxis.LINE_TYPE_SECONDARY -> {
                val size = axis.lineSecondarySize
                canvas.drawLine(right - size, top, right, top, linePaint)
            }
            BaseAxis.LINE_TYPE_NORMAL -> {
                val size = axis.lineSize
                canvas.drawLine(right - size, top, right, top, linePaint)
            }
        }
    }

    fun drawGridLine(
        canvas: Canvas,
        index: Int,
        left: Float,
        top: Float,
        right: Float,
        scale: Float
    ) {
        //绘制网格
        val type = axis.getAxisLineType(index, scale)
        if (axis.drawGridLine) {
            if (type == BaseAxis.LINE_TYPE_PROTRUDE) {
                canvas.drawLine(left, top, right, top, gridProtrudePaint)
            } else if (type != BaseAxis.LINE_TYPE_NONE) {
                canvas.drawLine(left, top, right, top, gridPaint)
            }
        }
    }
}