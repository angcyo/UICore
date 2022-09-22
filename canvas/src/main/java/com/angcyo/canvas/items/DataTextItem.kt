package com.angcyo.canvas.items

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.widget.LinearLayout
import com.angcyo.canvas.data.*
import com.angcyo.canvas.graphics.lineTextList
import com.angcyo.canvas.utils.FontManager
import com.angcyo.library.ex.textBounds
import com.angcyo.library.ex.textHeight
import com.angcyo.library.ex.textWidth
import com.angcyo.library.ex.toColor
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.tan

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/09/22
 */
class DataTextItem(bean: ItemDataBean) : DataItem(bean) {

    /**画笔*/
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 1f
        style = Paint.Style.FILL
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    //region ---操作---

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
            it.textSkewX = if (dataBean.isItalic()) PictureTextItem.ITALIC_SKEW else 0f
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
        paint.textBounds(text, _textMeasureBounds)
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
        return if (dataBean.isCompactText) {
            paint.textBounds(text, _textMeasureBounds)
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

    //endregion ---操作---

}