package com.angcyo.canvas.core.renderer

import android.graphics.Canvas
import android.graphics.Matrix
import androidx.core.graphics.withSave
import androidx.core.graphics.withTranslation
import com.angcyo.canvas.CanvasView
import com.angcyo.canvas.core.CanvasViewBox
import com.angcyo.canvas.core.component.BaseAxis
import com.angcyo.canvas.core.component.XAxis

/**
 * [XAxis]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/01
 */
class XAxisRenderer(val axis: XAxis, canvasViewBox: CanvasViewBox) :
    BaseAxisRenderer(canvasViewBox) {

    override fun onCanvasSizeChanged(canvasView: CanvasView) {
        super.onCanvasSizeChanged(canvasView)
        _bounds.set(
            0f,
            0f,
            canvasView.measuredWidth.toFloat(),
            axis.axisSize
        )
    }

    override fun onCanvasMatrixUpdate(canvasView: CanvasView, matrix: Matrix, oldValue: Matrix) {
        super.onCanvasMatrixUpdate(canvasView, matrix, oldValue)
        //更新数据
        axis.getPlusPixelList(canvasViewBox)
        axis.getMinusPixelList(canvasViewBox)
    }

    override fun render(canvasView: CanvasView, canvas: Canvas) {
        val bounds = getRendererBounds()
        val bottom = bounds.bottom
        canvas.drawLine(bounds.left, bottom, bounds.right, bottom, linePaint)

        val plusList = axis.plusList
        val minusList = axis.minusList

        val translateX = canvasViewBox._translateX
        val scaleX = canvasViewBox._scaleX

        val contentLeft = canvasViewBox.getContentLeft()
        val contentRight = canvasViewBox.getContentRight()
        val contentTop = canvasViewBox.getContentTop()
        val contentBottom = canvasViewBox.getContentBottom()

        //绘制刻度
        canvas.withTranslation(x = translateX) {

            //先/后 clip, 都有效果
            val clipRight = contentRight - translateX
            val clipLeft = clipRight - bounds.width() + contentLeft
            clipRect(clipLeft, bounds.top, clipRight, bottom)

            plusList.forEachIndexed { index, left ->
                val _left = left * scaleX
                drawLineAndLabel(canvas, index, _left, bottom, scaleX)
            }

            minusList.forEachIndexed { index, left ->
                val _left = left * scaleX
                drawLineAndLabel(canvas, -index, _left, bottom, scaleX)
            }
        }

        //网格线的绘制
        canvas.withSave {
            clipRect(canvasViewBox.contentRect)

            canvas.withTranslation(x = translateX) {
                minusList.forEachIndexed { index, left ->
                    val _left = left * scaleX
                    drawGridLine(canvas, -index, _left, contentTop, contentBottom, scaleX)
                }

                plusList.forEachIndexed { index, left ->
                    val _left = left * scaleX
                    drawGridLine(canvas, -index, _left, contentTop, contentBottom, scaleX)
                }
            }
        }
    }

    fun drawLineAndLabel(canvas: Canvas, index: Int, left: Float, bottom: Float, scale: Float) {
        val valueStr = "$index"

        when (axis.getAxisLineType(index, scale)) {
            BaseAxis.LINE_TYPE_PROTRUDE -> {
                val size = axis.lineProtrudeSize
                canvas.drawLine(left, bottom, left, bottom - size, lineProtrudePaint)
                canvas.drawText(
                    valueStr,
                    left + axis.labelXOffset,
                    axis.labelYOffset - labelPaint.ascent(),
                    labelPaint
                )
            }
            BaseAxis.LINE_TYPE_SECONDARY -> {
                val size = axis.lineSecondarySize
                canvas.drawLine(left, bottom, left, bottom - size, linePaint)
            }
            BaseAxis.LINE_TYPE_NORMAL -> {
                val size = axis.lineSize
                canvas.drawLine(left, bottom, left, bottom - size, linePaint)
            }
        }
    }

    fun drawGridLine(
        canvas: Canvas,
        index: Int,
        left: Float,
        top: Float,
        bottom: Float,
        scale: Float
    ) {
        //绘制网格
        val type = axis.getAxisLineType(index, scale)
        if (axis.drawGridLine) {
            if (type == BaseAxis.LINE_TYPE_PROTRUDE) {
                canvas.drawLine(left, top, left, bottom, gridProtrudePaint)
            } else if (type != BaseAxis.LINE_TYPE_NONE) {
                canvas.drawLine(left, top, left, bottom, gridPaint)
            }
        }
    }
}