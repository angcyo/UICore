package com.angcyo.canvas.render.core

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.graphics.withRotation
import androidx.core.graphics.withSave
import com.angcyo.canvas.render.R
import com.angcyo.canvas.render.annotation.CanvasInsideCoordinate
import com.angcyo.canvas.render.data.AxisPoint
import com.angcyo.canvas.render.data.RenderParams
import com.angcyo.canvas.render.util.createRenderPaint
import com.angcyo.library.annotation.Pixel
import com.angcyo.library.ex._color
import com.angcyo.library.ex.alpha
import com.angcyo.library.ex.dp
import com.angcyo.library.ex.have
import com.angcyo.library.ex.remove
import com.angcyo.library.ex.textDrawHeight
import com.angcyo.library.ex.textHeight
import com.angcyo.library.ex.textWidth
import com.angcyo.library.ex.toColor
import com.angcyo.library.unit.IRenderUnit
import com.angcyo.library.unit.MmRenderUnit

/**
 * 坐标尺/网格 绘制和管理
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/14
 */
class CanvasAxisManager(val delegate: CanvasRenderDelegate) : IRenderer {

    /**渲染刻度尺的单位*/
    var renderUnit: IRenderUnit = MmRenderUnit()

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

    /**元素大小提示画笔
     * [com.angcyo.canvas.render.core.component.CanvasMoveSelectorComponent.fillColor]*/
    val selectElementSizePaint = createRenderPaint().apply {
        style = Paint.Style.FILL
        color = _color(R.color.canvas_render_select).alpha(120)
    }

    /**是否要绘制网格线*/
    var enableRenderGrid: Boolean = true

    /**是否要绘制刻度尺的横竖线*/
    var enableRenderBounds: Boolean = true

    /**是否要绘制选中元素的坐标提示*/
    var enableRenderSelectElementSize: Boolean = true

    override var renderFlags: Int = 0xf

    private val tempRectF = RectF()

    init {
        renderFlags = renderFlags.remove(IRenderer.RENDERER_FLAG_ON_INSIDE)
            .remove(IRenderer.RENDERER_FLAG_ON_OUTSIDE)
        delegate.addCanvasRenderListener(object : BaseCanvasRenderListener() {
            override fun onRenderBoxBoundsUpdate(newBounds: RectF) {
                updateAxisList()
            }

            override fun onRenderBoxOriginGravityUpdate(newGravity: Int) {
                updateAxisList()
            }

            override fun onRenderBoxMatrixUpdate(
                newMatrix: Matrix,
                reason: Reason,
                finish: Boolean
            ) {
                updateAxisList()
            }
        })
    }

    override fun renderOnView(canvas: Canvas, params: RenderParams) {
        if (xAxisList.isEmpty() || yAxisList.isEmpty()) {
            updateAxisList()
        }
        canvas.withSave {
            clipRect(xAxisBounds)
            if (enableRenderSelectElementSize) {
                renderXAxisSelectElement(canvas)
            }
            renderXAxis(canvas)
        }
        canvas.withSave {
            clipRect(yAxisBounds)
            if (enableRenderSelectElementSize) {
                renderYAxisSelectElement(canvas)
            }
            renderYAxis(canvas)
        }
        if (enableRenderGrid) {
            renderGrid(canvas)
        }
        if (enableRenderBounds) {
            renderAxisBounds(canvas)
        }

        //renderOriginPoint(canvas)
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

    fun renderXAxisSelectElement(canvas: Canvas) {
        if (delegate.selectorManager.isSelectorElement) {
            val bounds = delegate.selectorManager.selectorComponent.getRendererBounds() ?: return
            renderXAxisRect(canvas, bounds.left, bounds.right)
        }
    }

    fun renderYAxisSelectElement(canvas: Canvas) {
        if (delegate.selectorManager.isSelectorElement) {
            val bounds = delegate.selectorManager.selectorComponent.getRendererBounds() ?: return
            renderYAxisRect(canvas, bounds.top, bounds.bottom)
        }
    }

    /**在x轴上绘制一块区域
     * [fromX] 坐标系中的坐标*/
    fun renderXAxisRect(
        canvas: Canvas,
        @CanvasInsideCoordinate @Pixel fromX: Float,
        @CanvasInsideCoordinate @Pixel toX: Float
    ) {
        val fx = getXPixelInView(fromX)
        val tx = getXPixelInView(toX)
        tempRectF.set(fx, xAxisBounds.top, tx, xAxisBounds.bottom)
        canvas.drawRect(tempRectF, selectElementSizePaint)
    }

    /**在y轴上绘制一块区域*/
    fun renderYAxisRect(
        canvas: Canvas,
        @CanvasInsideCoordinate @Pixel fromY: Float,
        @CanvasInsideCoordinate @Pixel toY: Float
    ) {
        val fy = getYPixelInView(fromY)
        val ty = getYPixelInView(toY)
        tempRectF.set(yAxisBounds.left, fy, yAxisBounds.right, ty)
        canvas.drawRect(tempRectF, selectElementSizePaint)
    }

    /**渲染出坐标系的中点*/
    fun renderOriginPoint(canvas: Canvas) {
        val renderViewBox = delegate.renderViewBox
        val originPoint = renderViewBox.getOriginPointOutside()
        renderViewBox.offsetToView(originPoint)

        selectElementSizePaint.style = Paint.Style.FILL_AND_STROKE
        selectElementSizePaint.strokeWidth = 1 * dp
        canvas.drawLine(
            originPoint.x,
            0f,
            originPoint.x,
            delegate.view.measuredHeight.toFloat(),
            selectElementSizePaint
        )
        canvas.drawLine(
            0f,
            originPoint.y,
            delegate.view.measuredWidth.toFloat(),
            originPoint.y,
            selectElementSizePaint
        )
    }
    //endregion---绘制---

    //region---计算---

    /**更新刻度尺信息*/
    fun updateAxisList() {
        updateXAxisList()
        updateYAxisList()
    }

    /**获取当前像素坐标[x] 对应在x轴上的位置*/
    fun getXPixelInView(@CanvasInsideCoordinate @Pixel x: Float): Float {
        val renderViewBox = delegate.renderViewBox
        val scale = renderViewBox.getScaleX()

        val originPoint = renderViewBox.getOriginPointOutside()
        renderViewBox.offsetToView(originPoint)

        return originPoint.x + x * scale
    }

    /**获取当前像素坐标[y] 对应在y轴上的位置*/
    fun getYPixelInView(@CanvasInsideCoordinate @Pixel y: Float): Float {
        val renderViewBox = delegate.renderViewBox
        val scale = renderViewBox.getScaleY()

        val originPoint = renderViewBox.getOriginPointOutside()
        renderViewBox.offsetToView(originPoint)

        return originPoint.y + y * scale
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

    //region---操作---

    /**更新渲染的刻度尺单位
     * [IRenderUnit]*/
    fun updateRenderUnit(unit: IRenderUnit) {
        if (renderUnit == unit) {
            return
        }
        val old = renderUnit
        renderUnit = unit
        updateAxisList()
        delegate.dispatchRenderUnitChange(old, unit)
        delegate.refresh()
    }

    //endregion---操作---

}