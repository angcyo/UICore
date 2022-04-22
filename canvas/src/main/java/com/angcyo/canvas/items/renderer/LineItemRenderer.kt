package com.angcyo.canvas.items.renderer

import android.graphics.*
import android.widget.LinearLayout
import com.angcyo.canvas.CanvasView
import com.angcyo.canvas.core.CanvasViewBox
import com.angcyo.canvas.core.component.ControlPoint
import com.angcyo.canvas.items.LineItem
import com.angcyo.library.ex.*

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/22
 */
class LineItemRenderer(canvasViewBox: CanvasViewBox) : BaseItemRenderer<LineItem>(canvasViewBox) {

    val lineBounds = RectF()

    override fun onUpdateRendererItem(item: LineItem?, oldItem: LineItem?) {
        super.onUpdateRendererItem(item, oldItem)
        item?.let {
            changeBounds {
                val width: Float
                val height: Float
                if (it.orientation == LinearLayout.VERTICAL) {
                    width = 1f
                    height = it.length
                } else {
                    width = it.length
                    height = 1f
                }
                lineBounds.set(0f, 0f, width, height)
                adjustSize(width, height, ADJUST_TYPE_LT)
            }
        }
    }

    /**限制线的宽或高*/
    override fun onItemBoundsChanged() {
        if (lineBounds.width() <= 1f) {
            getBounds().apply {
                adjustSizeWithRotate(lineBounds.width(), height(), rotate, ADJUST_TYPE_LT)
            }
        } else if (lineBounds.height() <= 1f) {
            getBounds().apply {
                adjustSizeWithRotate(width(), lineBounds.height(), rotate, ADJUST_TYPE_LT)
            }
        }
        super.onItemBoundsChanged()
    }

    override fun render(canvas: Canvas) {
        rendererItem?.apply {
            val renderBounds = getRendererBounds()
            if (orientation == LinearLayout.VERTICAL) {
                canvas.drawLine(
                    renderBounds.left,
                    renderBounds.top,
                    renderBounds.left,
                    renderBounds.bottom,
                    paint
                )
            } else {
                canvas.drawLine(
                    renderBounds.left,
                    renderBounds.top,
                    renderBounds.right,
                    renderBounds.top,
                    paint
                )
            }
        }
    }

    override fun isSupportControlPoint(type: Int): Boolean {
        if (type == ControlPoint.POINT_TYPE_LOCK) {
            return false
        }
        return super.isSupportControlPoint(type)
    }

    val tempRect = RectF()

    override fun containsPoint(point: PointF): Boolean {
        val rendererBounds = getRendererBounds()
        tempRect.set(rendererBounds)
        tempRect.inset(-5f, -5f)
        return getRotateMatrix(tempRect.centerX(), tempRect.centerY()).run {
            rotatePath.reset()
            rotatePath.addRect(tempRect, Path.Direction.CW)
            rotatePath.transform(this)
            rotatePath.contains(point.x.toInt(), point.y.toInt())
        }
    }
}

/**添加一根横线
 * [length] 线的长度
 * [orientation] 线的方向
 * [dash] 是否是虚线*/
fun CanvasView.addLineRenderer(
    length: Float = 100f,
    orientation: Int = LinearLayout.VERTICAL,
    dash: Boolean = false
) {
    val renderer = LineItemRenderer(canvasViewBox)
    renderer.rendererItem = LineItem().apply {
        this.length = length
        this.orientation = orientation
        this.dash = dash
        if (dash) {
            this.paint.style = Paint.Style.STROKE
            //因为是用矩形的方式绘制的线, 所以虚线的间隔和长度必须一致
            this.paint.pathEffect = DashPathEffect(floatArrayOf(4 * density, 5 * density), 0f)
        }
    }
    addCentreItemRenderer(renderer)
    selectedItem(renderer)
}