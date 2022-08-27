package com.angcyo.canvas.items

import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.widget.LinearLayout
import com.angcyo.canvas.TypefaceInfo
import com.angcyo.canvas.utils.CanvasConstant
import com.angcyo.library.component.ScalePictureDrawable
import com.angcyo.library.ex.*
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.tan

/**
 * 文本组件渲染数据
 *
 * [com.angcyo.canvas.laser.pecker.CanvasFontPopupConfig]
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/21
 */
class PictureTextItem(
    /**需要绘制的文本*/
    var text: String
) : PictureDrawableItem() {

    companion object {

        /**默认的字体列表*/
        val DEFAULT_TYPEFACE_LIST = mutableListOf<TypefaceInfo>().apply {
            //系统默认字体
            //typefaceItem("normal", Typeface.DEFAULT)
            //typefaceItem("sans", Typeface.SANS_SERIF)
            add(TypefaceInfo("serif", Typeface.SERIF))
            add(TypefaceInfo("Default-Normal", Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)))
            add(TypefaceInfo("Default-Bold", Typeface.create(Typeface.DEFAULT, Typeface.BOLD)))
            add(TypefaceInfo("Default-Italic", Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)))
            add(
                TypefaceInfo(
                    "Default-Bold-Italic",
                    Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
                )
            )
        }

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

    /**是否使用紧凑文本绘制*/
    var isCompactText: Boolean = false

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

    /**字体样式*/
    var textStyle: Int = TEXT_STYLE_NONE

    /**默认字体*/
    var textTypeface: Typeface? = DEFAULT_TYPEFACE_LIST[1].typeface

    init {
        itemLayerName = "Text"
        isCompactText = true
        wordSpacing = 1 * dp //字间距
        dataType = CanvasConstant.DATA_TYPE_TEXT
        dataMode = CanvasConstant.DATA_MODE_GREY
    }

    /**获取每一行的文本*/
    fun lineTextList(text: String): List<String> = text.lines()

    /**计算多行文本的宽度*/
    fun calcTextWidth(paint: Paint, text: String): Float {
        var result = 0f
        val lineTextList = lineTextList(text)
        if (orientation == LinearLayout.HORIZONTAL) {
            //横向排列
            lineTextList.forEach {
                result = max(result, measureTextWidth(paint, it))
            }
        } else {
            //纵向排列
            lineTextList.forEach { lineText ->
                var lineMax = 0f
                lineText.forEach {
                    lineMax = max(measureTextWidth(paint, "$it"), lineMax)
                }
                result += lineMax
            }
            result += lineSpacing * (lineTextList.size - 1)
        }
        return result
    }

    /**计算多行文本的高度*/
    fun calcTextHeight(paint: Paint, text: String): Float {
        var result = 0f
        val lineTextList = lineTextList(text)
        if (orientation == LinearLayout.HORIZONTAL) {
            //横向排列
            lineTextList.forEach { lineText ->
                result += measureTextHeight(paint, lineText)
            }
            result += lineSpacing * (lineTextList.size - 1)
        } else {
            //纵向排列
            lineTextList.forEach { lineText ->
                var lineHeight = 0f
                lineText.forEach {
                    //一个字一个字的高度
                    lineHeight += measureTextHeight(paint, "$it")
                }
                lineHeight += wordSpacing * (lineText.length - 1)
                result = max(result, lineHeight)
            }
        }
        return result
    }

    //temp
    val _textMeasureBounds = Rect()

    var _skewWidth: Float = 0f

    /**单行文本字符的宽度*/
    fun measureTextWidth(paint: Paint, text: String): Float {
        paint.textBounds(text, _textMeasureBounds)
        val textWidth = if (isCompactText) {
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
    fun measureTextHeight(paint: Paint, text: String): Float {
        return if (isCompactText) {
            paint.textBounds(text, _textMeasureBounds)
            _textMeasureBounds.height().toFloat()
        } else {
            paint.textHeight()
        }
    }

    /**下沉的距离*/
    fun measureTextDescent(paint: Paint, text: String): Float {
        return if (isCompactText) {
            paint.textBounds(text).bottom.toFloat()
        } else {
            paint.descent()
        }
    }

    /**重绘文本[Drawable]
     * [com.angcyo.canvas.items.DrawableItem.drawable]
     * */
    override fun updateItem(paint: Paint) {
        val drawText = text
        //createStaticLayout(drawText, paint)
        val width = calcTextWidth(paint, drawText) + widthIncrease
        val height = calcTextHeight(paint, drawText) + heightIncrease

        val drawable = ScalePictureDrawable(withPicture(width.toInt(), height.toInt()) {
            val lineTextList = lineTextList(drawText)

            var x = 0f
            var y = 0f

            if (orientation == LinearLayout.HORIZONTAL) {
                x = when (paint.textAlign) {
                    Paint.Align.RIGHT -> width
                    Paint.Align.CENTER -> width / 2
                    else -> 0f
                }

                lineTextList.forEach { lineText ->
                    val lineTextHeight = calcTextHeight(paint, lineText)
                    val descent = measureTextDescent(paint, lineText)

                    val offsetX = if (isCompactText) {
                        when (paint.textAlign) {
                            Paint.Align.RIGHT -> -_skewWidth.toInt()
                            Paint.Align.CENTER -> -_textMeasureBounds.left / 2 - _skewWidth.toInt() / 2
                            else -> -_textMeasureBounds.left
                        }
                    } else {
                        0
                    }

                    y += lineTextHeight
                    drawText(lineText, x + offsetX, y - descent, paint)
                    y += lineSpacing
                }
            } else {
                lineTextList.forEach { lineText ->
                    val textWidth = calcTextWidth(paint, lineText)

                    var offsetX = when (paint.textAlign) {
                        Paint.Align.RIGHT -> textWidth - _skewWidth.toInt()
                        Paint.Align.CENTER -> textWidth / 2 - _skewWidth.toInt() / 2
                        else -> 0f
                    }

                    lineText.forEach { char ->
                        val text = "$char"
                        y += measureTextHeight(paint, text)
                        val descent = measureTextDescent(paint, text)
                        val textBounds = paint.textBounds(text)

                        if (isCompactText) {
                            offsetX = when (paint.textAlign) {
                                Paint.Align.RIGHT -> textWidth - _skewWidth.toInt() /*+ textBounds.left.toFloat()*/
                                Paint.Align.CENTER -> textWidth / 2 - _skewWidth.toInt() / 2
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
        this.itemWidth = width
        this.itemHeight = height
    }
}