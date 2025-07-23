package com.angcyo.canvas.render.element

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.widget.LinearLayout
import androidx.core.graphics.withClip
import com.angcyo.canvas.render.core.CanvasRenderDelegate
import com.angcyo.canvas.render.core.component.BaseControlPoint
import com.angcyo.canvas.render.data.CharDrawInfo
import com.angcyo.canvas.render.data.RenderParams
import com.angcyo.canvas.render.data.TextProperty
import com.angcyo.canvas.render.data.toLineCharDrawInfoList
import com.angcyo.canvas.render.renderer.BaseRenderer
import com.angcyo.canvas.render.state.IStateStack
import com.angcyo.canvas.render.state.TextStateStack
import com.angcyo.library.annotation.Pixel
import com.angcyo.library.canvas.core.Reason
import com.angcyo.library.component.FontManager
import com.angcyo.library.component.SupportUndo
import com.angcyo.library.component.hawk.LibHawkKeys
import com.angcyo.library.ex.forEachBreak
import com.angcyo.library.ex.forEachBreakIndexed
import com.angcyo.library.ex.have
import com.angcyo.library.ex.size
import com.angcyo.library.ex.textBounds
import com.angcyo.library.ex.textHeight
import com.angcyo.library.ex.textWidth
import com.angcyo.library.ex.toColor
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

        /**
         * [TEXT_STYLE_BOLD]
         * [TEXT_STYLE_ITALIC]
         * [TEXT_STYLE_UNDER_LINE]
         * [TEXT_STYLE_DELETE_LINE]
         * */
        fun updatePaintStyle(paint: Paint, style: Int) {
            paint.let {
                //删除线
                it.isStrikeThruText = style.have(TEXT_STYLE_DELETE_LINE)
                //下划线
                it.isUnderlineText = style.have(TEXT_STYLE_UNDER_LINE)
                it.isFakeBoldText = style.have(TEXT_STYLE_BOLD)
                it.textSkewX = if (style.have(TEXT_STYLE_ITALIC)) ITALIC_SKEW else 0f //倾斜
            }
        }
    }

    //region---属性---

    /**文本属性*/
    var textProperty: TextProperty = TextProperty()

    /**是否已经是曲线文本*/
    val isCurveText: Boolean
        get() = textProperty.curvature != 0f && textProperty.text.singleText().size() > 1

    /**当前属性下, 是否支持曲线文本功能
     * 目前只支持水平方向的文本
     * */
    val isSupportCurve: Boolean
        get() {
            val text = textProperty.text
            return !text.isNullOrEmpty() /*|| text.have("\\n")*/
        }

    /**当前样式下, 是否支持行距*/
    val isSupportLineSpacing: Boolean
        get() {
            val text = textProperty.text
            return !(text.isNullOrEmpty() || !text.have("\\n"))
        }

    /**是否开启调试*/
    private val isDebug = false ///BuildConfig.BUILD_TYPE.isDebugType()

    //endregion---属性---

    /**获取每一行的文本*/
    protected fun String?.lineTextList(): List<String> = this?.lines() ?: emptyList()

    /**将多行文本转换成单行*/
    protected fun String?.singleText(): String? = this?.replace("\n", "")

    override fun createStateStack(): IStateStack = TextStateStack()

    override fun onRenderInside(renderer: BaseRenderer?, canvas: Canvas, params: RenderParams) {
        updatePaint(params)
        val renderMatrix = params._renderMatrix
        canvas.concat(renderMatrix)
        if (textProperty.curvature == 0f) {
            drawNormalText(canvas)
        } else {
            drawCurveText(canvas)
        }
    }

    /**更新画笔样式*/
    open fun updatePaint(params: RenderParams?) {
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

        val curveTextDraw = curveTextDrawInfo
        val newWidth: Float
        val newHeight: Float
        if (curveTextDraw == null) {
            //没有曲线绘制信息

            newWidth = calcLineTextWidth(text)
            newHeight = calcLineTextHeight(text)
        } else {
            //曲线
            newWidth = curveTextDraw.curveTextWidth
            newHeight = curveTextDraw.curveTextHeight
        }

        updateRenderWidthHeight(newWidth, newHeight, keepVisibleSize)
    }

    /**更新曲率, 并且保持可视化的效果不突兀
     * [updateTextProperty]*/
    open fun updateCurvature(
        curvature: Float,
        renderer: BaseRenderer?,
        delegate: CanvasRenderDelegate?
    ) {
        val oldCurvature = textProperty.curvature
        textProperty.curvature = curvature

        val oldWidth = renderProperty.width
        val oldHeight = renderProperty.height

        updateOriginText(textProperty.text, false)

        val newWidth = renderProperty.width
        val newHeight = renderProperty.height

        renderProperty.anchorX -= (newWidth - oldWidth) / 2
        if (curvature < 0) {
            //上弧, 保证顶部不变
            renderProperty.anchorY = if (oldCurvature >= 0) {
                //从正曲线到负曲线
                renderProperty.anchorY - (newHeight - (curveTextDrawInfo?.textHeight ?: 0f))
            } else {
                //从负曲线到负曲线
                renderProperty.anchorY
            }
        } else {
            //下弧, 保证底部不变
            renderProperty.anchorY = if (oldCurvature >= 0) {
                //从正曲线
                renderProperty.anchorY - (newHeight - oldHeight)
            } else {
                //从负曲线到正曲线
                renderProperty.anchorY + (oldHeight - (curveTextDrawInfo?.textHeight ?: 0f))
            }
        }

        //notify
        val reason: Reason = Reason.user.apply {
            controlType = BaseControlPoint.CONTROL_TYPE_DATA
        }
        renderer?.requestUpdatePropertyFlag(reason, delegate)
    }

    /**更新文本属性, 并且自动回退*/
    @SupportUndo
    fun updateTextProperty(
        renderer: BaseRenderer?,
        delegate: CanvasRenderDelegate?,
        keepVisibleSize: Boolean = false,
        reason: Reason = Reason.user.apply {
            controlType = BaseControlPoint.CONTROL_TYPE_DATA
        },
        block: TextProperty.() -> Unit
    ) {
        updateElementAction(renderer, delegate, reason) {
            textProperty.block()//do
            onUpdateTextPropertyAfter(renderer)
            updatePaint(null)
            updateOriginText(textProperty.text, keepVisibleSize)//重新计算宽高
        }
    }

    /**当[updateTextProperty]更新文本之后*/
    open fun onUpdateTextPropertyAfter(renderer: BaseRenderer?) {

    }

    /**更新字体*/
    @SupportUndo
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
    @SupportUndo
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

    /**获取描述文本的宽度, 支持多行*/
    fun getTextWidth() = calcLineTextWidth()

    /**获取描述文本的高度, 支持多行*/
    fun getTextHeight() = calcLineTextHeight()

    //endregion---操作---

    //region---core---

    protected val _deleteLineRect = RectF()
    protected val _underLineRect = RectF()

    //每行的文本宽度列表
    protected val _textWidthList = mutableListOf<Float>()

    //每行的文本高度列表
    protected val _textHeightList = mutableListOf<Float>()

    //抹平结构
    protected val _charDrawInfoList = mutableListOf<CharDrawInfo>()

    //一行一行的结构
    protected val _lineCharDrawInfoList = mutableListOf<List<CharDrawInfo>>()

    /**曲线文本*/
    val curveTextDrawInfo: CurveTextDraw?
        get() {
            if (textProperty.curvature == 0f) return null
            val text = textProperty.text ?: return null
            val textWidth = calcLineTextWidth(text)
            val textHeight = calcLineTextHeight(text)
            return CurveTextDraw.create(
                getCharDrawInfoList(),
                textProperty.curvature,
                textWidth,
                textHeight,
            )
        }

    /**绘制曲线文本*/
    protected fun drawCurveText(canvas: Canvas, paint: Paint = this.paint) {
        curveTextDrawInfo?.apply {
            canvas.withClip(0f, 0f, curveTextWidth, curveTextHeight) {
                draw(canvas, paint)
            }
        }
    }

    /**绘制普通文本*/
    protected fun drawNormalText(canvas: Canvas, paint: Paint = this.paint) {
        val charList = getCharDrawInfoList()

        //删除线和下划线的回执
        val oldUnderLine = paint.isUnderlineText
        val oldDeleteLine = paint.isStrikeThruText

        //因为是自己一个一个绘制的, 所以删除线和下划线也需要手绘
        paint.isUnderlineText = false
        paint.isStrikeThruText = false

        //文本的绘制
        charList.forEach { charInfo ->
            if (isDebug) {
                paint.color = Color.BLACK
            }
            canvas.drawText(
                charInfo.char,
                charInfo.bounds.left + charInfo.charDrawOffsetX,
                charInfo.bounds.bottom - charInfo.lineDescent + charInfo.charDrawOffsetY,
                paint
            )
            if (isDebug) {
                paint.color = Color.RED
                canvas.drawRect(charInfo.bounds, paint)
            }
        }

        //删除线和下划线的绘制
        _lineCharDrawInfoList.forEach { lineCharList ->
            if (lineCharList.isNotEmpty()) {
                val first = lineCharList.first()
                val last = lineCharList.last()
                val lineTextWidth = first.lineWidth
                val lineTextHeight = first.lineHeight

                val lineWidth = lineTextWidth / LibHawkKeys.canvasLineHeight //删除线的宽度
                val lineHeight = lineTextHeight / LibHawkKeys.canvasLineHeight //删除线的高度

                if (textProperty.orientation == LinearLayout.HORIZONTAL) {
                    val lineLeft = first.bounds.left
                    val lineRight = last.bounds.right
                    val lineCenterY = first.bounds.top + lineTextHeight / 2
                    val lineBottom = first.bounds.bottom

                    if (oldDeleteLine) {
                        _deleteLineRect.set(
                            lineLeft,
                            lineCenterY - lineHeight / 2,
                            lineRight,
                            lineCenterY + lineHeight / 2
                        )
                        canvas.drawRect(_deleteLineRect, paint)
                    }
                    if (oldUnderLine) {
                        _underLineRect.set(
                            lineLeft,
                            lineBottom - lineHeight,
                            lineRight,
                            lineBottom
                        )
                        canvas.drawRect(_underLineRect, paint)
                    }
                } else {
                    //垂直方向
                    val lineLeft = first.bounds.left
                    val lineTop = first.bounds.top
                    val lineBottom = last.bounds.bottom
                    val lineCenterX = first.bounds.left + lineTextWidth / 2

                    if (oldDeleteLine) {
                        _deleteLineRect.set(
                            lineCenterX - lineWidth / 2,
                            lineTop,
                            lineCenterX + lineWidth / 2,
                            lineBottom
                        )
                        canvas.drawRect(_deleteLineRect, paint)
                    }
                    if (oldUnderLine) {
                        _underLineRect.set(
                            lineLeft,
                            lineTop,
                            lineLeft + lineWidth,
                            lineBottom
                        )
                        canvas.drawRect(_underLineRect, paint)
                    }
                }
            }
        }

        paint.isUnderlineText = oldUnderLine
        paint.isStrikeThruText = oldDeleteLine

        /*val lineTextList = textProperty.text.lineTextList()

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

        //---

        val oldUnderLine = paint.isUnderlineText
        val oldDeleteLine = paint.isStrikeThruText

        //因为是自己一个一个绘制的, 所以删除线和下划线也需要手绘
        paint.isUnderlineText = false
        paint.isStrikeThruText = false

        val lineSize = lineTextList.size() //几行文本

        //删除线的宽度
        val lineWidth = paint.strokeWidth

        var x = 0f
        var y = 0f
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

                    canvas.drawText(text, max(0f, lineOffsetX + x + offsetX), y - descent, paint)
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
                            Paint.Align.RIGHT -> lineTextWidth - _skewWidth *//*+ textBounds.left.toFloat()*//*
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
                    y = max(y, 0f)
                }

                x += lineTextWidth + textProperty.lineSpacing
                y = 0f
            }
        }

        paint.isUnderlineText = oldUnderLine
        paint.isStrikeThruText = oldDeleteLine*/
    }

    /**获取每个字符绘制的信息*/
    fun getCharDrawInfoList(): List<CharDrawInfo> {
        _textWidthList.clear()
        _textHeightList.clear()

        val lineTextList = textProperty.text.lineTextList()
        val lineSize = lineTextList.size() //几行文本

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

        val result = _charDrawInfoList
        result.clear()

        var x = 0f
        var y = 0f

        if (textProperty.orientation == LinearLayout.HORIZONTAL) {
            lineTextList.forEachIndexed { lineIndex, lineText ->
                val lineTextWidth = _textWidthList[lineIndex]
                val lineTextHeight = _textHeightList[lineIndex]

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
                y = max(0f, y) + lineTextHeight

                //逐字绘制
                lineText.forEachBreakIndexed { columnIndex, char ->
                    val text = char
                    val charWidth = measureTextWidth(text)
                    val charHeight = measureTextHeight(text)

                    val offsetX = when (paint.textAlign) {
                        Paint.Align.RIGHT -> charWidth - _skewWidth
                        Paint.Align.CENTER -> charWidth / 2 - _textMeasureBounds.left / 2 - _skewWidth / 2
                        else -> -_textMeasureBounds.left.toFloat()
                    }

                    val left = max(0f, x) + lineOffsetX
                    val top = y - lineTextHeight
                    result.add(
                        CharDrawInfo(
                            text,
                            charWidth,
                            charHeight,
                            RectF(left, top, left + charWidth, top + lineTextHeight),
                            offsetX,
                            0f,
                            columnIndex,
                            lineIndex,
                            textProperty.orientation,
                            lineTextWidth,
                            lineTextHeight,
                            descent
                        )
                    )
                    x += charWidth + textProperty.charSpacing
                }

                y += textProperty.lineSpacing
                x = 0f
            }
        } else {
            lineTextList.forEachIndexed { lineIndex, lineText ->
                val lineTextWidth = _textWidthList[lineIndex]
                val lineTextHeight = _textHeightList[lineIndex]

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
                lineText.forEachIndexed { columnIndex, char ->
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

                    y += charHeight

                    val left = max(0f, x)
                    val top = lineOffsetY + y - charHeight
                    result.add(
                        CharDrawInfo(
                            text,
                            charWidth,
                            charHeight,
                            RectF(left, top, left + charWidth, top + charHeight),
                            offsetX,
                            0f,
                            columnIndex,
                            lineIndex,
                            textProperty.orientation,
                            lineTextWidth,
                            lineTextHeight,
                            descent
                        )
                    )

                    y += textProperty.charSpacing
                    y = max(y, 0f)
                }

                x += lineTextWidth + textProperty.lineSpacing
                y = 0f
            }
        }

        //end
        result.toLineCharDrawInfoList(_lineCharDrawInfoList)

        return result
    }

    /**计算多行文本的宽度*/
    @Pixel
    protected fun calcLineTextWidth(
        text: String? = textProperty.text,
        paint: Paint = this.paint
    ): Float {
        var result = 0f
        val lineTextList = text.lineTextList()
        if (textProperty.orientation == LinearLayout.HORIZONTAL) {
            //横向排列
            lineTextList.forEach { lineText ->
                var lineWidth = 0f //一行的宽度
                var maxCharWidth = 0f//一行中最大的字符宽度
                lineText.forEachBreak {
                    //一个字一个字的宽度
                    val charWidth = measureTextWidth(it, paint)
                    lineWidth += charWidth
                    maxCharWidth = max(maxCharWidth, charWidth)
                }
                lineWidth += textProperty.charSpacing * (lineText.length - 1)
                result = max(result, max(lineWidth, maxCharWidth))
            }
        } else {
            //纵向排列
            lineTextList.forEach { lineText ->
                var lineMax = 0f
                lineText.forEachBreak {
                    lineMax = max(measureTextWidth(it, paint), lineMax)
                }
                result += lineMax
            }
            result += textProperty.lineSpacing * (lineTextList.size - 1)
        }
        return result
    }

    /**计算多行文本的高度*/
    @Pixel
    protected fun calcLineTextHeight(
        text: String? = textProperty.text,
        paint: Paint = this.paint
    ): Float {
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
                var lineHeight = 0f //一行的高度
                var maxLineHeight = 0f //一行中最大的字符高度
                lineText.forEachBreak {
                    //一个字一个字的高度
                    val charHeight = measureTextHeight(it, paint)
                    lineHeight += charHeight
                    maxLineHeight = max(maxLineHeight, charHeight)
                }
                lineHeight += textProperty.charSpacing * (lineText.length - 1)
                result = max(result, max(lineHeight, maxLineHeight))
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
                val textWidth = LibHawkKeys.spaceTextWidth ?: paint.textWidth(text).toInt()
                _textMeasureBounds.set(0, 0, textWidth, textWidth)
            } else {
                paint.textBounds(text, _textMeasureBounds)
            }
        } else {
            _textMeasureBounds.setEmpty()
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
        } else {
            _textMeasureBounds.setEmpty()
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