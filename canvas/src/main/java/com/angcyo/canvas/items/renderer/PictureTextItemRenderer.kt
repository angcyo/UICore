package com.angcyo.canvas.items.renderer

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextPaint
import com.angcyo.canvas.ScalePictureDrawable
import com.angcyo.canvas.core.ICanvasView
import com.angcyo.canvas.items.PictureTextItem
import com.angcyo.library.ex.*
import kotlin.math.absoluteValue
import kotlin.math.tan

/**
 * 通过Picture实现的drawText, 效果比每次都drawText好
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/21
 */

@Deprecated("通用性不够好, 请使用[PictureItemRenderer]")
class PictureTextItemRenderer(canvasView: ICanvasView) :
    BaseItemRenderer<PictureTextItem>(canvasView) {

    /**Bounds*/
    val textBounds = emptyRectF()

    /**宽度增益的大小*/
    var widthIncrease: Float = 0f

    /**高度增益的大小*/
    var heightIncrease: Float = 0f

    fun getTextWidth(): Float {
        val text = _rendererItem?.text ?: ""
        var width = _rendererItem?.paint?.textWidth(text) ?: 0f
        width += widthIncrease
        if (_rendererItem?.paint?.textSkewX != 0f) {
            val skewWidth =
                tan((_rendererItem?.paint?.textSkewX ?: 0f) * 1.0) * getTextHeight()
            width += skewWidth.absoluteValue.toFloat()
        }
        return width
        /*return textBounds.width().toFloat() + widthIncrease*/
    }

    fun getTextHeight(): Float {
        var height = _rendererItem?.paint.textHeight()
        height += heightIncrease
        return height
        /*return textBounds.height().toFloat() + heightIncrease*/
    }

    /**更新绘制的内容*/
    fun updateTextDrawable() {
        _rendererItem?.apply {
            val width = paint.textWidth(text)
            val height = paint.textHeight()

            //倾斜的宽度
            val skewWidth = if (paint.textSkewX != 0f) {
                tan(paint.textSkewX.absoluteValue) * height
                //paint.getTextBounds(text ?: "", 0, text?.length ?: 0, tempRect)
                //(tempRect.width() - width).toInt()
            } else {
                0
            }

            drawable = ScalePictureDrawable(
                withPicture(
                    width.toInt() + skewWidth.toInt(),
                    height.toInt()
                ) {
                    drawText(text ?: "", 0f, height - paint.descent(), paint)
                })
            val textWidth = getTextWidth()
            val textHeight = getTextHeight()
            if (textBounds.isNoSize()) {
                textBounds.set(0f, 0f, textWidth, textHeight)
                updateBounds(textWidth, textHeight)
            } else {
                val scaleWidth = getBounds().width() / textBounds.width()
                val scaleHeight = getBounds().height() / textBounds.height()
                textBounds.set(0f, 0f, textWidth, textHeight)
                updateBounds(textWidth * scaleWidth, textHeight * scaleHeight)
            }
            refresh()
        }
    }

    /**添加一个文本用来渲染*/
    fun addTextRender(text: String, paint: TextPaint): PictureTextItem {
        _rendererItem = PictureTextItem().apply {
            this.paint = paint
            updatePaint(paint)
            this.text = text
        }
        updateTextDrawable()
        return _rendererItem as PictureTextItem
    }

    /**更新文本样式*/
    fun updateTextStyle(style: Int) {
        _rendererItem?.apply {
            textStyle = style
            updatePaint(paint)
            updateTextDrawable()
        }
    }

    /**激活文本样式*/
    fun enableTextStyle(style: Int, enable: Boolean = true) {
        _rendererItem?.apply {
            textStyle = if (enable) {
                textStyle.add(style)
            } else {
                textStyle.remove(style)
            }
            updateTextStyle(textStyle)
        }
    }

    /**更新笔的样式*/
    fun updatePaintStyle(style: Paint.Style) {
        _rendererItem?.paint?.apply {
            this.style = style
            updateTextDrawable()
        }
    }

    /**更新笔的字体*/
    fun updatePaintTypeface(typeface: Typeface?) {
        _rendererItem?.paint?.apply {
            this.typeface = typeface
            updateTextDrawable()
        }
    }

    /**更新文本*/
    fun updateText(text: String) {
        _rendererItem?.apply {
            this.text = text
            updateTextDrawable()
        }
    }

    override fun render(canvas: Canvas) {
        _rendererItem?.drawable?.let { drawable ->
            val bounds = getRenderBounds()
            drawable.setBounds(
                bounds.left.toInt(),
                bounds.top.toInt(),
                bounds.right.toInt(),
                bounds.bottom.toInt()
            )
            drawable.draw(canvas)
        }
    }
}