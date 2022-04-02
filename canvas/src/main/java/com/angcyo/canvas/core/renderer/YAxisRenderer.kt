package com.angcyo.canvas.core.renderer

import android.graphics.Canvas
import androidx.core.graphics.withSave
import androidx.core.graphics.withTranslation
import com.angcyo.canvas.CanvasView
import com.angcyo.canvas.core.CanvasViewBox
import com.angcyo.canvas.core.Transformer
import com.angcyo.canvas.core.component.BaseAxis
import com.angcyo.canvas.core.component.YAxis
import com.angcyo.canvas.utils.getScaleY
import com.angcyo.canvas.utils.getTranslateY
import com.angcyo.drawable.textHeight

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/01
 */
class YAxisRenderer(val axis: YAxis, canvasViewBox: CanvasViewBox, transformer: Transformer) :
    BaseAxisRenderer(canvasViewBox, transformer) {

    /**负向的刻度点坐标*/
    val minusList = mutableListOf<Float>()

    /**正向的刻度点坐标*/
    val plusList = mutableListOf<Float>()

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

        val translateY = canvasViewBox.matrix.getTranslateY()
        val scaleY = canvasViewBox.matrix.getScaleY()

        //默认, 每隔1mm绘制一个刻度
        val step = canvasViewBox.convertValueToPixel(scaleY)

        minusList.clear()
        plusList.clear()

        val contentLeft = canvasViewBox.getContentLeft()
        val contentRight = canvasViewBox.getContentRight()
        val contentTop = canvasViewBox.getContentTop()
        val contentBottom = canvasViewBox.getContentBottom()

        //只需要绘制这个x坐标范围内的点
        val drawMinY = contentTop - translateY
        val drawMaxY = contentBottom - translateY

        canvas.withTranslation(y = translateY) {

            //先/后 clip, 都有效果
            val clipBottom = drawMaxY
            val clipTop = clipBottom - bounds.height() + contentTop
            clipRect(bounds.left, clipTop, bounds.right, clipBottom)

            //从0坐标开始, 先绘制负坐标
            var startTop = contentTop
            while (startTop > drawMinY) {
                minusList.add(startTop)
                startTop -= step //负向延伸
            }

            startTop = contentTop
            while (startTop < drawMaxY) {
                plusList.add(startTop)
                startTop += step //正向延伸
            }

            minusList.forEachIndexed { index, top ->
                drawLineAndLabel(canvas, index, top, right, contentTop, scaleY)
            }

            plusList.forEachIndexed { index, top ->
                drawLineAndLabel(canvas, index, top, right, contentTop, scaleY)
            }
        }

        //网格线的绘制
        canvas.withSave {
            clipRect(canvasViewBox._contentRect)
            canvas.withTranslation(y = translateY) {

                minusList.forEachIndexed { index, top ->
                    drawGridLine(canvas, index, contentLeft, top, contentRight, scaleY)
                }

                plusList.forEachIndexed { index, top ->
                    drawGridLine(canvas, index, contentLeft, top, contentRight, scaleY)
                }
            }
        }
    }

    fun drawLineAndLabel(
        canvas: Canvas,
        index: Int,
        top: Float,
        right: Float,
        originTop: Float,
        scale: Float
    ) {
        //相对于原点的像素距离点数值
        val distance = (top - originTop) / scale
        //绘制刻度文本
        val value = canvasViewBox.convertPixelToValue(distance)
        val valueStr = canvasViewBox.formattedValue(value)

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