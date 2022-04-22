package com.angcyo.canvas.items.renderer

import android.graphics.*
import android.text.TextPaint
import com.angcyo.canvas.CanvasView
import com.angcyo.canvas.ScalePictureDrawable
import com.angcyo.canvas.core.CanvasViewBox
import com.angcyo.canvas.items.PictureTextItem
import com.angcyo.canvas.utils.createTextPaint
import com.angcyo.library.ex.*
import kotlin.math.absoluteValue
import kotlin.math.tan

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/21
 */
class PictureTextItemRenderer(canvasViewBox: CanvasViewBox) :
    BaseItemRenderer<PictureTextItem>(canvasViewBox) {

    /**Bounds*/
    val textBounds = RectF()

    /**宽度增益的大小*/
    var widthIncrease: Float = 0f

    /**高度增益的大小*/
    var heightIncrease: Float = 0f

    fun getTextWidth(): Float {
        val text = rendererItem?.text ?: ""
        var width = rendererItem?.paint?.textWidth(text) ?: 0f
        width += widthIncrease
        if (rendererItem?.paint?.textSkewX != 0f) {
            val skewWidth =
                tan((rendererItem?.paint?.textSkewX ?: 0f) * 1.0) * getTextHeight()
            width += skewWidth.absoluteValue.toFloat()
        }
        return width
        /*return textBounds.width().toFloat() + widthIncrease*/
    }

    fun getTextHeight(): Float {
        var height = rendererItem?.paint.textHeight()
        height += heightIncrease
        return height
        /*return textBounds.height().toFloat() + heightIncrease*/
    }

    /**更新绘制的内容*/
    fun updateTextDrawable() {
        rendererItem?.apply {
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
            if (textBounds.isEmpty) {
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
    fun addTextRender(text: String, paint: TextPaint) {
        rendererItem = PictureTextItem().apply {
            this.paint = paint
            updatePaintStyle()
            this.text = text
        }
        updateTextDrawable()
    }

    /**更新文本样式*/
    fun updateTextStyle(style: Int) {
        rendererItem?.apply {
            textStyle = style
            updatePaintStyle()
            updateTextDrawable()
        }
    }

    /**激活文本样式*/
    fun enableTextStyle(style: Int, enable: Boolean = true) {
        rendererItem?.apply {
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
        rendererItem?.paint?.apply {
            this.style = style
            updateTextDrawable()
        }
    }

    /**更新笔的字体*/
    fun updatePaintTypeface(typeface: Typeface?) {
        rendererItem?.paint?.apply {
            this.typeface = typeface
            updateTextDrawable()
        }
    }

    /**更新文本*/
    fun updateText(text: String) {
        rendererItem?.apply {
            this.text = text
            updateTextDrawable()
        }
    }

    override fun render(canvas: Canvas) {
        rendererItem?.drawable?.let { drawable ->
            val bounds = getRendererBounds()
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

/**添加一个文本渲染器*/
fun CanvasView.addPictureTextRenderer(
    text: String,
    paint: TextPaint = createTextPaint(Color.BLACK).apply {
        //init
        textSize = 12 * dp
    }
) {
    val renderer = PictureTextItemRenderer(canvasViewBox)
    renderer.addTextRender(text, paint)
    addCentreItemRenderer(renderer)
    selectedItem(renderer)
}