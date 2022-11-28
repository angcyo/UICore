package com.angcyo.canvas.core.renderer

import android.graphics.Canvas
import androidx.core.graphics.withClip
import androidx.core.graphics.withSave
import androidx.core.graphics.withTranslation
import com.angcyo.canvas.CanvasDelegate
import com.angcyo.canvas.core.ICanvasView
import com.angcyo.canvas.core.component.AxisPoint
import com.angcyo.canvas.core.component.BaseAxis
import com.angcyo.canvas.core.component.XAxis
import com.angcyo.library.ex.have

/**
 * [XAxis]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/01
 */
class XAxisRenderer(val axis: XAxis, canvasView: ICanvasView) :
    BaseAxisRenderer(canvasView) {

    override fun onCanvasSizeChanged(canvasView: CanvasDelegate) {
        super.onCanvasSizeChanged(canvasView)
        _renderBounds.set(
            0f,
            0f,
            canvasView.viewBounds.width(),
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
                plusList.forEachIndexed { index, axisPoint ->
                    val left = axisPoint.pixel * scaleX
                    drawLineAndLabel(canvas, axisPoint.index, axisPoint, left, bottom)
                }

                minusList.forEachIndexed { index, axisPoint ->
                    val left = axisPoint.pixel * scaleX
                    drawLineAndLabel(canvas, -axisPoint.index, axisPoint, left, bottom)
                }
            }
        }

        //网格线的绘制
        canvas.withSave {
            clipRect(canvasViewBox.contentRect)

            canvas.withTranslation(x = translateX) {
                minusList.forEachIndexed { index, axisPoint ->
                    val left = axisPoint.pixel * scaleX
                    drawGridLine(canvas, left, contentTop, contentBottom, axisPoint.type)
                }

                plusList.forEachIndexed { index, axisPoint ->
                    val left = axisPoint.pixel * scaleX
                    drawGridLine(canvas, left, contentTop, contentBottom, axisPoint.type)
                }
            }
        }
    }

    fun drawLineAndLabel(
        canvas: Canvas,
        index: Int,
        point: AxisPoint,
        left: Float,
        bottom: Float
    ) {
        val valueStr = canvasViewBox.valueUnit.getGraduatedLabel(index, point.gap)
        val axisLineType = point.type
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
        left: Float,
        top: Float,
        bottom: Float,
        axisLineType: Int
    ) {
        if (!axis.drawGridLine) {
            //不绘制所有线
            return
        }

        //绘制网格
        if (axisLineType.have(BaseAxis.LINE_TYPE_DRAW_GRID)) {
            if (axisLineType.have(BaseAxis.LINE_TYPE_PROTRUDE)) {
                canvas.drawLine(left, top, left, bottom, gridProtrudePaint)
            } else if (axisLineType.have(BaseAxis.LINE_TYPE_SECONDARY)) {
                canvas.drawLine(left, top, left, bottom, gridPaint)
            } else if (axis.drawGridLine && axisLineType.have(BaseAxis.LINE_TYPE_NORMAL)) {
                //普通网格线
                canvas.drawLine(left, top, left, bottom, gridPaint)
            }
        }
    }
}