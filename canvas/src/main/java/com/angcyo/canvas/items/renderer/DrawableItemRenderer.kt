package com.angcyo.canvas.items.renderer

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import androidx.core.graphics.withMatrix
import com.angcyo.canvas.ScalePictureDrawable
import com.angcyo.canvas.Strategy
import com.angcyo.canvas.core.ICanvasView
import com.angcyo.canvas.core.renderer.ICanvasStep
import com.angcyo.canvas.items.*
import com.angcyo.canvas.utils.CanvasDataHandleOperate
import com.angcyo.library.ex.*
import com.angcyo.svg.Svg
import com.pixplicity.sharp.SharpDrawable

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/11
 */
open class DrawableItemRenderer<T : DrawableItem>(canvasView: ICanvasView) :
    BaseItemRenderer<T>(canvasView) {

    //<editor-fold desc="临时变量">

    val _flipMatrix = Matrix()
    val _flipRect = emptyRectF()

    //</editor-fold desc="临时变量">

    //<editor-fold desc="初始化">

    override fun getName(): CharSequence? {
        return _name ?: when (_rendererItem) {
            is ShapeItem -> "Shape"
            is PictureTextItem -> "Text"
            is PictureBitmapItem -> "Bitmap"
            is PictureShapeItem -> "Shape"
            else -> super.getName()
        }
    }

    override fun onUpdateRendererItem(item: T?, oldItem: T?) {
        super.onUpdateRendererItem(item, oldItem)
        initBounds(item, oldItem)
    }

    /**初始化默认的宽高*/
    open fun initBounds(item: T?, oldItem: T?) {
        if (item != oldItem) {
            val width = _rendererItem?.drawable?.minimumWidth?.toFloat() ?: 0f
            val height = _rendererItem?.drawable?.minimumHeight?.toFloat() ?: 0f
            if (width > 0 && height > 0) {
                changeBounds {
                    adjustSize(width, height, ADJUST_TYPE_LT)
                }
            }
        }
    }

    override fun render(canvas: Canvas) {
        _rendererItem?.drawable?.let { drawable ->
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
                bounds.adjustFlipRect(_flipRect)
                var sx = 1f
                var sy = 1f
                if (getBounds().isFlipHorizontal) {
                    sx = -1f
                }
                if (getBounds().isFlipVertical) {
                    sy = -1f
                }
                _flipMatrix.reset()
                _flipMatrix.postScale(sx, sy, _flipRect.centerX(), _flipRect.centerY())
                canvas.withMatrix(_flipMatrix) {
                    drawable.setBounds(
                        _flipRect.left.toInt(),
                        _flipRect.top.toInt(),
                        _flipRect.right.toInt(),
                        _flipRect.bottom.toInt()
                    )
                    drawable.draw(canvas)
                }
            }
        }
    }

    //</editor-fold desc="初始化">

    /**更新笔的样式, 目前只对[com.pixplicity.sharp.SharpDrawable]有效*/
    open fun updatePaintStyle(style: Paint.Style, strategy: Strategy = Strategy.normal) {
        val rendererItem = getRendererItem()
        val oldValue = rendererItem?.paint?.style
        if (oldValue == style) {
            return
        }

        val drawable = rendererItem?.drawable
        if (drawable is SharpDrawable) {
            if (drawable.pathList.isNotEmpty()) {
                rendererItem.paint.style = style

                val sharpDrawable =
                    Svg.loadPathList(drawable.pathList, drawable.pathBounds, style, null, 0, 0)
                rendererItem.drawable = sharpDrawable
                rendererItem.setHoldData(CanvasDataHandleOperate.KEY_SVG, sharpDrawable.pathList)

                refresh()
                if (strategy.type == Strategy.STRATEGY_TYPE_NORMAL) {
                    canvasViewBox.canvasView.getCanvasUndoManager()
                        .addUndoAction(object : ICanvasStep {
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
        }
    }
}