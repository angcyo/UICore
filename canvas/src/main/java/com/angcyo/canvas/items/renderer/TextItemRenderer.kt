package com.angcyo.canvas.items.renderer

import android.graphics.*
import com.angcyo.canvas.CanvasView
import com.angcyo.canvas.core.CanvasViewBox
import com.angcyo.canvas.core.component.ControlPoint
import com.angcyo.canvas.items.TextItem
import com.angcyo.canvas.utils.createTextPaint
import com.angcyo.library.ex.*
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.tan

/**
 * 文本组件渲染
 * [TextItem]
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/04/03
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class TextItemRenderer(canvasViewBox: CanvasViewBox) : BaseItemRenderer<TextItem>(canvasViewBox) {

    val paint = createTextPaint(Color.BLACK).apply {
        //init
        textSize = 12 * dp
    }

    /**Bounds*/
    val textBounds = Rect()

    /**宽度增益的大小*/
    var widthIncrease: Float = 0f

    /**高度增益的大小*/
    var heightIncrease: Float = 0f

    override fun updateRendererItem(item: TextItem?, oldItem: TextItem?) {
        super.updateRendererItem(item, oldItem)

        item?.let { updateTextPaint(item) }

        if (item != oldItem || item?.text != oldItem?.text) {
            val textWidth = getTextWidth()
            val textHeight = getTextHeight()
            if (getBounds().isEmpty) {
                changeBounds {
                    set(0f, 0f, textWidth, textHeight)
                }
            } else {
                if (textWidth > 0 && textHeight > 0) {
                    changeBounds {
                        adjustSizeWithLT(textWidth, textHeight)
                    }
                }
            }
        }
    }

    fun updateTextPaint(item: TextItem) {
        paint.apply {
            isStrikeThruText = item.isDeleteLine
            isUnderlineText = item.isUnderLine
            isFakeBoldText = item.isTextBold
            textSkewX = if (item.isTextItalic) -0.25f else 0f
            //typeface =

            val text = item.text ?: ""
            getTextBounds(text, 0, text.length, textBounds)
        }
    }

    override fun onCanvasSizeChanged(canvasView: CanvasView) {
        super.onCanvasSizeChanged(canvasView)
        if (_renderBounds.isEmpty) {
            changeBounds {
                set(0f, 0f, getTextWidth(), getTextHeight())
            }
        }
    }

    override fun onControlFinish(controlPoint: ControlPoint) {
        super.onControlFinish(controlPoint)
        if (controlPoint.type == ControlPoint.POINT_TYPE_SCALE) {
            changeBounds {
                adjustSizeWithLT(getTextWidth(), getTextHeight())
            }
        }
    }

    fun getTextWidth(): Float {
        val text = rendererItem?.text ?: ""
        var width = paint.textWidth(text)
        width += widthIncrease
        if (paint.textSkewX != 0f) {
            val skewWidth = tan(paint.textSkewX * 1.0) * getTextHeight()
            width += skewWidth.absoluteValue.toFloat()
        }
        return width
        /*return textBounds.width().toFloat() + widthIncrease*/
    }

    fun getTextHeight(): Float {
        var height = paint.textHeight()
        height += heightIncrease
        return height
        /*return textBounds.height().toFloat() + heightIncrease*/
    }

    //val _rect = Rect()

    override fun scaleBy(scaleX: Float, scaleY: Float, widthCenter: Boolean) {
        super.scaleBy(scaleX, scaleY, widthCenter)

        rendererItem?.let {
            val max = max(scaleX, scaleY)
            paint.textSize = paint.textSize * max
            updateTextPaint(it)

            if (widthCenter) {
                changeBounds {
                    adjustSizeWithCenter(getTextWidth(), getTextHeight())
                }
            } else {
                //等到操作结束后再更新
                //bounds.adjustSizeWithLT(paint.textWidth(rendererItem?.text ?: ""), getTextHeight())
            }

            //paint.getTextBounds(textItem.text, 0, textItem.text?.length ?: 0, _rect)//这样测量出来的文本高度, 非行高
            //bounds.adjustSize(_rect.width().toFloat(), _rect.height().toFloat())
        }
    }

    override fun render(canvas: Canvas) {
        val renderBounds = getRendererBounds()
        canvas.drawText(
            rendererItem?.text ?: "",
            renderBounds.left,
            renderBounds.bottom - paint.descent(),
            paint
        )
    }

    /**更新文本样式*/
    fun enableTextStyle(style: Int, enable: Boolean = true) {
        rendererItem?.apply {
            textStyle = if (enable) {
                textStyle.add(style)
            } else {
                textStyle.remove(style)
            }

            updateRendererItem(this)

            canvasViewBox.canvasView.refresh()
        }
    }

    /**更新笔的样式*/
    fun updatePaintStyle(style: Paint.Style) {
        rendererItem?.apply {
            paint.style = style
            updateRendererItem(this)
            canvasViewBox.canvasView.refresh()
        }
    }

    /**更新笔的字体*/
    fun updatePaintTypeface(typeface: Typeface?) {
        rendererItem?.apply {
            paint.typeface = typeface
            updateRendererItem(this)
            canvasViewBox.canvasView.refresh()
        }
    }
}

/**添加一个文本渲染器*/
fun CanvasView.addTextRenderer(text: String) {
    val textRenderer = TextItemRenderer(canvasViewBox)
    textRenderer.rendererItem = TextItem().apply { this.text = text }
    addCentreItemRenderer(textRenderer)
    selectedItem(textRenderer)
}