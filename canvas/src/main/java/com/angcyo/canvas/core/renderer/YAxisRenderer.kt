package com.angcyo.canvas.core.renderer

import android.graphics.Canvas
import androidx.core.graphics.withClip
import androidx.core.graphics.withSave
import androidx.core.graphics.withTranslation
import com.angcyo.canvas.CanvasDelegate
import com.angcyo.canvas.core.ICanvasView
import com.angcyo.canvas.core.component.AxisPoint
import com.angcyo.canvas.core.component.BaseAxis
import com.angcyo.canvas.core.component.YAxis
import com.angcyo.library.ex.getScaleY
import com.angcyo.library.ex.getTranslateY
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
                plusList.forEachIndexed { index, axisPoint ->
                    val top = axisPoint.pixel * scaleY
                    drawLineAndLabel(canvas, axisPoint.index, axisPoint, top, right)
                }

                minusList.forEachIndexed { index, axisPoint ->
                    val top = axisPoint.pixel * scaleY
                    drawLineAndLabel(canvas, -axisPoint.index, axisPoint, top, right)
                }
            }
        }

        //网格线的绘制
        canvas.withSave {
            clipRect(canvasViewBox.contentRect)
            canvas.withTranslation(y = translateY) {

                minusList.forEachIndexed { index, axisPoint ->
                    val top = axisPoint.pixel * scaleY
                    drawGridLine(canvas, contentLeft, top, contentRight, axisPoint.type)
                }

                plusList.forEachIndexed { index, axisPoint ->
                    val top = axisPoint.pixel * scaleY
                    drawGridLine(canvas, contentLeft, top, contentRight, axisPoint.type)
                }
            }
        }
    }

    fun drawLineAndLabel(
        canvas: Canvas,
        index: Int,
        point: AxisPoint,
        top: Float,
        right: Float,
    ) {
        val valueStr = canvasViewBox.valueUnit.getGraduatedLabel(index, point.gap)
        val axisLineType = point.type
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
        left: Float,
        top: Float,
        right: Float,
        axisLineType: Int
    ) {
        if (!axis.drawGridLine) {
            //不绘制所有线
            return
        }

        //绘制网格
        if (axisLineType.have(BaseAxis.LINE_TYPE_DRAW_GRID)) {
            if (axisLineType.have(BaseAxis.LINE_TYPE_PROTRUDE)) {
                canvas.drawLine(left, top, right, top, gridProtrudePaint)
            } else if (axisLineType.have(BaseAxis.LINE_TYPE_SECONDARY)) {
                canvas.drawLine(left, top, right, top, gridPaint)
            } else if (axis.drawGridLine && axisLineType.have(BaseAxis.LINE_TYPE_NORMAL)) {
                //普通网格线
                canvas.drawLine(left, top, right, top, gridPaint)
            }
        }
    }
}