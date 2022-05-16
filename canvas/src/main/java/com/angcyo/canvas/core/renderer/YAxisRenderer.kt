package com.angcyo.canvas.core.renderer

import android.graphics.Canvas
import androidx.core.graphics.withClip
import androidx.core.graphics.withSave
import androidx.core.graphics.withTranslation
import com.angcyo.canvas.CanvasDelegate
import com.angcyo.canvas.core.ICanvasView
import com.angcyo.canvas.core.component.BaseAxis
import com.angcyo.canvas.core.component.YAxis
import com.angcyo.canvas.utils.getScaleY
import com.angcyo.canvas.utils.getTranslateY
import com.angcyo.library.ex.have
import com.angcyo.library.ex.textHeight

/**
 * [YAxis]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/01
 */
class YAxisRenderer(val axis: YAxis, canvasView: ICanvasView) :
    BaseAxisRenderer(canvasView) {

    override fun onCanvasSizeChanged(canvasView: CanvasDelegate) {
        super.onCanvasSizeChanged(canvasView)
        _renderBounds.set(
            0f,
            0f,
            axis.axisSize,
            canvasView.viewBounds.height(),
        )
    }

    override fun updateAxisData() {
        //更新数据
        axis.getPlusPixelList(canvasViewBox)
        axis.getMinusPixelList(canvasViewBox)
    }

    override fun render(canvas: Canvas) {
        val bounds = getRenderBounds()
        val right = bounds.right
        canvas.drawLine(right, bounds.top, right, bounds.bottom, linePaint)

        val plusList = axis.plusList
        val minusList = axis.minusList

        val translateY = canvasViewBox.matrix.getTranslateY()
        val scaleY = canvasViewBox.matrix.getScaleY()

        val contentLeft = canvasViewBox.getContentLeft()
        val contentRight = canvasViewBox.getContentRight()
        val contentTop = canvasViewBox.getContentTop()
        val contentBottom = canvasViewBox.getContentBottom()

        //绘制刻度
        canvas.withClip(0f, contentTop, right, contentBottom) {
            canvas.withTranslation(y = translateY) {
                plusList.forEachIndexed { index, top ->
                    val _top = top * scaleY
                    drawLineAndLabel(canvas, index, _top, right, scaleY)
                }

                minusList.forEachIndexed { index, top ->
                    val _top = top * scaleY
                    drawLineAndLabel(canvas, -index, _top, right, scaleY)
                }
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
        val valueStr = canvasViewBox.valueUnit.getGraduatedLabel(index)
        val axisLineType = axis.getAxisLineType(canvasViewBox, index, scale)
        when {
            axisLineType.have(BaseAxis.LINE_TYPE_PROTRUDE) -> {
                val size = axis.lineProtrudeSize
                canvas.drawLine(right - size, top, right, top, lineProtrudePaint)
            }
            axisLineType.have(BaseAxis.LINE_TYPE_SECONDARY) -> {
                val size = axis.lineSecondarySize
                canvas.drawLine(right - size, top, right, top, lineProtrudePaint)
            }
            axisLineType.have(BaseAxis.LINE_TYPE_NORMAL) -> {
                val size = axis.lineSize
                canvas.drawLine(right - size, top, right, top, lineProtrudePaint)
            }
        }

        if (axisLineType.have(BaseAxis.LINE_TYPE_DRAW_LABEL)) {
            calcLabelPaintSize(axis, valueStr)
            canvas.drawText(
                valueStr,
                axis.labelXOffset,
                top + labelPaint.textHeight() + axis.labelYOffset - labelPaint.descent(),
                labelPaint
            )
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
        val axisLineType = axis.getAxisLineType(canvasViewBox, index, scale)

        if (axisLineType.have(BaseAxis.LINE_TYPE_DRAW_GRID)) {
            if (axisLineType.have(BaseAxis.LINE_TYPE_PROTRUDE)) {
                canvas.drawLine(left, top, right, top, gridProtrudePaint)
            } else if (axisLineType.have(BaseAxis.LINE_TYPE_SECONDARY)) {
                canvas.drawLine(left, top, right, top, gridPaint)
            } else if (axis.drawGridLine && axisLineType.have(BaseAxis.LINE_TYPE_NORMAL)) {
                canvas.drawLine(left, top, right, top, gridPaint)
            }
        }
    }
}