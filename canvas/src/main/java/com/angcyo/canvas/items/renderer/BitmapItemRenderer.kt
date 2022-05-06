package com.angcyo.canvas.items.renderer

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.RectF
import androidx.core.graphics.withMatrix
import com.angcyo.canvas.Strategy
import com.angcyo.canvas.core.CanvasViewBox
import com.angcyo.canvas.core.renderer.ICanvasStep
import com.angcyo.canvas.items.BitmapItem
import com.angcyo.canvas.utils.limitMaxWidthHeight
import com.angcyo.library.ex.*

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/22
 */
class BitmapItemRenderer(canvasViewBox: CanvasViewBox) :
    BaseItemRenderer<BitmapItem>(canvasViewBox) {

    val bitmapMatrix = Matrix()

    val tempRect = RectF()

    override fun onUpdateRendererItem(item: BitmapItem?, oldItem: BitmapItem?) {
        super.onUpdateRendererItem(item, oldItem)
        changeBounds {
            adjustSize(
                item?.bitmap?.width?.toFloat() ?: 0f,
                item?.bitmap?.height?.toFloat() ?: 0f,
                ADJUST_TYPE_LT
            )
        }
    }

    override fun render(canvas: Canvas) {
        rendererItem?.bitmap?.let { bitmap ->
            bitmapMatrix.reset()
            val bounds = getRenderBounds()

            bounds.adjustFlipRect(tempRect)

            var sx = 1f
            var sy = 1f
            if (getBounds().isFlipHorizontal) {
                sx = -1f
            }
            if (getBounds().isFlipVertical) {
                sy = -1f
            }
            bitmapMatrix.postScale(sx, sy, tempRect.centerX(), tempRect.centerY())
            canvas.withMatrix(bitmapMatrix) {
                canvas.drawBitmap(bitmap, null, tempRect, null)
            }
        }
    }

    /**更新需要绘制的图片, 并保持原先的缩放比例*/
    fun updateBitmap(
        bitmap: Bitmap,
        strategy: Strategy = Strategy(Strategy.STRATEGY_TYPE_NORMAL)
    ): BitmapItem {
        val oldValue = rendererItem?.bitmap
        if (oldValue == bitmap) {
            return rendererItem!!
        }

        val oldBounds = RectF(getBounds())
        val maxWidth = oldBounds.width()
        val maxHeight = oldBounds.height()

        rendererItem = BitmapItem().apply { this.bitmap = bitmap }

        val newWidth = rendererItem?.bitmap?.width ?: 0
        val newHeight = rendererItem?.bitmap?.height ?: 0

        if (maxWidth > 0 && maxHeight > 0) {
            limitMaxWidthHeight(
                newWidth.toFloat(),
                newHeight.toFloat(),
                maxWidth,
                maxHeight
            ).apply {
                updateBounds(this[0], this[1])
            }
        } else {
            updateBounds(newWidth.toFloat(), newHeight.toFloat())
        }
        refresh()

        val newBounds = RectF(getBounds())
        if (strategy.type == Strategy.STRATEGY_TYPE_NORMAL && oldValue != null) {
            canvasViewBox.canvasView.getCanvasUndoManager().addUndoAction(object : ICanvasStep {
                override fun runUndo() {
                    rendererItem?.bitmap = oldValue
                    changeBounds {
                        set(oldBounds)
                    }
                }

                override fun runRedo() {
                    rendererItem?.bitmap = bitmap
                    changeBounds {
                        set(newBounds)
                    }
                }
            })
        }

        /*val oldWidth = rendererItem?.bitmap?.width ?: 0
        val oldHeight = rendererItem?.bitmap?.height ?: 0
        rendererItem = BitmapItem().apply { this.bitmap = bitmap }
        val newWidth = rendererItem?.bitmap?.width ?: 0
        val newHeight = rendererItem?.bitmap?.height ?: 0

        //保持缩放后的比例
        if (oldWidth > 0 && oldHeight > 0) {
            val scaleWidth = getBounds().width() / oldWidth
            val scaleHeight = getBounds().height() / oldHeight
            updateBounds(newWidth * scaleWidth, newHeight * scaleHeight)
        } else {
            updateBounds(newWidth.toFloat(), newHeight.toFloat())
        }
        refresh()*/

        return rendererItem as BitmapItem
    }
}