package com.angcyo.canvas.items.renderer

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.RectF
import androidx.core.graphics.withMatrix
import com.angcyo.canvas.ScalePictureDrawable
import com.angcyo.canvas.core.ICanvasView
import com.angcyo.canvas.items.*
import com.angcyo.library.ex.*

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/11
 */
open class DrawableItemRenderer<T : DrawableItem>(canvasView: ICanvasView) :
    BaseItemRenderer<T>(canvasView) {

    //<editor-fold desc="临时变量">

    val _flipMatrix = Matrix()
    val _flipRect = RectF()

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
        if (item != oldItem) {
            val bounds = getBounds()
            if (bounds.isNoSize()) {
                initBounds()
            }
        }
    }

    open fun initBounds() {
        changeBounds {
            adjustSize(
                _rendererItem?.drawable?.minimumWidth?.toFloat() ?: 0f,
                _rendererItem?.drawable?.minimumHeight?.toFloat() ?: 0f,
                ADJUST_TYPE_LT
            )
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
}