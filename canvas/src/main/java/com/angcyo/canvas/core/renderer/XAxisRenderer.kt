package com.angcyo.canvas.core.renderer

import android.graphics.Canvas
import androidx.core.graphics.withSave
import androidx.core.graphics.withTranslation
import com.angcyo.canvas.CanvasView
import com.angcyo.canvas.core.CanvasViewBox
import com.angcyo.canvas.core.Transformer
import com.angcyo.canvas.core.component.BaseAxis
import com.angcyo.canvas.core.component.XAxis
import com.angcyo.canvas.utils.getScaleX
import com.angcyo.canvas.utils.getTranslateX

/**
 * [XAxis]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/01
 */
class XAxisRenderer(val axis: XAxis, canvasViewBox: CanvasViewBox, transformer: Transformer) :
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
            canvasView.measuredWidth.toFloat(),
            axis.axisSize
        )
    }

    override fun render(canvas: Canvas) {
        val bounds = getRenderBounds()
        val bottom = bounds.bottom
        canvas.drawLine(bounds.left, bottom, bounds.right, bottom, linePaint)

        val translateX = canvasViewBox.matrix.getTranslateX()
        val scaleX = canvasViewBox.matrix.getScaleX()

        //默认, 每隔1mm绘制一个刻度
        val step = canvasViewBox.convertValueToPixel(scaleX)

        minusList.clear()
        plusList.clear()

        val contentLeft = canvasViewBox.getContentLeft()
        val contentRight = canvasViewBox.getContentRight()
        val contentTop = canvasViewBox.getContentTop()
        val contentBottom = canvasViewBox.getContentBottom()

        //只需要绘制这个x坐标范围内的点
        val drawMinX = contentLeft - translateX
        val drawMaxX = contentRight - translateX

        //刻度的绘制
        canvas.withTranslation(x = translateX) {

            //先/后 clip, 都有效果
            val clipRight = drawMaxX //bounds.right - translateX
            val clipLeft = clipRight - bounds.width() + contentLeft
            clipRect(clipLeft, bounds.top, clipRight, bottom)

            //从0坐标开始, 先绘制负坐标
            var startLeft = contentLeft
            while (startLeft > drawMinX) {
                minusList.add(startLeft)
                startLeft -= step //负向延伸
            }

            startLeft = contentLeft
            while (startLeft < drawMaxX) {
                plusList.add(startLeft)
                startLeft += step //正向延伸
            }

            minusList.forEachIndexed { index, left ->
                drawLineAndLabel(canvas, index, left, bottom, contentLeft, scaleX)
            }

            plusList.forEachIndexed { index, left ->
                drawLineAndLabel(canvas, index, left, bottom, contentLeft, scaleX)
            }
        }

        //网格线的绘制
        canvas.withSave {
            clipRect(canvasViewBox._contentRect)

            canvas.withTranslation(x = translateX) {
                minusList.forEachIndexed { index, left ->
                    drawGridLine(canvas, index, left, contentTop, contentBottom, scaleX)
                }

                plusList.forEachIndexed { index, left ->
                    drawGridLine(canvas, index, left, contentTop, contentBottom, scaleX)
                }
            }
        }
    }

    fun drawLineAndLabel(
        canvas: Canvas,
        index: Int,
        left: Float,
        bottom: Float,
        originLeft: Float,
        scale: Float
    ) {
        //相对于原点的像素距离点数值
        val distance = (left - originLeft) / scale
        //绘制刻度文本
        val value = canvasViewBox.convertPixelToValue(distance)
        val valueStr = canvasViewBox.formattedValue(value)

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