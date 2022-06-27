package com.angcyo.canvas.items

import android.graphics.Paint
import android.graphics.Rect
import android.widget.LinearLayout
import com.angcyo.canvas.ScalePictureDrawable
import com.angcyo.library.ex.*
import kotlin.math.max

/**
 * 文本组件渲染数据
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/21
 */
class PictureTextItem : PictureItem() {

    companion object {

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

        /**斜体的倾斜角度*/
        const val ITALIC_SKEW = -0.25f
    }

    /**需要绘制的文本*/
    var text: String? = null

    /**是否使用紧凑文本绘制*/
    var compactText: Boolean = false

    /**文本排列方向
     * [LinearLayout.HORIZONTAL]
     * [LinearLayout.VERTICAL]*/
    var orientation: Int = LinearLayout.HORIZONTAL

    /**换行绘制文本时的行间距, 行于行之间的间隙*/
    var lineSpacing: Float = 0f

    /**垂直绘制文本时的行间距, 字与字之间的间隙*/
    var wordSpacing: Float = 0f

    /**宽度增益的大小*/
    var widthIncrease: Float = 0f

    /**高度增益的大小*/
    var heightIncrease: Float = 0f

    init {
        itemName = "Text"
        compactText = true
        wordSpacing = 1 * dp //字间距
    }

    /**获取每一行的文本*/
    fun lineTextList(text: String): List<String> = text.lines()

    /**计算多行文本的宽度*/
    fun calcTextWidth(text: String): Float {
        var result = 0f
        val lineTextList = lineTextList(text)
        if (orientation == LinearLayout.HORIZONTAL) {
            //横向排列
            lineTextList.forEach {
                result = max(result, measureTextWidth(it))
            }
        } else {
            //纵向排列
            lineTextList.forEach { lineText ->
                var lineMax = 0f
                lineText.forEach {
                    lineMax = max(measureTextWidth("$it"), lineMax)
                }
                result += lineMax
            }
            result += lineSpacing * (lineTextList.size - 1)
        }
        return result
    }

    /**计算多行文本的高度*/
    fun calcTextHeight(text: String): Float {
        var result = 0f
        val lineTextList = lineTextList(text)
        if (orientation == LinearLayout.HORIZONTAL) {
            //横向排列
            lineTextList.forEach { lineText ->
                result += measureTextHeight(lineText)
            }
            result += lineSpacing * (lineTextList.size - 1)
        } else {
            //纵向排列
            lineTextList.forEach { lineText ->
                var lineHeight = 0f
                lineText.forEach {
                    //一个字一个字的高度
                    lineHeight += measureTextHeight("$it")
                }
                lineHeight += wordSpacing * (lineText.length - 1)
                result = max(result, lineHeight)
            }
        }
        return result
    }

    //temp
    val _textMeasureBounds = Rect()

    /**单行文本字符的宽度*/
    fun measureTextWidth(text: String): Float {
        val textWidth = if (compactText) {
            paint.textBounds(text, _textMeasureBounds)
            _textMeasureBounds.width().toFloat()
        } else {
            paint.textWidth(text)
        }

        //画笔的宽度
        val paintWidth = paint.strokeWidth

        //倾斜的宽度
        val skewWidth = if (paint.textSkewX != 0f) {
            //tan(paint.textSkewX.absoluteValue) * measureTextHeight(text)
            //paint.getTextBounds(drawText ?: "", 0, drawText?.length ?: 0, tempRect)
            //(tempRect.width() - width).toInt()
            0f
        } else {
            0f
        }

        return textWidth + skewWidth + paintWidth
    }

    /**单个文本字符的高度*/
    fun measureTextHeight(text: String): Float {
        return if (compactText) {
            paint.textBounds(text, _textMeasureBounds)
            _textMeasureBounds.height().toFloat()
        } else {
            paint.textHeight()
        }
    }

    /**下沉的距离*/
    fun measureTextDescent(text: String): Float {
        return if (compactText) {
            paint.textBounds(text).bottom.toFloat()
        } else {
            paint.descent()
        }
    }

    override fun updatePictureDrawable(resetSize: Boolean) {
        text?.let { drawText ->
            //createStaticLayout(drawText, paint)
            val width = calcTextWidth(drawText) + widthIncrease
            val height = calcTextHeight(drawText) + heightIncrease

            val itemWidth = width
            val itemHeight = height

            val drawable = ScalePictureDrawable(withPicture(itemWidth.toInt(), itemHeight.toInt()) {
                val lineTextList = lineTextList(drawText)

                var x = 0f
                var y = 0f

                if (orientation == LinearLayout.HORIZONTAL) {
                    x = when (paint.textAlign) {
                        Paint.Align.RIGHT -> itemWidth
                        Paint.Align.CENTER -> itemWidth / 2
                        else -> 0f
                    }

                    lineTextList.forEach { lineText ->
                        val lineTextHeight = calcTextHeight(lineText)
                        val descent = measureTextDescent(lineText)

                        y += lineTextHeight
                        drawText(lineText, x, y - descent, paint)
                        y += lineSpacing
                    }
                } else {
                    lineTextList.forEach { lineText ->
                        val textWidth = calcTextWidth(lineText)

                        var offsetX = when (paint.textAlign) {
                            Paint.Align.RIGHT -> textWidth
                            Paint.Align.CENTER -> textWidth / 2
                            else -> 0f
                        }

                        lineText.forEach { char ->
                            val text = "$char"
                            y += measureTextHeight(text)
                            val descent = measureTextDescent(text)
                            val textBounds = paint.textBounds(text)

                            if (compactText) {
                                offsetX = when (paint.textAlign) {
                                    Paint.Align.RIGHT -> textWidth /*+ textBounds.left.toFloat()*/
                                    Paint.Align.CENTER -> textWidth / 2
                                    else -> -textBounds.left.toFloat()
                                }
                            }

                            drawText(text, x + offsetX, y - descent, paint)
                            y += wordSpacing
                        }
                        x += textWidth + lineSpacing
                        y = 0f
                    }
                }
            })

            this.drawable = drawable
            this.itemWidth = itemWidth
            this.itemHeight = itemHeight
        }
    }
}