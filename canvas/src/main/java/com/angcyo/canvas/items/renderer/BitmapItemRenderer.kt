package com.angcyo.canvas.items.renderer

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.RectF
import androidx.core.graphics.withMatrix
import com.angcyo.canvas.Strategy
import com.angcyo.canvas.core.ICanvasView
import com.angcyo.canvas.core.renderer.ICanvasStep
import com.angcyo.canvas.items.BitmapItem
import com.angcyo.canvas.utils.limitMaxWidthHeight
import com.angcyo.library.ex.*

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/22
 */
class BitmapItemRenderer(canvasView: ICanvasView) :
    BaseItemRenderer<BitmapItem>(canvasView) {

    val bitmapMatrix = Matrix()

    val tempRect = emptyRectF()

    init {
        _name = "Bitmap"
    }

    override fun onUpdateRendererItem(item: BitmapItem?, oldItem: BitmapItem?) {
        super.onUpdateRendererItem(item, oldItem)
    }

    override fun render(canvas: Canvas) {
        _rendererItem?.bitmap?.let { bitmap ->
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

    /**更新需要绘制的图片, 并保持原先的缩放比例
     * [keepBounds] 需要更新的Bounds, 如果有*/
    fun updateBitmap(
        bitmap: Bitmap,
        keepBounds: RectF? = null,
        strategy: Strategy = Strategy.normal
    ): BitmapItem {
        val oldValue = _rendererItem?.bitmap
        if (oldValue == bitmap) {
            return _rendererItem!!
        }

        val oldBounds = RectF(getBounds())
        val maxWidth = oldBounds.width()
        val maxHeight = oldBounds.height()

        if (_rendererItem == null) {
            _rendererItem = BitmapItem().apply { this.bitmap = bitmap }
        } else {
            _rendererItem?.bitmap = bitmap
        }

        if (keepBounds != null) {
            changeBounds {
                set(keepBounds)
            }
        } else {
            val newWidth = _rendererItem?.bitmap?.width ?: 0
            val newHeight = _rendererItem?.bitmap?.height ?: 0

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
        }

        if (strategy.type == Strategy.STRATEGY_TYPE_NORMAL && oldValue != null) {
            canvasViewBox.canvasView.getCanvasUndoManager().addUndoAction(object : ICanvasStep {

                val newBounds = RectF(getBounds())

                override fun runUndo() {
                    updateBitmap(oldValue, oldBounds, Strategy.undo)
                }

                override fun runRedo() {
                    updateBitmap(bitmap, newBounds, Strategy.redo)
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

        return _rendererItem as BitmapItem
    }
}