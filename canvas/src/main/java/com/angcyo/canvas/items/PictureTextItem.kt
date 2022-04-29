package com.angcyo.canvas.items

import android.graphics.Paint
import android.widget.LinearLayout
import com.angcyo.canvas.ScalePictureDrawable
import com.angcyo.library.ex.have
import com.angcyo.library.ex.textHeight
import com.angcyo.library.ex.textWidth
import com.angcyo.library.ex.withPicture
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.tan

/**
 * 文本组件数据
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

    /**文本排列方向
     * [LinearLayout.HORIZONTAL]
     * [LinearLayout.VERTICAL]*/
    var orientation: Int = LinearLayout.HORIZONTAL

    /**换行绘制文本时的行间距*/
    var lineSpacing: Float = 0f

    /**垂直绘制文本时的行间距*/
    var wordSpacing: Float = 0f

    /**宽度增益的大小*/
    var widthIncrease: Float = 0f

    /**高度增益的大小*/
    var heightIncrease: Float = 0f

    fun lineTextList(text: String): List<String> = text.lines()

    /**计算文本的宽度*/
    fun calcTextWidth(text: String): Float {
        var result = 0f
        val lineTextList = lineTextList(text)
        if (orientation == LinearLayout.HORIZONTAL) {
            lineTextList.forEach {
                result = max(result, paint.textWidth(it))
            }
        } else {
            lineTextList.forEach { lineText ->
                var lineMax = 0f
                lineText.forEach {
                    lineMax = max(paint.textWidth("$it"), lineMax)
                }
                result += lineMax + lineSpacing
            }
            result -= lineSpacing
        }
        return result
    }

    /**计算文本的高度*/
    fun calcTextHeight(text: String): Float {
        var result = 0f
        val lineTextList = lineTextList(text)
        if (orientation == LinearLayout.HORIZONTAL) {
            result = paint.textHeight() * lineTextList.size + lineSpacing * (lineTextList.size - 1)
        } else {
            lineTextList.forEach { lineText ->
                var lineHeight = 0f
                lineText.forEach {
                    lineHeight += paint.textHeight() + wordSpacing
                }
                lineHeight -= wordSpacing
                result = max(result, lineHeight)
            }
        }
        return result
    }

    override fun updatePictureDrawable() {
        text?.let { text ->
            //createStaticLayout(text, paint)
            val width = calcTextWidth(text) + widthIncrease
            val height = calcTextHeight(text) + heightIncrease

            //倾斜的宽度
            val skewWidth = if (paint.textSkewX != 0f) {
                tan(paint.textSkewX.absoluteValue) * height
                //paint.getTextBounds(text ?: "", 0, text?.length ?: 0, tempRect)
                //(tempRect.width() - width).toInt()
            } else {
                0f
            }

            val itemWidth = width + skewWidth
            val itemHeight = height

            val drawable = ScalePictureDrawable(withPicture(itemWidth.toInt(), itemHeight.toInt()) {
                val lineTextList = lineTextList(text)

                var x = 0f
                var y = 0f

                if (orientation == LinearLayout.HORIZONTAL) {
                    x = when (paint.textAlign) {
                        Paint.Align.RIGHT -> itemWidth
                        Paint.Align.CENTER -> itemWidth / 2
                        else -> 0f
                    }

                    lineTextList.forEach { text ->
                        val textHeight = calcTextHeight(text)

                        y += textHeight
                        drawText(text, x, y - paint.descent(), paint)
                        y += lineSpacing
                    }
                } else {
                    if (paint.textAlign == Paint.Align.LEFT) {
                        x = 0f
                    }

                    lineTextList.forEachIndexed { index, text ->
                        val textWidth = calcTextWidth(text)

                        when (paint.textAlign) {
                            Paint.Align.RIGHT -> x += textWidth
                            Paint.Align.CENTER -> x += textWidth / 2
                        }

                        text.forEach { char ->
                            y += paint.textHeight()
                            drawText("$char", x, y - paint.descent(), paint)
                            y += wordSpacing
                        }

                        if (paint.textAlign == Paint.Align.LEFT) {
                            x += textWidth
                        }

                        x += lineSpacing
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

val Int.isTextBold: Boolean
    get() = have(PictureTextItem.TEXT_STYLE_BOLD)

val Int.isUnderLine: Boolean
    get() = have(PictureTextItem.TEXT_STYLE_UNDER_LINE)

val Int.isDeleteLine: Boolean
    get() = have(PictureTextItem.TEXT_STYLE_DELETE_LINE)

val Int.isTextItalic: Boolean
    get() = have(PictureTextItem.TEXT_STYLE_ITALIC)