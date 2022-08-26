package com.angcyo.canvas.items.renderer

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.drawable.Drawable
import androidx.core.graphics.withMatrix
import com.angcyo.canvas.Strategy
import com.angcyo.canvas.core.ICanvasView
import com.angcyo.canvas.core.renderer.ICanvasStep
import com.angcyo.canvas.items.DrawableItem
import com.angcyo.canvas.utils.toDataTypeStr
import com.angcyo.library.component.ScalePictureDrawable
import com.angcyo.library.ex.adjustFlipRect
import com.angcyo.library.ex.emptyRectF
import com.angcyo.library.ex.isFlipHorizontal
import com.angcyo.library.ex.isFlipVertical

/**
 * 用来绘制[android.graphics.drawable.Drawable]
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
        return _name ?: _rendererItem?.dataType?.toDataTypeStr() ?: super.getName()
    }

    override fun onUpdateRendererItem(item: T?, oldItem: T?) {
        super.onUpdateRendererItem(item, oldItem)
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

    /**更新笔的样式
     * 针对[com.pixplicity.sharp.SharpDrawable]特殊处理*/
    open fun updatePaintStyle(style: Paint.Style, strategy: Strategy = Strategy.normal) {
        val oldValue = paint.style
        if (oldValue == style) {
            return
        }
        paint.style = style
        onRendererItemUpdate()
        if (strategy.type == Strategy.STRATEGY_TYPE_NORMAL) {
            canvasViewBox.canvasView.getCanvasUndoManager().addUndoAction(object : ICanvasStep {
                override fun runUndo() {
                    updatePaintStyle(oldValue, Strategy.undo)
                }

                override fun runRedo() {
                    updatePaintStyle(style, Strategy.redo)
                }
            })
        }
    }

    /**设置渲染的[drawable]*/
    open fun setRenderDrawable(drawable: Drawable?): T {
        val item = DrawableItem()
        item.drawable = drawable
        _rendererItem = item as T
        onRendererItemUpdate()
        return item
    }
}