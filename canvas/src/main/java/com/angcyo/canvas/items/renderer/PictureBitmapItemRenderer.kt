package com.angcyo.canvas.items.renderer

import android.graphics.Bitmap
import android.graphics.RectF
import android.graphics.drawable.Drawable
import com.angcyo.canvas.Strategy
import com.angcyo.canvas.core.ICanvasView
import com.angcyo.canvas.core.renderer.ICanvasStep
import com.angcyo.canvas.items.PictureBitmapItem

/**
 * [PictureBitmapItem]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/08/23
 */
class PictureBitmapItemRenderer(canvasView: ICanvasView) :
    PictureItemRenderer<PictureBitmapItem>(canvasView) {

    /**设置渲染的图片*/
    fun setRenderBitmap(bitmap: Bitmap): PictureBitmapItem {
        val item = PictureBitmapItem()
        item.originBitmap = bitmap
        item.previewBitmap = bitmap
        item.bitmap = bitmap
        _rendererItem = item

        onRendererItemUpdate()
        return item
    }

    /**更新需要绘制的图片, 并保持原先的缩放比例
     * [bounds] 需要更新的Bounds, 如果有*/
    fun updateItemBitmap(
        bitmap: Bitmap,
        holdData: Map<String, Any?>? = getRendererItem()?.holdData,
        bounds: RectF? = null,
        strategy: Strategy = Strategy.normal
    ) {
        val item = getRendererItem() ?: return
        val oldValue = item.bitmap ?: item.drawable
        if (oldValue == bitmap) {
            return
        }

        val oldBounds = RectF(getBounds())
        val oldData = item.holdData
        val oldPreviewBitmap = item.previewBitmap

        item.holdData = holdData
        item.bitmap = bitmap
        item.previewBitmap = oldPreviewBitmap

        onRendererItemUpdate()//

        if (strategy.type == Strategy.STRATEGY_TYPE_NORMAL && oldValue != null) {
            canvasViewBox.canvasView.getCanvasUndoManager().addUndoAction(object : ICanvasStep {

                val newBounds = RectF(getBounds())

                override fun runUndo() {
                    if (oldValue is Bitmap) {
                        updateItemBitmap(oldValue, oldData, oldBounds, Strategy.undo)
                    } else if (oldValue is Drawable) {
                        updateItemDrawable(oldValue, oldData, oldBounds, Strategy.undo)
                    }
                }

                override fun runRedo() {
                    updateItemBitmap(bitmap, holdData, newBounds, Strategy.redo)
                }
            })
        }
    }
}