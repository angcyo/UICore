package com.angcyo.canvas.items.renderer

import android.graphics.Typeface
import com.angcyo.canvas.Strategy
import com.angcyo.canvas.core.ICanvasView
import com.angcyo.canvas.core.renderer.ICanvasStep
import com.angcyo.canvas.items.PictureTextItem
import com.angcyo.canvas.utils.isDeleteLine
import com.angcyo.canvas.utils.isTextBold
import com.angcyo.canvas.utils.isTextItalic
import com.angcyo.canvas.utils.isUnderLine
import com.angcyo.library.ex.add
import com.angcyo.library.ex.remove

/**
 * 通过Picture实现的drawText
 *
 * [PictureTextItem]
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/21
 */

class PictureTextItemRenderer(canvasView: ICanvasView) :
    PictureItemRenderer<PictureTextItem>(canvasView) {

    //<editor-fold desc="core">

    /**设置渲染的[text]*/
    fun setRenderText(text: String): PictureTextItem {
        val item = PictureTextItem(text)
        rendererItem = item
        updatePaint()
        return item
    }

    /**更新画笔*/
    fun updatePaint() {
        val item = getRendererRenderItem() ?: return
        paint.let {
            it.isStrikeThruText = item.textStyle.isDeleteLine
            it.isUnderlineText = item.textStyle.isUnderLine
            it.isFakeBoldText = item.textStyle.isTextBold
            it.textSkewX = if (item.textStyle.isTextItalic) PictureTextItem.ITALIC_SKEW else 0f
            it.typeface = item.textTypeface
            //it.textAlign = paintAlign
            //it.style = paintStyle
        }
        //更新对应的drawable
        requestRendererItemUpdate()
    }

    //</editor-fold desc="core">

    //<editor-fold desc="文本渲染操作方法">

    /**更新渲染的文本*/
    fun updateItemText(text: String, strategy: Strategy = Strategy.normal) {
        val item = getRendererRenderItem() ?: return
        val oldValue = item.text
        if (oldValue == text) {
            return
        }
        item.text = text
        requestRendererItemUpdate()
        if (strategy.type == Strategy.STRATEGY_TYPE_NORMAL) {
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
        val item = getRendererRenderItem() ?: return
        val oldValue = item.textStyle
        if (oldValue == style) {
            return
        }
        item.textStyle = style
        updatePaint()
        if (strategy.type == Strategy.STRATEGY_TYPE_NORMAL) {
            canvasViewBox.canvasView.getCanvasUndoManager().addUndoAction(object : ICanvasStep {
                override fun runUndo() {
                    updateTextStyle(oldValue, Strategy.undo)
                }

                override fun runRedo() {
                    updateTextStyle(style, Strategy.redo)
                }
            })
        }
    }

    /**激活文本样式*/
    fun enableTextStyle(style: Int, enable: Boolean = true, strategy: Strategy = Strategy.normal) {
        val item = getRendererRenderItem() ?: return
        val oldValue = item.textStyle

        val newValue = if (enable) {
            oldValue.add(style)
        } else {
            oldValue.remove(style)
        }

        if (oldValue == newValue) {
            return
        }
        item.textStyle = newValue
        updatePaint()
        if (strategy.type == Strategy.STRATEGY_TYPE_NORMAL) {
            canvasViewBox.canvasView.getCanvasUndoManager().addUndoAction(object : ICanvasStep {
                override fun runUndo() {
                    updateTextStyle(oldValue, Strategy.undo)
                }

                override fun runRedo() {
                    updateTextStyle(newValue, Strategy.redo)
                }
            })
        }
    }

    /**更新笔的字体*/
    fun updateTextTypeface(typeface: Typeface?, strategy: Strategy = Strategy.normal) {
        val item = getRendererRenderItem() ?: return
        val oldValue = paint.typeface
        if (oldValue == typeface) {
            return
        }
        item.textTypeface = typeface
        updatePaint()
        if (strategy.type == Strategy.STRATEGY_TYPE_NORMAL) {
            canvasViewBox.canvasView.getCanvasUndoManager().addUndoAction(object : ICanvasStep {
                override fun runUndo() {
                    updateTextTypeface(oldValue ?: Typeface.DEFAULT, Strategy.undo)
                }

                override fun runRedo() {
                    updateTextTypeface(typeface, Strategy.redo)
                }
            })
        }
    }

    /**更新文本的方向*/
    fun updateTextOrientation(orientation: Int, strategy: Strategy = Strategy.normal) {
        val item = getRendererRenderItem() ?: return
        val oldValue = item.orientation
        if (oldValue == orientation) {
            return
        }
        item.orientation = orientation
        requestRendererItemUpdate()//更新Drawable
        if (strategy.type == Strategy.STRATEGY_TYPE_NORMAL) {
            canvasViewBox.canvasView.getCanvasUndoManager().addUndoAction(object : ICanvasStep {
                override fun runUndo() {
                    updateTextOrientation(oldValue, Strategy.undo)
                }

                override fun runRedo() {
                    updateTextOrientation(orientation, Strategy.redo)
                }
            })
        }
    }

    //</editor-fold desc="文本渲染操作方法">
}