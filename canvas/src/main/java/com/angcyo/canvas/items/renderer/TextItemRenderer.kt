package com.angcyo.canvas.items.renderer

import android.graphics.*
import com.angcyo.canvas.CanvasView
import com.angcyo.canvas.core.CanvasViewBox
import com.angcyo.canvas.core.component.ControlPoint
import com.angcyo.canvas.items.PictureTextItem
import com.angcyo.library.ex.*
import kotlin.math.absoluteValue
import kotlin.math.tan

/**
 * 文本组件渲染, 通过drawText实现
 * 改变bounds时, 实时改变textSize, 会抖动. 不支持镜像
 * [TextItem]
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/04/03
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
@Deprecated("效果不好, 请使用[PictureItemRenderer]")
class TextItemRenderer(canvasViewBox: CanvasViewBox) :
    BaseItemRenderer<PictureTextItem>(canvasViewBox) {

    /**Bounds*/
    val textBounds = Rect()

    /**宽度增益的大小*/
    var widthIncrease: Float = 0f

    /**高度增益的大小*/
    var heightIncrease: Float = 0f

    override fun onUpdateRendererItem(item: PictureTextItem?, oldItem: PictureTextItem?) {
        super.onUpdateRendererItem(item, oldItem)

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
                        adjustSize(textWidth, textHeight, ADJUST_TYPE_LT)
                    }
                }
            }
        }
    }

    fun updateTextPaint(item: PictureTextItem) {
        item.updatePaint(item.paint)
        val text = item.text ?: ""
        item.paint.getTextBounds(text, 0, text.length, textBounds)
    }

    override fun onCanvasSizeChanged(canvasView: CanvasView) {
        super.onCanvasSizeChanged(canvasView)
        if (_renderBounds.isNoSize()) {
            changeBounds {
                set(0f, 0f, getTextWidth(), getTextHeight())
            }
        }
    }

    override fun onControlFinish(controlPoint: ControlPoint) {
        super.onControlFinish(controlPoint)
        if (controlPoint.type == ControlPoint.POINT_TYPE_SCALE) {
            changeBounds {
                val newWidth = if (isFlipHorizontal) -getTextWidth() else getTextWidth()
                val newHeight = if (isFlipVertical) -getTextHeight() else getTextHeight()
                adjustSize(newWidth, newHeight, ADJUST_TYPE_LT)
            }
        }
    }

    override fun isSupportControlPoint(type: Int): Boolean {
        if (type == ControlPoint.POINT_TYPE_LOCK) {
            return false
        }
        return super.isSupportControlPoint(type)
    }

    fun getTextWidth(): Float {
        val text = rendererItem?.text ?: ""
        var width = rendererItem?.paint?.textWidth(text) ?: 0f
        width += widthIncrease
        if (rendererItem?.paint?.textSkewX != 0f) {
            val skewWidth = tan(rendererItem?.paint?.textSkewX!! * 1.0) * getTextHeight()
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

    override fun onItemBoundsChanged() {
        super.onItemBoundsChanged()
        rendererItem?.apply {
            updateLargestTextSizeWhichFits(getBounds())

            /*if (!changeBeforeBounds.isEmpty) {
                val scaleX = getBounds().width() / changeBeforeBounds.width()
                val scaleY = getBounds().height() / changeBeforeBounds.height()
                val max = max(scaleX, scaleY)
                paint.textSize = paint.textSize * max
            }*/

            updateTextPaint(this)
        }
    }

    /**[android.widget.TextView#findLargestTextSizeWhichFits]*/
    private fun updateLargestTextSizeWhichFits(availableSpace: RectF) {
        val paint = rendererItem?.paint ?: return
        val maxWidth = availableSpace.width().absoluteValue
        val maxHeight = availableSpace.height().absoluteValue

        var largestSize = paint.textSize
        var adjustStep = 0.05f

        if (getTextWidth() > maxWidth || getTextHeight() > maxHeight) {
            //需要减少size
            adjustStep = -adjustStep

            while (!(getTextWidth() < maxWidth && getTextHeight() < maxHeight)) {
                largestSize = paint.textSize
                val size = largestSize + adjustStep
                paint.textSize = size
                if (size <= 0f) {
                    largestSize = 0f
                    break
                }
            }
        } else {
            while (!(getTextWidth() > maxWidth && getTextHeight() > maxHeight)) {
                largestSize = paint.textSize
                paint.textSize = largestSize + adjustStep
            }
        }

        paint.textSize = largestSize
    }

    /* override fun scaleBy(scaleX: Float, scaleY: Float, withCenter: Boolean) {
         super.scaleBy(scaleX, scaleY, withCenter)

         rendererItem?.let {
             val max = max(scaleX, scaleY)
             paint.textSize = paint.textSize * max
             updateTextPaint(it)

             if (withCenter) {
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
     }*/

    override fun render(canvas: Canvas) {
        rendererItem?.apply {
            val renderBounds = getRenderBounds()
            canvas.drawText(
                text ?: "",
                renderBounds.flipLeft,
                renderBounds.flipBottom - paint.descent(),
                paint
            )
        }
    }

    /**更新文本样式*/
    fun enableTextStyle(style: Int, enable: Boolean = true) {
        rendererItem?.apply {
            textStyle = if (enable) {
                textStyle.add(style)
            } else {
                textStyle.remove(style)
            }

            onUpdateRendererItem(this)

            canvasViewBox.canvasView.refresh()
        }
    }

    /**更新笔的样式*/
    fun updatePaintStyle(style: Paint.Style) {
        rendererItem?.apply {
            paint.style = style
            onUpdateRendererItem(this)
            canvasViewBox.canvasView.refresh()
        }
    }

    /**更新笔的字体*/
    fun updatePaintTypeface(typeface: Typeface?) {
        rendererItem?.apply {
            paint.typeface = typeface
            onUpdateRendererItem(this)
            canvasViewBox.canvasView.refresh()
        }
    }

    /**更新文本*/
    fun updateText(text: String) {
        rendererItem?.apply {
            this.text = text
            onUpdateRendererItem(this)
            canvasViewBox.canvasView.refresh()
        }
    }
}