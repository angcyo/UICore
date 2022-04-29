package com.angcyo.canvas.items.renderer

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.RectF
import androidx.core.graphics.withMatrix
import com.angcyo.canvas.ScalePictureDrawable
import com.angcyo.canvas.core.CanvasViewBox
import com.angcyo.canvas.items.DrawableItem
import com.angcyo.library.ex.*

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/11
 */
open class DrawableItemRenderer<T : DrawableItem>(canvasViewBox: CanvasViewBox) :
    BaseItemRenderer<T>(canvasViewBox) {

    override fun onUpdateRendererItem(item: T?, oldItem: T?) {
        super.onUpdateRendererItem(item, oldItem)
        if (item != oldItem) {
            val bounds = getBounds()
            if (bounds.isEmpty) {
                initBounds()
            }
        }
    }

    open fun initBounds() {
        changeBounds {
            adjustSize(
                rendererItem?.drawable?.minimumWidth?.toFloat() ?: 0f,
                rendererItem?.drawable?.minimumHeight?.toFloat() ?: 0f,
                ADJUST_TYPE_LT
            )
        }
    }

    val flipMatrix = Matrix()
    val flipRect = RectF()

    override fun render(canvas: Canvas) {
        rendererItem?.drawable?.let { drawable ->
            val bounds = getRenderBounds()
            //需要处理矩形翻转的情况
            if (drawable is ScalePictureDrawable) {
                drawable.setBounds(
                    bounds.left.toInt(),
                    bounds.top.toInt(),
                    bounds.right.toInt(),
                    bounds.bottom.toInt()
                )
                drawable.draw(canvas)
            } else {
                bounds.adjustFlipRect(flipRect)
                var sx = 1f
                var sy = 1f
                if (getBounds().isFlipHorizontal) {
                    sx = -1f
                }
                if (getBounds().isFlipVertical) {
                    sy = -1f
                }
                flipMatrix.reset()
                flipMatrix.postScale(sx, sy, flipRect.centerX(), flipRect.centerY())
                canvas.withMatrix(flipMatrix) {
                    drawable.setBounds(
                        flipRect.left.toInt(),
                        flipRect.top.toInt(),
                        flipRect.right.toInt(),
                        flipRect.bottom.toInt()
                    )
                    drawable.draw(canvas)
                }
            }
        }
    }
}