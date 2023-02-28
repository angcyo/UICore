package com.angcyo.canvas.render.core

import android.graphics.*
import androidx.core.graphics.withRotation
import androidx.core.graphics.withSave
import com.angcyo.canvas.render.core.component.CanvasRenderProperty
import com.angcyo.canvas.render.data.AxisPoint
import com.angcyo.canvas.render.renderer.BaseRenderer
import com.angcyo.canvas.render.unit.IRenderUnit
import com.angcyo.canvas.render.unit.PxRenderUnit
import com.angcyo.canvas.render.util.createRenderPaint
import com.angcyo.library.annotation.Pixel
import com.angcyo.library.ex.*

/**
 * 坐标尺/网格 绘制和管理
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/14
 */
class CanvasAxisManager(val delegate: CanvasRenderDelegate) : IRenderer {

    /**渲染刻度尺的单位*/
    var renderUnit: IRenderUnit = PxRenderUnit()

    /**X轴刻度在View中的绘制坐标*/
    @Pixel
    var xAxisBounds = RectF(0f, 0f, 0f, 0f)

    /**Y轴刻度在View中的绘制坐标*/
    @Pixel
    var yAxisBounds = RectF(0f, 0f, 0f, 0f)

    /**横纵坐标刻度尺信息*/
    val xAxisList = mutableListOf<AxisPoint>()

    val yAxisList = mutableListOf<AxisPoint>()

    /**主要刻度线的画笔*/
    val axisLinePrimaryPaint = createRenderPaint(Color.GRAY)

    /**次要刻度线的画笔*/
    val axisLineSecondaryPaint = createRenderPaint("#d0d0d0".toColor())

    /**普通刻度线的画笔*/
    val axisLineNormalPaint = createRenderPaint("#d0d0d0".toColor())

    /**label的文本大小*/
    var labelTextSize = 9 * dp

    /**label的偏移量*/
    var labelOffset = 3 * dp

    /**刻度文本画笔*/
    val axisLabelPaint = createRenderPaint(Color.GRAY).apply {
        style = Paint.Style.FILL
    }

    /**是否要绘制网格线*/
    var enableRenderGrid: Boolean = true

    /**是否要绘制刻度尺的横竖线*/
    var enableRenderBounds: Boolean = true

    init {
        delegate.renderListenerList.add(object : ICanvasRenderListener {
            override fun onRenderBoxBoundsUpdate(newBounds: RectF) {
                super.onRenderBoxBoundsUpdate(newBounds)
                updateAxisList()
            }

            override fun onRenderBoxOriginGravityUpdate(newGravity: Int) {
                super.onRenderBoxOriginGravityUpdate(newGravity)
                updateAxisList()
            }

            override fun onRenderBoxMatrixUpdate(newMatrix: Matrix, finish: Boolean) {
                super.onRenderBoxMatrixUpdate(newMatrix, finish)
                updateAxisList()
            }
        })
    }

    override fun render(canvas: Canvas) {
        if (xAxisList.isEmpty() || yAxisList.isEmpty()) {
            updateAxisList()
        }
        canvas.withSave {
            clipRect(xAxisBounds)
            renderXAxis(canvas)
        }
        canvas.withSave {
            clipRect(yAxisBounds)
            renderYAxis(canvas)
        }
        if (enableRenderGrid) {
            renderGrid(canvas)
        }
        if (enableRenderBounds) {
            renderAxisBounds(canvas)
        }
    }

    /**更新刻度尺的绘制范围
     * [size] 刻度尺的宽度或者高度
     * [l] 绘制的左边记录
     * [t] 绘制的上边距离
     * [w] 最大宽度, 用来计算绘制的右边距离
     * [h] 最大高度, 用来计算绘制的下边距离
     * */
    fun updateAxisBounds(@Pixel size: Float, l: Int, t: Int, w: Int, h: Int) {
        xAxisBounds.set(l + size, t.toFloat(), w.toFloat(), t + size)
        yAxisBounds.set(l.toFloat(), t + size, l + size, h.toFloat())
        updateAxisList()
    }

    //region---绘制---

    /**绘制x轴刻度尺*/
    fun renderXAxis(canvas: Canvas, rect: RectF = xAxisBounds) {
        for (point in xAxisList) {
            val x = point.pixel
            val height = when (point.typeMask) {
                IRenderUnit.AXIS_TYPE_PRIMARY -> rect.height()
                IRenderUnit.AXIS_TYPE_SECONDARY -> rect.height() / 2
                else -> rect.height() / 4
            }
            val y = rect.bottom - height
            val paint = when (point.typeMask) {
                IRenderUnit.AXIS_TYPE_PRIMARY -> axisLinePrimaryPaint
                IRenderUnit.AXIS_TYPE_SECONDARY -> axisLineSecondaryPaint
                else -> axisLineNormalPaint
            }
            canvas.drawLine(x, rect.bottom, x, y, paint)

            if (point.type.have(IRenderUnit.AXIS_TYPE_LABEL)) {
                val label =
                    renderUnit.formatValue(renderUnit.convertPixelToValue(point.value), true, false)
                axisLabelPaint.textSize = labelTextSize
                val labelY = rect.top + axisLabelPaint.textDrawHeight()
                canvas.drawText(label, x + labelOffset, labelY, axisLabelPaint)
            }
        }
    }

    /**绘制y轴刻度尺*/
    fun renderYAxis(canvas: Canvas, rect: RectF = yAxisBounds) {
        for (point in yAxisList) {
            val y = point.pixel
            val width = when (point.typeMask) {
                IRenderUnit.AXIS_TYPE_PRIMARY -> rect.width()
                IRenderUnit.AXIS_TYPE_SECONDARY -> rect.width() / 2
                else -> rect.width() / 4
            }
            val x = rect.right - width
            val paint = when (point.typeMask) {
                IRenderUnit.AXIS_TYPE_PRIMARY -> axisLinePrimaryPaint
                IRenderUnit.AXIS_TYPE_SECONDARY -> axisLineSecondaryPaint
                else -> axisLineNormalPaint
            }
            canvas.drawLine(x, y, rect.right, y, paint)

            if (point.type.have(IRenderUnit.AXIS_TYPE_LABEL)) {
                val label =
                    renderUnit.formatValue(renderUnit.convertPixelToValue(point.value), true, false)
                axisLabelPaint.textSize = labelTextSize

                val textHeight = axisLabelPaint.textHeight()
                val textDrawHeight = axisLabelPaint.textDrawHeight()
                val textWidth = axisLabelPaint.textWidth(label)

                val labelX = rect.left
                val labelY = y - labelOffset

                canvas.withRotation(-90f, labelX, labelY) {
                    translate(0f, textDrawHeight)
                    canvas.drawText(label, labelX, labelY, axisLabelPaint)
                }
            }
        }
    }

    /**绘制网格线*/
    fun renderGrid(canvas: Canvas) {
        val renderBounds = delegate.renderViewBox.renderBounds

        val left = renderBounds.left
        val right = renderBounds.right
        val top = renderBounds.top
        val bottom = renderBounds.bottom

        //绘制竖向网格
        for (point in xAxisList) {
            val x = point.pixel

            if (x in left..right) {
                val paint = when (point.typeMask) {
                    IRenderUnit.AXIS_TYPE_PRIMARY -> axisLinePrimaryPaint
                    IRenderUnit.AXIS_TYPE_SECONDARY -> axisLineSecondaryPaint
                    else -> axisLineNormalPaint
                }
                canvas.drawLine(x, top, x, bottom, paint)
            }
        }

        //绘制横向网格
        for (point in yAxisList) {
            val y = point.pixel

            if (y in top..bottom) {
                val paint = when (point.typeMask) {
                    IRenderUnit.AXIS_TYPE_PRIMARY -> axisLinePrimaryPaint
                    IRenderUnit.AXIS_TYPE_SECONDARY -> axisLineSecondaryPaint
                    else -> axisLineNormalPaint
                }
                canvas.drawLine(left, y, right, y, paint)
            }
        }
    }

    /**绘制刻度尺的Bounds*/
    fun renderAxisBounds(canvas: Canvas) {
        canvas.drawLine(
            0f,
            xAxisBounds.bottom,
            delegate.view.measuredWidth.toFloat(),
            xAxisBounds.bottom,
            axisLinePrimaryPaint
        )

        canvas.drawLine(
            yAxisBounds.right,
            0f,
            yAxisBounds.right,
            delegate.view.measuredHeight.toFloat(),
            axisLinePrimaryPaint
        )
    }

    //endregion---绘制---

    //region---计算---

    /**更新刻度尺信息*/
    fun updateAxisList() {
        updateXAxisList()
        updateYAxisList()
    }

    private fun updateXAxisList() {
        xAxisList.clear()

        val renderViewBox = delegate.renderViewBox
        val scale = renderViewBox.getScaleX()

        val originPoint = renderViewBox.getOriginPointOutside()
        renderViewBox.offsetToView(originPoint)

        //先计算正向刻度
        var distance = xAxisBounds.right - originPoint.x
        var pixel = originPoint.x
        var index = 0
        while (distance > 0) {
            //距离右边还有空间
            val gap = renderUnit.getGap(scale)
            val gapPixel = gap * scale
            val type = renderUnit.getRenderType(index, scale)
            xAxisList.add(AxisPoint(pixel, gap * index, index, type))
            distance -= gapPixel
            pixel += gapPixel
            index++
        }

        //再计算负向刻度
        distance = originPoint.x - 0 //xAxisBounds.left //左边多释放一点控件, 可以将label绘制出来
        pixel = originPoint.x
        index = 0
        while (distance > 0) {
            //距离左边还有空间
            val gap = renderUnit.getGap(scale)
            val gapPixel = gap * scale
            val type = renderUnit.getRenderType(index, scale)
            xAxisList.add(AxisPoint(pixel, gap * index, index, type))
            distance -= gapPixel
            pixel -= gapPixel
            index--
        }
    }

    private fun updateYAxisList() {
        yAxisList.clear()

        val renderViewBox = delegate.renderViewBox
        val scale = renderViewBox.getScaleY()

        val originPoint = renderViewBox.getOriginPointOutside()
        renderViewBox.offsetToView(originPoint)

        //先计算正向刻度
        var distance = yAxisBounds.bottom - originPoint.y
        var pixel = originPoint.y
        var index = 0
        while (distance > 0) {
            //距离右边还有空间
            val gap = renderUnit.getGap(scale)
            val gapPixel = gap * scale
            val type = renderUnit.getRenderType(index, scale)
            yAxisList.add(AxisPoint(pixel, gap * index, index, type))
            distance -= gapPixel
            pixel += gapPixel
            index++
        }

        //再计算负向刻度
        distance = originPoint.y - yAxisBounds.top
        pixel = originPoint.y
        index = 0
        while (distance > 0) {
            //距离左边还有空间
            val gap = renderUnit.getGap(scale)
            val gapPixel = gap * scale
            val type = renderUnit.getRenderType(index, scale)
            yAxisList.add(AxisPoint(pixel, gap * index, index, type))
            distance -= gapPixel
            pixel -= gapPixel
            index--
        }
    }

    //endregion---计算---
}