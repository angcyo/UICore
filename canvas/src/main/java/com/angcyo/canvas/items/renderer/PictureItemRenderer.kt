package com.angcyo.canvas.items.renderer

import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import com.angcyo.canvas.core.CanvasViewBox
import com.angcyo.canvas.items.PictureItem
import com.angcyo.canvas.items.PictureShapeItem
import com.angcyo.canvas.items.PictureTextItem
import com.angcyo.library.ex.add
import com.angcyo.library.ex.remove

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/29
 */
class PictureItemRenderer(canvasViewBox: CanvasViewBox) :
    DrawableItemRenderer<PictureItem>(canvasViewBox) {

    /**当渲染的[drawable]改变后, 调用此方法, 更新bounds*/
    fun updatePictureDrawableBounds(oldWidth: Float = 0f, oldHeight: Float = 0f) {
        rendererItem?.let { item ->
            val bounds = getBounds()
            val newWith = item.itemWidth
            val newHeight = item.itemHeight
            if (bounds.isEmpty || oldWidth == 0f || oldHeight == 0f) {
                //首次更新bounds
                updateBounds(newWith, newHeight)
            } else {
                //再次更新bounds
                val scaleWidth = bounds.width() / oldWidth
                val scaleHeight = bounds.height() / oldHeight
                updateBounds(newWith * scaleWidth, newHeight * scaleHeight)
            }
            refresh()
        }
    }

    //<editor-fold desc="操作方法">

    /**围绕[updatePictureDrawableBounds]*/
    fun wrapItemUpdate(block: PictureItem.() -> Unit) {
        rendererItem?.let {
            val oldWidth = it.itemWidth
            val oldHeight = it.itemHeight
            it.block()
            updatePictureDrawableBounds(oldWidth, oldHeight)
        }
    }

    /**更新笔的样式*/
    fun updatePaintStyle(style: Paint.Style) {
        wrapItemUpdate {
            this.paintStyle = style
            updatePaint()
        }
    }

    /**更新画笔绘制文本时的对齐方式*/
    fun updatePaintAlign(align: Paint.Align) {
        wrapItemUpdate {
            this.paintAlign = align
            updatePaint()
        }
    }

    //</editor-fold desc="操作方法">

    //<editor-fold desc="文本渲染操作方法">

    /**添加一个文本用来渲染*/
    fun addTextRender(text: String) {
        rendererItem = PictureTextItem().apply {
            this.text = text
            updatePaint()
        }
        updatePictureDrawableBounds()
    }

    /**更新渲染的文本*/
    fun updateItemText(text: String) {
        wrapItemUpdate {
            if (this is PictureTextItem) {
                this.text = text
                updatePaint()
            }
        }
    }

    /**更新文本样式*/
    fun updateTextStyle(style: Int) {
        wrapItemUpdate {
            if (this is PictureTextItem) {
                textStyle = style
                updatePaint()
            }
        }
    }

    /**激活文本样式*/
    fun enableTextStyle(style: Int, enable: Boolean = true) {
        wrapItemUpdate {
            if (this is PictureTextItem) {
                textStyle = if (enable) {
                    textStyle.add(style)
                } else {
                    textStyle.remove(style)
                }
                updatePaint()
            }
        }
    }

    /**更新笔的字体*/
    fun updateTextTypeface(typeface: Typeface?) {
        wrapItemUpdate {
            if (this is PictureTextItem) {
                this.paintTypeface = typeface
                updatePaint()
            }
        }
    }

    /**更新文本的方向*/
    fun updateTextOrientation(orientation: Int) {
        wrapItemUpdate {
            if (this is PictureTextItem) {
                this.orientation = orientation
                updatePaint()
            }
        }
    }

    //</editor-fold desc="文本渲染操作方法">

    //<editor-fold desc="Shapes渲染操作方法">

    /**添加一个文本用来渲染*/
    fun addShapeRender(path: Path) {
        rendererItem = PictureShapeItem().apply {
            this.shapePath = path
            updatePaint()
        }
        updatePictureDrawableBounds()
    }

    /**更新渲染的Path*/
    fun updateItemPath(path: Path) {
        wrapItemUpdate {
            if (this is PictureShapeItem) {
                this.shapePath = path
                updatePaint()
            }
        }
    }

    //</editor-fold desc="Shapes渲染操作方法">

}