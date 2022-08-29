package com.angcyo.canvas.items.renderer

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.graphics.drawable.PictureDrawable
import com.angcyo.canvas.LinePath
import com.angcyo.canvas.Reason
import com.angcyo.canvas.Strategy
import com.angcyo.canvas.core.ICanvasView
import com.angcyo.canvas.core.component.ControlPoint
import com.angcyo.canvas.core.component.SmartAssistant
import com.angcyo.canvas.core.renderer.ICanvasStep
import com.angcyo.canvas.items.PictureBitmapItem
import com.angcyo.canvas.items.PictureDrawableItem
import com.angcyo.canvas.items.PictureShapeItem
import com.angcyo.canvas.utils.isLineShape
import com.angcyo.library.ex.isNoSize
import com.angcyo.library.gesture.RectScaleGestureHandler

/**
 * [PictureDrawable]
 *
 * [com.angcyo.library.component.ScalePictureDrawable]
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/29
 */
open class PictureItemRenderer<T : PictureDrawableItem>(canvasView: ICanvasView) :
    DrawableItemRenderer<T>(canvasView) {

    init {
        paint.strokeWidth = 1f //* dp
        paint.style = Paint.Style.STROKE
    }

    override fun changeBounds(reason: Reason, block: RectF.() -> Unit): Boolean {
        return super.changeBounds(reason, block)
    }

    override fun isSupportControlPoint(type: Int): Boolean {
        if (type == ControlPoint.POINT_TYPE_LOCK) {
            val item = getRendererRenderItem()
            if (item is PictureShapeItem) {
                if (item.shapePath is LinePath) {
                    //线段不支持任意比例缩放
                    return false
                }
            }
        }
        return super.isSupportControlPoint(type)
    }

    override fun isSupportSmartAssistant(type: Int): Boolean {
        if (isLineShape()) {
            //线段不支持调整高度
            return type != SmartAssistant.SMART_TYPE_H
        }
        return super.isSupportSmartAssistant(type)
    }

    override fun onChangeBoundsAfter(reason: Reason) {
        super.onChangeBoundsAfter(reason)
        if (isLineShape()) {
            if (reason.reason == Reason.REASON_USER && !changeBeforeBounds.isNoSize()) {
                val bounds = getBounds()
                //线段,只能调整宽度
                RectScaleGestureHandler.updateRectTo(
                    changeBeforeBounds,
                    bounds,
                    bounds.width(),
                    getRendererRenderItem()?.itemHeight ?: 1f,
                    rotate,
                    changeBeforeBounds.left,
                    changeBeforeBounds.top
                )
            }
        }
    }

    override fun itemBoundsChanged(reason: Reason, oldBounds: RectF) {
        super.itemBoundsChanged(reason, oldBounds)
        /*getRendererItem()?.let {
            if (it is PictureShapeItem && !oldBounds.isNoSize() && oldBounds.isSizeChanged(getBounds())) {
                it.shapePath?.apply {
                    if (this is LinePath) {
                        //no
                    } else {
                        val scaleX = getBounds().width() / oldBounds.width()
                        val scaleY = getBounds().height() / oldBounds.height()
                        if (scaleX != 1f || scaleY != 1f) {
                            val matrix = Matrix()
                            matrix.postScale(
                                scaleX,
                                scaleY,
                                it.shapeBounds.left,
                                it.shapeBounds.top
                            )
                            transform(matrix)
                            it.updatePictureDrawable(true)
                        }
                    }
                }
            }
        }*/
    }

    //<editor-fold desc="渲染操作方法">

    /**设置一个新的[rendererItem]
     * [bounds] 是否强制指定新的bounds*/
    fun updateRendererItem(item: T?, bounds: RectF? = null, strategy: Strategy = Strategy.normal) {
        val oldItem = getRendererRenderItem()
        if (oldItem == item) {
            return
        }

        val oldBounds = RectF(getBounds())
        var setBounds: RectF? = null

        setRendererRenderItem(item)

        if (bounds != null) {
            setBounds = RectF(bounds)
            changeBounds {
                set(bounds)
            }
        }

        if (strategy.type == Strategy.STRATEGY_TYPE_NORMAL) {
            canvasViewBox.canvasView.getCanvasUndoManager().addUndoAction(object : ICanvasStep {

                val newBounds = RectF(getBounds())

                override fun runUndo() {
                    updateRendererItem(oldItem, oldBounds, Strategy.undo)
                }

                override fun runRedo() {
                    updateRendererItem(item, setBounds ?: newBounds, Strategy.redo)
                }
            })
        }
    }

    /**直接更新[drawable]*/
    fun updateItemDrawable(
        drawable: Drawable?,
        bounds: RectF? = null,
        strategy: Strategy = Strategy.normal
    ) {
        val item = getRendererRenderItem() ?: return
        val oldValue = item.drawable

        val oldBounds = RectF(getBounds())
        item.drawable = drawable

        requestRendererItemUpdate()//

        if (bounds != null) {
            changeBounds {
                set(bounds)
            }
        }

        if (strategy.type == Strategy.STRATEGY_TYPE_NORMAL) {
            canvasViewBox.canvasView.getCanvasUndoManager().addUndoAction(object : ICanvasStep {

                val newBounds = RectF(getBounds())

                override fun runUndo() {
                    updateItemDrawable(oldValue, oldBounds, Strategy.undo)
                }

                override fun runRedo() {
                    updateItemDrawable(drawable, newBounds, Strategy.redo)
                }
            })
        }
    }

    /**更新算法修改后的图片, 并保持原先的缩放比例
     * [bounds] 需要强制更新的Bounds, 如果有*/
    fun updateItemModifyBitmap(
        modifyBitmap: Bitmap?,
        previewDrawable: Drawable?,
        bounds: RectF? = null,
        strategy: Strategy = Strategy.normal
    ) {
        val item = getRendererRenderItem() ?: return
        if (item !is PictureBitmapItem) {
            throw IllegalStateException("类型不匹配")
        }
        val oldBounds = RectF(getBounds())

        val oldModifyBitmap = item.modifyBitmap
        val oldPreviewDrawable = item.previewDrawable
        item.modifyBitmap = modifyBitmap
        item.previewDrawable = previewDrawable

        requestRendererItemUpdate()//

        bounds?.let {
            changeBounds {
                set(it)
            }
        }

        if (strategy.type == Strategy.STRATEGY_TYPE_NORMAL) {
            canvasViewBox.canvasView.getCanvasUndoManager().addUndoAction(object : ICanvasStep {

                val newBounds = RectF(getBounds())

                override fun runUndo() {
                    updateItemModifyBitmap(
                        oldModifyBitmap,
                        oldPreviewDrawable,
                        oldBounds,
                        Strategy.undo
                    )
                }

                override fun runRedo() {
                    updateItemModifyBitmap(modifyBitmap, previewDrawable, newBounds, Strategy.redo)
                }
            })
        }
    }

    //</editor-fold desc="渲染操作方法">

}