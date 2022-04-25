package com.angcyo.canvas.core.renderer

import android.graphics.Canvas
import androidx.core.graphics.withClip
import androidx.core.graphics.withSave
import androidx.core.graphics.withTranslation
import com.angcyo.canvas.CanvasView
import com.angcyo.canvas.core.CanvasViewBox
import com.angcyo.canvas.core.component.BaseAxis
import com.angcyo.canvas.core.component.XAxis
import com.angcyo.library.ex.have

/**
 * [XAxis]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/01
 */
class XAxisRenderer(val axis: XAxis, canvasViewBox: CanvasViewBox) :
    BaseAxisRenderer(canvasViewBox) {

    override fun onCanvasSizeChanged(canvasView: CanvasView) {
        super.onCanvasSizeChanged(canvasView)
        _renderBounds.set(
            0f,
            0f,
            canvasView.measuredWidth.toFloat(),
            axis.axisSize
        )
    }

    override fun updateAxisData() {
        //更新数据
        axis.getPlusPixelList(canvasViewBox)
        axis.getMinusPixelList(canvasViewBox)
    }

    override fun render(canvas: Canvas) {
        val bounds = getRenderBounds()
        val bottom = bounds.bottom
        canvas.drawLine(bounds.left, bottom, bounds.right, bottom, linePaint)

        val plusList = axis.plusList
        val minusList = axis.minusList

        val translateX = canvasViewBox.getTranslateX()
        val scaleX = canvasViewBox.getScaleX()

        val contentLeft = canvasViewBox.getContentLeft()
        val contentRight = canvasViewBox.getContentRight()
        val contentTop = canvasViewBox.getContentTop()
        val contentBottom = canvasViewBox.getContentBottom()

        //绘制刻度
        canvas.withClip(contentLeft, 0f, contentRight, bottom) {
            canvas.withTranslation(x = translateX) {
                plusList.forEachIndexed { index, left ->
                    val _left = left * scaleX
                    drawLineAndLabel(canvas, index, _left, bottom, scaleX)
                }

                minusList.forEachIndexed { index, left ->
                    val _left = left * scaleX
                    drawLineAndLabel(canvas, -index, _left, bottom, scaleX)
                }
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
        val valueStr = canvasViewBox.valueUnit.getGraduatedLabel(index)
        val axisLineType = axis.getAxisLineType(canvasViewBox, index, scale)

        when {
            axisLineType.have(BaseAxis.LINE_TYPE_PROTRUDE) -> {
                val size = axis.lineProtrudeSize
                canvas.drawLine(left, bottom, left, bottom - size, lineProtrudePaint)
            }
            axisLineType.have(BaseAxis.LINE_TYPE_SECONDARY) -> {
                val size = axis.lineSecondarySize
                canvas.drawLine(left, bottom, left, bottom - size, linePaint)
            }
            axisLineType.have(BaseAxis.LINE_TYPE_NORMAL) -> {
                val size = axis.lineSize
                canvas.drawLine(left, bottom, left, bottom - size, linePaint)
            }
        }

        if (axisLineType.have(BaseAxis.LINE_TYPE_DRAW_LABEL)) {
            calcLabelPaintSize(axis, valueStr)
            canvas.drawText(
                valueStr,
                left + axis.labelXOffset,
                axis.labelYOffset - labelPaint.ascent(),
                labelPaint
            )
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
        if (axis.drawGridLine) {
            val axisLineType = axis.getAxisLineType(canvasViewBox, index, scale)

            if (axisLineType.have(BaseAxis.LINE_TYPE_DRAW_GRID)) {
                if (axisLineType.have(BaseAxis.LINE_TYPE_PROTRUDE)) {
                    canvas.drawLine(left, top, left, bottom, gridProtrudePaint)
                } else if (axisLineType.have(BaseAxis.LINE_TYPE_SECONDARY) ||
                    axisLineType.have(BaseAxis.LINE_TYPE_NORMAL)
                ) {
                    canvas.drawLine(left, top, left, bottom, gridPaint)
                }
            }
        }
    }
}