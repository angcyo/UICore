package com.angcyo.canvas.items.renderer

import android.graphics.Canvas
import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import android.widget.LinearLayout
import com.angcyo.canvas.Reason
import com.angcyo.canvas.core.ICanvasView
import com.angcyo.canvas.core.component.ControlPoint
import com.angcyo.canvas.items.LineItem
import com.angcyo.library.ex.ADJUST_TYPE_LT
import com.angcyo.library.ex.adjustSize
import com.angcyo.library.ex.adjustSizeWithRotate
import com.angcyo.library.ex.contains

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/22
 */

@Deprecated("不支持镜像绘制")
class LineItemRenderer(canvasView: ICanvasView) : BaseItemRenderer<LineItem>(canvasView) {

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
    override fun itemBoundsChanged(reason: Reason, oldBounds: RectF) {
        if (lineBounds.width() <= 1f) {
            getBounds().apply {
                adjustSizeWithRotate(lineBounds.width(), height(), rotate, ADJUST_TYPE_LT)
            }
        } else if (lineBounds.height() <= 1f) {
            getBounds().apply {
                adjustSizeWithRotate(width(), lineBounds.height(), rotate, ADJUST_TYPE_LT)
            }
        }
        super.itemBoundsChanged(reason, oldBounds)
    }

    override fun render(canvas: Canvas) {
        _rendererItem?.apply {
            val renderBounds = getRenderBounds()
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
        val rendererBounds = getRenderBounds()
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