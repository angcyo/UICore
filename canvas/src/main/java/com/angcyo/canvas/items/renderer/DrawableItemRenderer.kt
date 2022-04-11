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

    override fun onUpdateRendererBounds(canvasView: CanvasView) {
        super.onUpdateRendererBounds(canvasView)
        if (bounds.isEmpty) {
            bounds.set(
                0f,
                0f,
                rendererItem?.drawable?.intrinsicWidth?.toFloat() ?: 0f,
                rendererItem?.drawable?.intrinsicHeight?.toFloat() ?: 0f
            )
        }
    }

    override fun render(canvas: Canvas) {
        rendererItem?.drawable?.let { drawable ->
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