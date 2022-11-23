package com.angcyo.canvas.items.data

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.widget.LinearLayout
import com.angcyo.canvas.Strategy
import com.angcyo.canvas.data.*
import com.angcyo.canvas.graphics.TextGraphicsParser
import com.angcyo.canvas.graphics.lineTextList
import com.angcyo.library.annotation.Pixel
import com.angcyo.library.component.FontManager
import com.angcyo.library.ex.*
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.tan

/**
 * 文本数据item
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/09/22
 */
class DataTextItem(bean: CanvasProjectItemBean) : DataItem(bean) {

    companion object {

        //---

        /**字体样式, 无*/
        const val TEXT_STYLE_NONE = 0x00

        /**字体样式, 加粗*/
        const val TEXT_STYLE_BOLD = 0x01

        /**字体样式, 斜体*/
        const val TEXT_STYLE_ITALIC = 0x02

        /**字体样式, 下划线*/
        const val TEXT_STYLE_UNDER_LINE = 0x04

        /**字体样式, 删除线*/
        const val TEXT_STYLE_DELETE_LINE = 0x08

        //---

        /**斜体的倾斜角度*/
        const val ITALIC_SKEW = -0.25f
    }


    //region ---属性---

    /**画笔*/
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 1f
        style = Paint.Style.FILL
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    //endregion ---属性---

    //region ---方法---

    /**更新画笔属性*/
    fun updatePaint() {
        val typefaceInfo =
            FontManager.loadTypefaceInfo(dataBean.fontFamily) ?: FontManager.getSystemFontList()
                .firstOrNull()
        textPaint.let {
            //删除线
            it.isStrikeThruText = dataBean.linethrough
            //下划线
            it.isUnderlineText = dataBean.underline
            it.isFakeBoldText = dataBean.isBold()
            it.textSkewX = if (dataBean.isItalic()) ITALIC_SKEW else 0f
            //it.typeface = item.textTypeface

            it.textAlign = dataBean.textAlign.toPaintAlign()
            it.style = dataBean.paintStyle.toPaintStyle()

            it.textSize = dataBean.fontSize.toPixel()
            it.color = dataBean.textColor?.toColor() ?: Color.BLACK

            typefaceInfo?.typeface?.let { typeface ->
                textPaint.typeface = typeface
            }
        }
    }

    /**计算多行文本的宽度*/
    fun calcTextWidth(text: String, paint: Paint = textPaint): Float {
        var result = 0f
        val lineTextList = text.lineTextList()
        if (dataBean.orientation == LinearLayout.HORIZONTAL) {
            //横向排列
            lineTextList.forEach { lineText ->
                var lineWidth = 0f
                lineText.forEach {
                    //一个字一个字的宽度
                    lineWidth += measureTextWidth("$it", paint)
                }
                lineWidth += dataBean.charSpacing.toPixel() * (lineText.length - 1)
                result = max(result, lineWidth)
            }
        } else {
            //纵向排列
            lineTextList.forEach { lineText ->
                var lineMax = 0f
                lineText.forEach {
                    lineMax = max(measureTextWidth("$it", paint), lineMax)
                }
                result += lineMax
            }
            result += dataBean.lineSpacing.toPixel() * (lineTextList.size - 1)
        }
        return result
    }

    /**计算多行文本的高度*/
    fun calcTextHeight(text: String, paint: Paint = textPaint): Float {
        var result = 0f
        val lineTextList = text.lineTextList()
        if (dataBean.orientation == LinearLayout.HORIZONTAL) {
            //横向排列
            lineTextList.forEach { lineText ->
                result += measureTextHeight(lineText, paint)
            }
            result += dataBean.lineSpacing.toPixel() * (lineTextList.size - 1)
        } else {
            //纵向排列
            lineTextList.forEach { lineText ->
                var lineHeight = 0f
                lineText.forEach {
                    //一个字一个字的高度
                    lineHeight += measureTextHeight("$it", paint)
                }
                lineHeight += dataBean.charSpacing.toPixel() * (lineText.length - 1)
                result = max(result, lineHeight)
            }
        }
        return result
    }

    //temp
    val _textMeasureBounds = Rect()

    var _skewWidth: Float = 0f

    /**单行文本字符的宽度*/
    fun measureTextWidth(text: String, paint: Paint = textPaint): Float {
        if (dataBean.isCompactText) {
            if (text.isBlank()) {
                //空格
                val textWidth = paint.textWidth(text).toInt()
                _textMeasureBounds.set(0, 0, textWidth, textWidth)
            } else {
                paint.textBounds(text, _textMeasureBounds)
            }
        }
        val textWidth = if (dataBean.isCompactText) {
            _textMeasureBounds.width().toFloat()
        } else {
            paint.textWidth(text)
        }

        //画笔的宽度
        val paintWidth = paint.strokeWidth

        //倾斜的宽度
        _skewWidth = if (paint.textSkewX != 0f) {
            tan(paint.textSkewX.absoluteValue) * (_textMeasureBounds.height() / 3)
            //paint.getTextBounds(drawText ?: "", 0, drawText?.length ?: 0, tempRect)
            //(tempRect.width() - width).toInt()
            //0f
        } else {
            0f
        }

        return textWidth + _skewWidth + paintWidth
    }

    /**单个文本字符的高度*/
    fun measureTextHeight(text: String, paint: Paint = textPaint): Float {
        if (dataBean.isCompactText) {
            if (text.isBlank()) {
                //空格
                val textWidth = paint.textWidth(text).toInt()
                _textMeasureBounds.set(0, 0, textWidth, textWidth)
            } else {
                paint.textBounds(text, _textMeasureBounds)
            }
        }
        return if (dataBean.isCompactText) {
            _textMeasureBounds.height().toFloat()
        } else {
            paint.textHeight()
        }
    }

    /**下沉的距离*/
    fun measureTextDescent(text: String, paint: Paint = textPaint): Float {
        return if (dataBean.isCompactText) {
            paint.textBounds(text).bottom.toFloat()
        } else {
            paint.descent()
        }
    }

    //endregion ---方法---

    //region ---可恢复的操作---

    /**紧凑/宽松文本*/
    fun updateTextCompact(
        compactText: Boolean,
        renderer: DataItemRenderer,
        strategy: Strategy = Strategy.normal
    ) {
        val old = dataBean.isCompactText
        if (old == compactText) {
            return
        }
        renderer.canvasView.getCanvasUndoManager().addAndRedo(strategy, {
            dataBean.isCompactText = old
            updateRenderItem(renderer)
        }) {
            dataBean.isCompactText = compactText
            updateRenderItem(renderer)
        }
    }

    /**更新字体*/
    fun updateTextTypeface(
        typeface: Typeface?,
        renderer: DataItemRenderer,
        strategy: Strategy = Strategy.normal
    ) {
        val typefaceInfo = if (typeface == null) {
            FontManager.getSystemFontList().firstOrNull()
        } else {
            FontManager.loadTypefaceInfo(typeface)
        }
        updateTextTypeface(typefaceInfo?.name, renderer, strategy)
    }

    /**更新字体*/
    fun updateTextTypeface(
        name: String?,
        renderer: DataItemRenderer,
        strategy: Strategy = Strategy.normal
    ) {
        val old = dataBean.fontFamily
        if (old == name) {
            return
        }
        renderer.canvasView.getCanvasUndoManager().addAndRedo(strategy, {
            dataBean.fontFamily = old
            updateRenderItem(renderer)
        }) {
            dataBean.fontFamily = name
            updateRenderItem(renderer)
        }
    }

    /**更新文本样式*/
    fun updateTextStyle(
        style: Int,
        enable: Boolean,
        renderer: DataItemRenderer,
        strategy: Strategy = Strategy.normal
    ) {
        val old = dataBean.textStyle()
        val newValue = if (enable) {
            old.add(style)
        } else {
            old.remove(style)
        }
        if (old == newValue) {
            return
        }
        renderer.canvasView.getCanvasUndoManager().addAndRedo(strategy, {
            dataBean.setTextStyle(old)
            updateRenderItem(renderer)
        }) {
            dataBean.setTextStyle(newValue)
            updateRenderItem(renderer)
        }
    }

    /**更新画笔绘制文本时的对齐方式*/
    fun updatePaintAlign(
        align: Paint.Align,
        renderer: DataItemRenderer,
        strategy: Strategy = Strategy.normal
    ) {
        val old = dataBean.textAlign
        val new = align.toAlignString()
        if (old == new) {
            return
        }
        renderer.canvasView.getCanvasUndoManager().addAndRedo(strategy, {
            dataBean.textAlign = old
            updateRenderItem(renderer)
        }) {
            dataBean.textAlign = new
            updateRenderItem(renderer)
        }
    }

    /**更新字体大小*/
    fun updateTextSize(
        @Pixel
        textSize: Float,
        renderer: DataItemRenderer,
        strategy: Strategy = Strategy.normal
    ) {
        val old = dataBean.fontSize.toPixel()
        val new = textSize
        if (old == new) {
            return
        }
        renderer.canvasView.getCanvasUndoManager().addAndRedo(strategy, {
            dataBean.fontSize = old.toMm()
            updateRenderItem(renderer)
        }) {
            dataBean.fontSize = new.toMm()
            updateRenderItem(renderer)
        }
    }

    /**更新字间距*/
    fun updateTextWordSpacing(
        @Pixel
        wordSpacing: Float,
        renderer: DataItemRenderer,
        strategy: Strategy = Strategy.normal
    ) {
        val old = dataBean.charSpacing.toPixel()
        val new = wordSpacing
        if (old == new) {
            return
        }
        renderer.canvasView.getCanvasUndoManager().addAndRedo(strategy, {
            dataBean.charSpacing = old.toMm()
            updateRenderItem(renderer)
        }) {
            dataBean.charSpacing = new.toMm()
            updateRenderItem(renderer)
        }
    }

    /**更新行间距*/
    fun updateTextLineSpacing(
        @Pixel
        lineSpacing: Float,
        renderer: DataItemRenderer,
        strategy: Strategy = Strategy.normal
    ) {
        val old = dataBean.lineSpacing.toPixel()
        val new = lineSpacing
        if (old == new) {
            return
        }
        renderer.canvasView.getCanvasUndoManager().addAndRedo(strategy, {
            dataBean.lineSpacing = old.toMm()
            updateRenderItem(renderer)
        }) {
            dataBean.lineSpacing = new.toMm()
            updateRenderItem(renderer)
        }
    }

    /**更新文本排列方向*/
    fun updateTextOrientation(
        orientation: Int,
        renderer: DataItemRenderer,
        strategy: Strategy = Strategy.normal
    ) {
        val old = dataBean.orientation
        val new = orientation
        if (old == new) {
            return
        }
        renderer.canvasView.getCanvasUndoManager().addAndRedo(strategy, {
            dataBean.orientation = old
            updateRenderItem(renderer)
        }) {
            dataBean.orientation = new
            updateRenderItem(renderer)
        }
    }

    /**更新文本*/
    fun updateText(
        text: String?,
        type: Int,
        renderer: DataItemRenderer,
        strategy: Strategy = Strategy.normal
    ) {
        val oldText = dataBean.text
        val oldType = dataBean.mtype
        if (oldText == text && oldType == type) {
            return
        }
        renderer.canvasView.getCanvasUndoManager().addAndRedo(strategy, {
            dataBean.text = oldText
            dataBean.mtype = oldType
            TextGraphicsParser().updateRotateOffset(this)
            updateRenderItem(renderer)
        }) {
            dataBean.text = text
            dataBean.mtype = type
            TextGraphicsParser().updateRotateOffset(this)
            updateRenderItem(renderer)
        }
    }

    //endregion ---可恢复的操作---

}