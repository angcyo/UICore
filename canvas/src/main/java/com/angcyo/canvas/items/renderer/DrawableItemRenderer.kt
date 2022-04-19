package com.angcyo.canvas.items.renderer

import android.graphics.Canvas
import com.angcyo.canvas.CanvasView
import com.angcyo.canvas.core.CanvasViewBox
import com.angcyo.canvas.items.DrawableItem

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/11
 */
class DrawableItemRenderer(canvasViewBox: CanvasViewBox) :
    BaseItemRenderer<DrawableItem>(canvasViewBox) {

    override fun updateRendererItem(item: DrawableItem?, oldItem: DrawableItem?) {
        super.updateRendererItem(item, oldItem)
        if (item != oldItem) {
            val bounds = getBounds()
            if (bounds.isEmpty) {
                changeBounds {
                    set(
                        0f,
                        0f,
                        rendererItem?.drawable?.minimumWidth?.toFloat() ?: 0f,
                        rendererItem?.drawable?.minimumHeight?.toFloat() ?: 0f
                    )
                }
            }
        }
    }

    override fun render(canvas: Canvas) {
        rendererItem?.drawable?.let { drawable ->
            val bounds = getRendererBounds()
            drawable.setBounds(
                bounds.left.toInt(),
                bounds.top.toInt(),
                bounds.right.toInt(),
                bounds.bottom.toInt()
            )
            drawable.draw(canvas)
        }
    }
}