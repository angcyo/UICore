package com.angcyo.canvas.items.renderer

import android.graphics.*
import android.graphics.drawable.Drawable
import android.widget.LinearLayout
import com.angcyo.canvas.LinePath
import com.angcyo.canvas.Reason
import com.angcyo.canvas.Strategy
import com.angcyo.canvas.core.ICanvasView
import com.angcyo.canvas.core.component.ControlPoint
import com.angcyo.canvas.core.renderer.ICanvasStep
import com.angcyo.canvas.items.PictureBitmapItem
import com.angcyo.canvas.items.PictureItem
import com.angcyo.canvas.items.PictureShapeItem
import com.angcyo.canvas.items.PictureTextItem
import com.angcyo.canvas.utils.limitMaxWidthHeight
import com.angcyo.library.ex.add
import com.angcyo.library.ex.isNoSize
import com.angcyo.library.ex.isSizeChanged
import com.angcyo.library.ex.remove

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/29
 */
class PictureItemRenderer(canvasView: ICanvasView) :
    DrawableItemRenderer<PictureItem>(canvasView) {

    override fun isSupportControlPoint(type: Int): Boolean {
        if (type == ControlPoint.POINT_TYPE_LOCK) {
            val item = getRendererItem()
            if (item is PictureShapeItem) {
                if (item.shapePath is LinePath) {
                    //线段不支持任意比例缩放
                    return false
                }
            }
        }
        return super.isSupportControlPoint(type)
    }

    override fun itemBoundsChanged(reason: Reason, oldBounds: RectF) {
        super.itemBoundsChanged(reason, oldBounds)
        getRendererItem()?.let {
            if (it is PictureShapeItem && !oldBounds.isNoSize() && oldBounds.isSizeChanged(getBounds())) {
                it.shapePath?.apply {
                    val matrix = Matrix()
                    val scaleX = getBounds().width() / oldBounds.width()
                    val scaleY = getBounds().height() / oldBounds.height()
                    matrix.postScale(scaleX, scaleY, it.shapeBounds.left, it.shapeBounds.top)
                    transform(matrix)
                    it.updatePictureDrawable(true)
                }
            }
        }
    }

    /**当渲染的[drawable]改变后, 调用此方法, 更新bounds*/
    fun updatePictureDrawableBounds(oldWidth: Float = 0f, oldHeight: Float = 0f) {
        getRendererItem()?.let { item ->
            val bounds = getBounds()
            val newWith = item.itemWidth
            val newHeight = item.itemHeight
            if (bounds.isNoSize() || oldWidth == 0f || oldHeight == 0f) {
                //首次更新bounds
                updateBounds(newWith, newHeight)
            } else {
                //再次更新bounds
                val scaleWidth = bounds.width() / oldWidth
                val scaleHeight = bounds.height() / oldHeight
                if (scaleWidth == 1f && scaleHeight == 1f) {
                    //限制目标大小到原来的大小
                    limitMaxWidthHeight(newWith, newHeight, oldWidth, oldHeight).apply {
                        updateBounds(this[0], this[1])
                    }
                } else {
                    //重新缩放当前的大小,达到和原来的缩放效果一致性
                    updateBounds(newWith * scaleWidth, newHeight * scaleHeight)
                }
            }
            refresh()
        }
    }

    //<editor-fold desc="操作方法">

    /**围绕[updatePictureDrawableBounds]*/
    fun wrapItemUpdate(block: PictureItem.() -> Unit) {
        getRendererItem()?.let {
            val oldWidth = it.itemWidth
            val oldHeight = it.itemHeight
            it.block()
            updatePictureDrawableBounds(oldWidth, oldHeight)
        }
    }

    /**更新笔的样式*/
    fun updatePaintStyle(
        style: Paint.Style,
        strategy: Strategy = Strategy.normal
    ) {
        val oldValue = getRendererItem()?.paint?.style
        if (oldValue == style) {
            return
        }
        wrapItemUpdate {
            paint.style = style
            updatePaint()
        }
        if (strategy.type == Strategy.STRATEGY_TYPE_NORMAL) {
            canvasViewBox.canvasView.getCanvasUndoManager().addUndoAction(object : ICanvasStep {
                override fun runUndo() {
                    updatePaintStyle(
                        oldValue ?: Paint.Style.STROKE,
                        Strategy.undo
                    )
                }

                override fun runRedo() {
                    updatePaintStyle(style, Strategy.redo)
                }
            })
        }
    }

    /**更新画笔绘制文本时的对齐方式*/
    fun updatePaintAlign(
        align: Paint.Align,
        strategy: Strategy = Strategy.normal
    ) {
        val oldValue = getRendererItem()?.paint?.textAlign
        if (oldValue == align) {
            return
        }
        wrapItemUpdate {
            paint.textAlign = align
            updatePaint()
        }
        if (strategy.type == Strategy.STRATEGY_TYPE_NORMAL) {
            canvasViewBox.canvasView.getCanvasUndoManager().addUndoAction(object : ICanvasStep {
                override fun runUndo() {
                    updatePaintAlign(
                        oldValue ?: Paint.Align.LEFT,
                        Strategy.undo
                    )
                }

                override fun runRedo() {
                    updatePaintAlign(align, Strategy.redo)
                }
            })
        }
    }

    //</editor-fold desc="操作方法">

    //<editor-fold desc="文本渲染操作方法">

    /**添加一个文本用来渲染*/
    fun addTextRender(text: String): PictureTextItem {
        _rendererItem = PictureTextItem().apply {
            this.text = text
            updatePaint()
        }
        updatePictureDrawableBounds()
        return _rendererItem as PictureTextItem
    }

    /**更新渲染的文本*/
    fun updateItemText(text: String, strategy: Strategy = Strategy.normal) {
        val item = getRendererItem()
        val oldValue = if (item is PictureTextItem) {
            item.text
        } else {
            null
        }
        if (oldValue == text) {
            return
        }
        wrapItemUpdate {
            if (this is PictureTextItem) {
                this.text = text
                updatePaint()
            }
        }
        if (strategy.type == Strategy.STRATEGY_TYPE_NORMAL && oldValue != null) {
            canvasViewBox.canvasView.getCanvasUndoManager().addUndoAction(object : ICanvasStep {
                override fun runUndo() {
                    updateItemText(oldValue, Strategy.undo)
                }

                override fun runRedo() {
                    updateItemText(text, Strategy.redo)
                }
            })
        }
    }

    /**更新文本样式*/
    fun updateTextStyle(style: Int, strategy: Strategy = Strategy.normal) {
        val item = getRendererItem()
        val oldValue = if (item is PictureTextItem) {
            item.textStyle
        } else {
            null
        }
        if (oldValue == style) {
            return
        }
        wrapItemUpdate {
            if (this is PictureTextItem) {
                textStyle = style
                updatePaint()
            }
        }
        if (strategy.type == Strategy.STRATEGY_TYPE_NORMAL) {
            canvasViewBox.canvasView.getCanvasUndoManager().addUndoAction(object : ICanvasStep {
                override fun runUndo() {
                    updateTextStyle(
                        oldValue ?: PictureTextItem.TEXT_STYLE_NONE,
                        Strategy.undo
                    )
                }

                override fun runRedo() {
                    updateTextStyle(style, Strategy.redo)
                }
            })
        }
    }

    /**激活文本样式*/
    fun enableTextStyle(
        style: Int,
        enable: Boolean = true,
        strategy: Strategy = Strategy.normal
    ) {
        val item = getRendererItem()
        val oldValue = if (item is PictureTextItem) {
            item.textStyle
        } else {
            style
        }

        val newValue = if (enable) {
            oldValue.add(style)
        } else {
            oldValue.remove(style)
        }

        if (oldValue == newValue) {
            return
        }
        wrapItemUpdate {
            if (this is PictureTextItem) {
                textStyle = newValue
                updatePaint()
            }
        }
        if (strategy.type == Strategy.STRATEGY_TYPE_NORMAL) {
            canvasViewBox.canvasView.getCanvasUndoManager().addUndoAction(object : ICanvasStep {
                override fun runUndo() {
                    updateTextStyle(
                        oldValue ?: PictureTextItem.TEXT_STYLE_NONE,
                        Strategy.undo
                    )
                }

                override fun runRedo() {
                    updateTextStyle(
                        newValue ?: PictureTextItem.TEXT_STYLE_NONE,
                        Strategy.redo
                    )
                }
            })
        }
    }

    /**更新笔的字体*/
    fun updateTextTypeface(
        typeface: Typeface?,
        strategy: Strategy = Strategy.normal
    ) {
        val oldValue = getRendererItem()?.paint?.typeface
        if (oldValue == typeface) {
            return
        }
        wrapItemUpdate {
            if (this is PictureTextItem) {
                paint.typeface = typeface
                updatePaint()
            }
        }
        if (strategy.type == Strategy.STRATEGY_TYPE_NORMAL) {
            canvasViewBox.canvasView.getCanvasUndoManager().addUndoAction(object : ICanvasStep {
                override fun runUndo() {
                    updateTextTypeface(
                        oldValue ?: Typeface.DEFAULT,
                        Strategy.undo
                    )
                }

                override fun runRedo() {
                    updateTextTypeface(typeface, Strategy.redo)
                }
            })
        }
    }

    /**更新文本的方向*/
    fun updateTextOrientation(
        orientation: Int,
        strategy: Strategy = Strategy.normal
    ) {
        val item = getRendererItem()
        val oldValue = if (item is PictureTextItem) {
            item.orientation
        } else {
            null
        }
        if (oldValue == orientation) {
            return
        }
        wrapItemUpdate {
            if (this is PictureTextItem) {
                this.orientation = orientation
                updatePaint()
            }
        }
        if (strategy.type == Strategy.STRATEGY_TYPE_NORMAL) {
            canvasViewBox.canvasView.getCanvasUndoManager().addUndoAction(object : ICanvasStep {
                override fun runUndo() {
                    updateTextOrientation(
                        oldValue ?: LinearLayout.HORIZONTAL,
                        Strategy.undo
                    )
                }

                override fun runRedo() {
                    updateTextOrientation(orientation, Strategy.redo)
                }
            })
        }
    }

    //</editor-fold desc="文本渲染操作方法">

    //<editor-fold desc="Shapes渲染操作方法">

    /**添加一个文本用来渲染*/
    fun addShapeRender(path: Path): PictureShapeItem {
        _rendererItem = PictureShapeItem().apply {
            this.shapePath = path
            updatePaint()
        }
        updatePictureDrawableBounds()
        return _rendererItem as PictureShapeItem
    }

    /**更新渲染的Path*/
    fun updateItemPath(path: Path, strategy: Strategy = Strategy.normal) {
        val item = getRendererItem()
        val oldValue = if (item is PictureShapeItem) {
            item.shapePath
        } else {
            null
        }
        if (oldValue == path) {
            return
        }
        wrapItemUpdate {
            if (this is PictureShapeItem) {
                this.shapePath = path
                updatePaint()
            }
        }
        if (strategy.type == Strategy.STRATEGY_TYPE_NORMAL && oldValue != null) {
            canvasViewBox.canvasView.getCanvasUndoManager().addUndoAction(object : ICanvasStep {
                override fun runUndo() {
                    updateItemPath(oldValue, Strategy.undo)
                }

                override fun runRedo() {
                    updateItemPath(path, Strategy.redo)
                }
            })
        }
    }

    //</editor-fold desc="Shapes渲染操作方法">

    //<editor-fold desc="Bitmap渲染操作方法">

    /**添加一个图片用来渲染*/
    fun addBitmapRender(bitmap: Bitmap): PictureBitmapItem {
        _rendererItem = PictureBitmapItem().apply {
            this.originBitmap = bitmap
            this.bitmap = bitmap
            updatePaint()
        }
        updatePictureDrawableBounds()
        return _rendererItem as PictureBitmapItem
    }

    /**更新需要绘制的图片, 并保持原先的缩放比例
     * [bounds] 需要更新的Bounds, 如果有*/
    fun updateItemBitmap(
        bitmap: Bitmap,
        holdData: Map<String, Any?>? = null,
        bounds: RectF? = null,
        strategy: Strategy = Strategy.normal
    ) {
        val item = getRendererItem()
        val oldValue = if (item is PictureBitmapItem) {
            item.bitmap
        } else {
            null
        }
        if (oldValue == bitmap) {
            return
        }

        val oldBounds = RectF(getBounds())
        val oldData = item?.holdData

        if (bounds != null) {
            getRendererItem()?.apply {
                this.holdData = holdData
                if (this is PictureBitmapItem) {
                    this.bitmap = bitmap
                    updatePaint()

                    changeBounds {
                        set(bounds)
                    }
                }
            }
        } else {
            wrapItemUpdate {
                this.holdData = holdData
                if (this is PictureBitmapItem) {
                    this.bitmap = bitmap
                    updatePaint()
                }
            }
        }

        if (strategy.type == Strategy.STRATEGY_TYPE_NORMAL && oldValue != null) {
            canvasViewBox.canvasView.getCanvasUndoManager().addUndoAction(object : ICanvasStep {

                val newBounds = RectF(getBounds())

                override fun runUndo() {
                    updateItemBitmap(oldValue, oldData, oldBounds, Strategy.undo)
                }

                override fun runRedo() {
                    updateItemBitmap(bitmap, holdData, newBounds, Strategy.redo)
                }
            })
        }
    }

    /**直接更新[drawable]*/
    fun updateItemDrawable(
        drawable: Drawable?,
        holdData: Map<String, Any?>? = null,
        bounds: RectF? = null,
        strategy: Strategy = Strategy.normal
    ) {
        val item = getRendererItem()
        val oldValue = item?.drawable

        if (oldValue == drawable) {
            return
        }

        val oldBounds = RectF(getBounds())
        val oldData = item?.holdData

        if (bounds != null) {
            getRendererItem()?.apply {
                this.holdData = holdData
                if (this is PictureBitmapItem) {
                    this.bitmap = null
                }
                updateDrawable(drawable)

                changeBounds {
                    set(bounds)
                }
            }
        } else {
            wrapItemUpdate {
                this.holdData = holdData
                if (this is PictureBitmapItem) {
                    this.bitmap = null
                }
                updateDrawable(drawable)
            }
        }

        if (strategy.type == Strategy.STRATEGY_TYPE_NORMAL && oldValue != null) {
            canvasViewBox.canvasView.getCanvasUndoManager().addUndoAction(object : ICanvasStep {

                val newBounds = RectF(getBounds())

                override fun runUndo() {
                    updateItemDrawable(oldValue, oldData, oldBounds, Strategy.undo)
                }

                override fun runRedo() {
                    updateItemDrawable(drawable, holdData, newBounds, Strategy.redo)
                }
            })
        }
    }

    //</editor-fold desc="Bitmap渲染操作方法">

}