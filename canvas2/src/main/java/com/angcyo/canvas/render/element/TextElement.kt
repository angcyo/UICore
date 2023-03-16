package com.angcyo.canvas.render.element

import android.graphics.*
import android.graphics.drawable.Drawable
import android.widget.LinearLayout
import com.angcyo.canvas.render.core.CanvasRenderDelegate
import com.angcyo.canvas.render.core.Reason
import com.angcyo.canvas.render.core.component.BaseControlPoint
import com.angcyo.canvas.render.data.RenderParams
import com.angcyo.canvas.render.data.TextProperty
import com.angcyo.canvas.render.renderer.BaseRenderer
import com.angcyo.canvas.render.state.IStateStack
import com.angcyo.canvas.render.state.TextStateStack
import com.angcyo.library.annotation.Pixel
import com.angcyo.library.component.FontManager
import com.angcyo.library.ex.*
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.tan

/**
 * 用来绘制[String]元素的对象
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/03/13
 */
open class TextElement : BaseElement() {

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

        //---

        /**文本居中对齐*/
        const val TEXT_ALIGN_CENTER = "center"

        /**文本左对齐*/
        const val TEXT_ALIGN_LEFT = "left"

        /**文本右对齐*/
        const val TEXT_ALIGN_RIGHT = "right"

        //---

        /**斜体的倾斜系数*/
        const val ITALIC_SKEW = -0.25f
    }

    //region---属性---

    /**文本属性*/
    var textProperty: TextProperty = TextProperty()

    //endregion---属性---

    /**获取每一行的文本*/
    protected fun String?.lineTextList(): List<String> = this?.lines() ?: emptyList()

    override fun createStateStack(): IStateStack = TextStateStack()

    override fun requestElementRenderDrawable(renderParams: RenderParams?): Drawable? {
        updatePaint()
        return createPictureDrawable(renderParams) {
            val renderMatrix = renderProperty.getDrawMatrix(includeRotate = true)
            concat(renderMatrix)
            drawNormalText(this)
        }
    }

    /**更新画笔样式*/
    fun updatePaint() {
        //字体加载
        val loadTypefaceInfo = FontManager.loadTypefaceInfo(textProperty.fontFamily)
        val typefaceInfo = loadTypefaceInfo ?: FontManager.getSystemFontList().firstOrNull()
        paint.let {
            //删除线
            it.isStrikeThruText = textProperty.isStrikeThruText
            //下划线
            it.isUnderlineText = textProperty.isUnderlineText
            it.isFakeBoldText = textProperty.isFakeBoldText
            it.textSkewX = if (textProperty.isItalic) ITALIC_SKEW else 0f //倾斜

            if (textProperty.text.lineTextList().size() <= 1) {
                //单行文本的对齐方式
                it.textAlign = textProperty.textAlign.toPaintAlign()
            } else {
                //多行文本的对齐方式
                it.textAlign = Paint.Align.LEFT
            }
            it.style = textProperty.paintStyle

            it.textSize = textProperty.fontSize
            it.color = textProperty.textColor?.toColor() ?: Color.BLACK

            typefaceInfo?.typeface?.let { typeface ->
                it.typeface = typeface
            }
        }
    }

    //region---操作---

    /**更新文本, 并保持可视化的宽高不变*/
    open fun updateOriginText(text: String?, keepVisibleSize: Boolean = false) {
        textProperty.text = text
        updateOriginWidthHeight(calcLineTextWidth(text), calcLineTextHeight(text), keepVisibleSize)
    }

    /**更新文本属性, 并且自动回退*/
    fun updateTextProperty(
        renderer: BaseRenderer?,
        delegate: CanvasRenderDelegate?,
        keepGroupProperty: Boolean = false,
        keepVisibleSize: Boolean = false,
        block: TextProperty.() -> Unit
    ) {
        val reason: Reason = Reason.user.apply {
            controlType = if (keepGroupProperty) {
                BaseControlPoint.CONTROL_TYPE_KEEP_GROUP_PROPERTY or
                        BaseControlPoint.CONTROL_TYPE_DATA
            } else {
                BaseControlPoint.CONTROL_TYPE_DATA
            }
        }
        updateElement(renderer, delegate, reason) {
            textProperty.block()//do
            updatePaint()
            updateOriginText(textProperty.text, keepVisibleSize)//重新计算宽高
        }
    }

    /**更新字体*/
    fun updatePaintTypeface(
        typeface: Typeface?,
        renderer: BaseRenderer?,
        delegate: CanvasRenderDelegate?,
    ) {
        updateTextProperty(renderer, delegate) {
            val typefaceInfo = if (typeface == null) {
                FontManager.getSystemFontList().firstOrNull()
            } else {
                FontManager.loadTypefaceInfo(typeface)
            }
            fontFamily = typefaceInfo?.name
        }
    }

    /**更新文本样式*/
    fun updateTextStyle(
        style: Int,
        enable: Boolean,
        renderer: BaseRenderer?,
        delegate: CanvasRenderDelegate?
    ) {
        renderer ?: return
        updateTextProperty(renderer, delegate) {
            when (style) {
                TEXT_STYLE_BOLD -> isFakeBoldText = enable
                TEXT_STYLE_ITALIC -> isItalic = enable
                TEXT_STYLE_UNDER_LINE -> isUnderlineText = enable
                TEXT_STYLE_DELETE_LINE -> isStrikeThruText = enable
            }
        }
    }

    /**是否有指定的文本样式*/
    fun haveTextStyle(style: Int): Boolean {
        return when (style) {
            TEXT_STYLE_BOLD -> textProperty.isFakeBoldText
            TEXT_STYLE_ITALIC -> textProperty.isItalic
            TEXT_STYLE_UNDER_LINE -> textProperty.isUnderlineText
            TEXT_STYLE_DELETE_LINE -> textProperty.isStrikeThruText
            else -> false
        }
    }

    //endregion---操作---

    //region---core---

    protected val _deleteLineRect = RectF()
    protected val _underLineRect = RectF()

    //每行的文本宽度列表
    protected val _textWidthList = mutableListOf<Float>()

    //每行的文本高度列表
    protected val _textHeightList = mutableListOf<Float>()

    /**绘制普通文本*/
    protected fun drawNormalText(canvas: Canvas, paint: Paint = this.paint) {
        val oldUnderLine = paint.isUnderlineText
        val oldDeleteLine = paint.isStrikeThruText

        //因为是自己一个一个绘制的, 所以删除线和下划线也需要手绘
        paint.isUnderlineText = false
        paint.isStrikeThruText = false

        val lineTextList = textProperty.text.lineTextList()
        val lineSize = lineTextList.size() //几行文本

        var x = 0f
        var y = 0f

        //删除线的宽度
        val lineWidth = paint.strokeWidth

        _textWidthList.clear()
        _textHeightList.clear()

        var maxLineWidth = 0f
        var maxLineHeight = 0f
        for (lineText in lineTextList) {
            val lineTextWidth = calcLineTextWidth(lineText)
            val lineTextHeight = calcLineTextHeight(lineText)

            maxLineWidth = max(maxLineWidth, lineTextWidth)
            maxLineHeight = max(maxLineHeight, lineTextHeight)

            _textWidthList.add(lineTextWidth)
            _textHeightList.add(lineTextHeight)
        }

        if (textProperty.orientation == LinearLayout.HORIZONTAL) {
            lineTextList.forEachIndexed { index, lineText ->
                val lineTextWidth = _textWidthList[index]
                val lineTextHeight = _textHeightList[index]

                //多行文本对齐方式的x偏移量
                var lineOffsetX = 0f
                if (lineSize > 1) {
                    lineOffsetX = when (textProperty.textAlign) {
                        TEXT_ALIGN_CENTER -> (maxLineWidth - lineTextWidth) / 2
                        TEXT_ALIGN_RIGHT -> maxLineWidth - lineTextWidth
                        else -> 0f
                    }
                }

                val descent = measureTextDescent(lineText)

                val lineHeight = lineTextHeight / 10
                _deleteLineRect.set(
                    lineOffsetX + x + lineWidth / 2,
                    y + lineTextHeight / 2 - lineHeight / 2,
                    lineOffsetX + lineTextWidth - lineWidth / 2,
                    y + lineTextHeight / 2 + lineHeight / 2
                )
                _underLineRect.set(
                    lineOffsetX + x + lineWidth / 2,
                    y + lineTextHeight - lineHeight,
                    lineOffsetX + lineTextWidth - lineWidth / 2,
                    y + lineTextHeight
                )

                y += lineTextHeight

                //逐字绘制
                lineText.forEach { char ->
                    val text = "$char"
                    val charWidth = measureTextWidth(text)

                    val offsetX = when (paint.textAlign) {
                        Paint.Align.RIGHT -> charWidth - _skewWidth
                        Paint.Align.CENTER -> charWidth / 2 - _textMeasureBounds.left / 2 - _skewWidth / 2
                        else -> -_textMeasureBounds.left.toFloat()
                    }

                    canvas.drawText(text, lineOffsetX + x + offsetX, y - descent, paint)
                    x += charWidth + textProperty.charSpacing
                }

                //删除线
                if (oldDeleteLine) {
                    canvas.drawRect(_deleteLineRect, paint)
                }
                //下划线
                if (oldUnderLine) {
                    canvas.drawRect(_underLineRect, paint)
                }

                y += textProperty.lineSpacing
                x = 0f
            }
        } else {
            lineTextList.forEachIndexed { index, lineText ->
                val lineTextWidth = _textWidthList[index]
                val lineTextHeight = _textHeightList[index]

                //多行文本对齐方式的y偏移量
                var lineOffsetY = 0f
                if (lineSize > 1) {
                    lineOffsetY = when (textProperty.textAlign) {
                        TEXT_ALIGN_CENTER -> (maxLineHeight - lineTextHeight) / 2
                        TEXT_ALIGN_RIGHT -> maxLineHeight - lineTextHeight
                        else -> 0f
                    }
                }

                //逐字绘制
                lineText.forEach { char ->
                    val text = "$char"
                    val charWidth = measureTextWidth(text)
                    val charHeight = measureTextHeight(text)
                    val descent = measureTextDescent(text)
                    val textBounds = paint.textBounds(text)

                    val offsetX = if (textProperty.isCompactText) {
                        when (paint.textAlign) {
                            Paint.Align.RIGHT -> lineTextWidth - _skewWidth /*+ textBounds.left.toFloat()*/
                            Paint.Align.CENTER -> lineTextWidth / 2 - _skewWidth / 2
                            else -> -textBounds.left.toFloat()
                        }
                    } else {
                        when (paint.textAlign) {
                            Paint.Align.RIGHT -> lineTextWidth - _skewWidth.toInt()
                            Paint.Align.CENTER -> lineTextWidth / 2 - _skewWidth.toInt() / 2
                            else -> 0f
                        }
                    }

                    val lineHeight = charHeight / 10
                    //删除线
                    if (oldDeleteLine) {
                        _deleteLineRect.set(
                            x + lineWidth / 2,
                            lineOffsetY + y + charHeight / 2 - lineHeight / 2,
                            x + charWidth - lineWidth / 2,
                            lineOffsetY + y + charHeight / 2 + lineHeight / 2
                        )

                        canvas.drawRect(_deleteLineRect, paint)
                    }
                    //下划线
                    if (oldUnderLine) {
                        _underLineRect.set(
                            x + lineWidth / 2,
                            lineOffsetY + y + charHeight - lineHeight,
                            x + charWidth - lineWidth / 2,
                            lineOffsetY + y + charHeight
                        )
                        canvas.drawRect(_underLineRect, paint)
                    }

                    y += charHeight
                    canvas.drawText(text, x + offsetX, lineOffsetY + y - descent, paint)
                    y += textProperty.charSpacing
                }

                x += lineTextWidth + textProperty.lineSpacing
                y = 0f
            }
        }

        paint.isUnderlineText = oldUnderLine
        paint.isStrikeThruText = oldDeleteLine
    }

    /**计算多行文本的宽度*/
    @Pixel
    protected fun calcLineTextWidth(text: String?, paint: Paint = this.paint): Float {
        var result = 0f
        val lineTextList = text.lineTextList()
        if (textProperty.orientation == LinearLayout.HORIZONTAL) {
            //横向排列
            lineTextList.forEach { lineText ->
                var lineWidth = 0f
                lineText.forEach {
                    //一个字一个字的宽度
                    lineWidth += measureTextWidth("$it", paint)
                }
                lineWidth += textProperty.charSpacing * (lineText.length - 1)
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
            result += textProperty.lineSpacing * (lineTextList.size - 1)
        }
        return result
    }

    /**计算多行文本的高度*/
    @Pixel
    protected fun calcLineTextHeight(text: String?, paint: Paint = this.paint): Float {
        var result = 0f
        val lineTextList = text.lineTextList()
        if (textProperty.orientation == LinearLayout.HORIZONTAL) {
            //横向排列
            lineTextList.forEach { lineText ->
                result += measureTextHeight(lineText, paint)
            }
            result += textProperty.lineSpacing * (lineTextList.size - 1)
        } else {
            //纵向排列
            lineTextList.forEach { lineText ->
                var lineHeight = 0f
                lineText.forEach {
                    //一个字一个字的高度
                    lineHeight += measureTextHeight("$it", paint)
                }
                lineHeight += textProperty.charSpacing * (lineText.length - 1)
                result = max(result, lineHeight)
            }
        }
        return result
    }

    //temp
    protected val _textMeasureBounds = Rect()

    protected var _skewWidth: Float = 0f

    /**单行文本字符的宽度*/
    protected fun measureTextWidth(text: String, paint: Paint = this.paint): Float {
        if (textProperty.isCompactText) {
            if (text.isBlank()) {
                //空格
                val textWidth = paint.textWidth(text).toInt()
                _textMeasureBounds.set(0, 0, textWidth, textWidth)
            } else {
                paint.textBounds(text, _textMeasureBounds)
            }
        }
        val textWidth = if (textProperty.isCompactText) {
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
    protected fun measureTextHeight(text: String, paint: Paint = this.paint): Float {
        if (textProperty.isCompactText) {
            if (text.isBlank()) {
                //空格
                val textWidth = paint.textWidth(text).toInt()
                _textMeasureBounds.set(0, 0, textWidth, textWidth)
            } else {
                paint.textBounds(text, _textMeasureBounds)
            }
        }
        return if (textProperty.isCompactText) {
            _textMeasureBounds.height().toFloat()
        } else {
            paint.textHeight()
        }
    }

    /**下沉的距离*/
    protected fun measureTextDescent(text: String, paint: Paint = this.paint): Float {
        return if (textProperty.isCompactText) {
            paint.textBounds(text).bottom.toFloat()
        } else {
            paint.descent()
        }
    }

    //endregion---core---

}

//---

/**对齐方式*/
fun Paint.Align.toAlignString(): String = when (this) {
    Paint.Align.CENTER -> TextElement.TEXT_ALIGN_CENTER
    Paint.Align.LEFT -> TextElement.TEXT_ALIGN_LEFT
    Paint.Align.RIGHT -> TextElement.TEXT_ALIGN_RIGHT
    else -> TextElement.TEXT_ALIGN_LEFT
}

fun String?.toPaintAlign(): Paint.Align = when (this) {
    TextElement.TEXT_ALIGN_CENTER -> Paint.Align.CENTER
    TextElement.TEXT_ALIGN_LEFT -> Paint.Align.LEFT
    TextElement.TEXT_ALIGN_RIGHT -> Paint.Align.RIGHT
    else -> Paint.Align.LEFT
}