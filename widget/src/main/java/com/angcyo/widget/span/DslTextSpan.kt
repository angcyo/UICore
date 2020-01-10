package com.angcyo.widget.span

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.text.style.ReplacementSpan
import android.view.Gravity
import androidx.annotation.ColorInt
import androidx.annotation.Px
import com.angcyo.library.ex.undefined_color
import com.angcyo.library.ex.undefined_int

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/08
 */

open class DslTextSpan : ReplacementSpan() {

    val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

    @Px
    var textSize: Float = -1f

    @ColorInt
    var textColor: Int = undefined_color

    var textGravity: Int = Gravity.LEFT or Gravity.BOTTOM

    /**需要替换显示的文本*/
    var showText: CharSequence? = null

    /**强制指定宽度*/
    var spanWidth: Int = undefined_int
    var spanHeight: Int = undefined_int

    /**背景[Drawable]*/
    var backgroundDrawable: Drawable? = null
        set(value) {
            field = value
            field?.apply {
                if (bounds.isEmpty) {
                    setBounds(0, 0, minimumWidth, minimumHeight)
                }
            }
        }

    /**前景[Drawable]*/
    var foregroundDrawable: Drawable? = null
        set(value) {
            field = value
            field?.apply {
                if (bounds.isEmpty) {
                    setBounds(0, 0, minimumWidth, minimumHeight)
                }
            }
        }

    //影响宽度, 背景偏移, 文本偏移
    var marginLeft: Int = 0
    var marginRight: Int = 0
    var marginTop: Int = 0
    var marginBottom: Int = 0

    //影响宽度, 影响文本与背景的距离
    var paddingLeft: Int = 0
    var paddingRight: Int = 0
    var paddingTop: Int = 0
    var paddingBottom: Int = 0

    //整体偏移
    var offsetX: Float = 0f
    var offsetY: Float = 0f

    //单独文本偏移
    var textOffsetX: Float = 0f
    var textOffsetY: Float = 0f

    fun _initPaint(paint: Paint) {
        textPaint.set(paint)
        if (textSize > 0) {
            textPaint.textSize = textSize
        }
        if (textColor != undefined_color) {
            textPaint.color = textColor
        }
    }

    fun _targetText(
        text: CharSequence?,
        start: Int,
        end: Int
    ): CharSequence {
        return showText?.run { this } ?: text?.subSequence(start, end) ?: ""
    }

    fun _drawableWidth(drawable: Drawable?): Int {
        return drawable?.run { if (bounds.isEmpty) if (bounds.left == -1) -1 else minimumWidth else bounds.width() }
            ?: 0
    }

    fun _drawableHeight(drawable: Drawable?): Int {
        return drawable?.run { if (bounds.isEmpty) if (bounds.top == -1) -1 else minimumHeight else bounds.height() }
            ?: 0
    }

    /**高度包含 marigin padding , 宽度不包含*/
    fun _measureSize(
        paint: Paint,
        text: CharSequence?,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt? = null
    ): IntArray {
        _initPaint(paint)

        val targetText = _targetText(text, start, end)

        val textWidth = textPaint.measureText(targetText, 0, targetText.length).toInt()

        val bgWidth = _drawableWidth(backgroundDrawable)
        val fgWidth = _drawableWidth(foregroundDrawable)

        val bgHeight = _drawableHeight(backgroundDrawable)
        val fgHeight = _drawableHeight(foregroundDrawable)

        val height: Int

        if (fm != null) {
            fm.ascent =
                if (spanHeight > 0) -spanHeight else minOf(
                    textPaint.ascent().toInt(),
                    -bgHeight,
                    -fgHeight
                ) - paddingTop - paddingBottom - marginTop - marginBottom
            fm.descent = textPaint.descent().toInt()

            //决定高度
            fm.top = fm.ascent
            //基线下距离
            fm.bottom = fm.descent

            height = fm.descent - fm.ascent
        } else {
            height = if (spanHeight > 0) spanHeight else maxOf(
                (textPaint.descent() - textPaint.ascent()).toInt(),
                bgHeight,
                fgHeight
            ) + paddingTop + paddingBottom + marginTop + marginBottom
        }

        return intArrayOf(
            if (spanWidth > 0) spanWidth else maxOf(textWidth, bgWidth, fgWidth),
            height
        )
    }

    override fun getSize(
        paint: Paint,
        text: CharSequence?,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        val measureSize = _measureSize(paint, text, start, end, fm)
        return measureSize[0] + marginLeft + marginRight + paddingLeft + paddingRight
    }

    override fun draw(
        canvas: Canvas,
        text: CharSequence?,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,//基线位置
        bottom: Int,//底部位置
        paint: Paint
    ) {
        val measureSize = _measureSize(paint, text, start, end)
        val measureWidth = measureSize[0]
        val measureHeight = measureSize[1] - marginTop - marginBottom
        val targetText = _targetText(text, start, end)

        canvas.save()

        //绘制文本
        val textWidth = textPaint.measureText(targetText, 0, targetText.length).toInt()
        val textHeight = textPaint.descent() - textPaint.ascent()

        //偏移画布
        canvas.translate(marginLeft + offsetX, offsetY)

        val layoutDirection = 0
        val absoluteGravity = Gravity.getAbsoluteGravity(textGravity, layoutDirection)
        val verticalGravity = textGravity and Gravity.VERTICAL_GRAVITY_MASK
        val horizontalGravity = absoluteGravity and Gravity.HORIZONTAL_GRAVITY_MASK

        val textX: Float = when (horizontalGravity) {
            Gravity.CENTER_HORIZONTAL -> x + measureWidth / 2 - textWidth / 2 + paddingLeft
            Gravity.RIGHT -> x + measureWidth - textWidth - paddingRight
            else -> x + paddingLeft
        }

        val textY: Float = when (verticalGravity) {
            Gravity.CENTER_VERTICAL -> top + measureHeight - (measureHeight - textHeight) / 2 - textPaint.descent() / 2 + marginTop
            Gravity.BOTTOM -> (y - marginBottom - paddingBottom).toFloat()
            else -> top - textPaint.ascent() - marginTop
        }

        fun drawDrawable(drawable: Drawable?) {
            //空白文本, drawable将采用measure的size当做wrap_content
            val blankText = targetText.isBlank()
            drawable?.let {
                val height = _drawableHeight(it).other(
                    measureHeight,
                    if (blankText) measureHeight else textHeight.toInt()
                )

                val width = _drawableWidth(it).other(
                    measureWidth,
                    if (blankText) measureHeight else textWidth
                )

                val textCenterX = textX + textWidth / 2
                val textCenterY = textY + textPaint.ascent() / 2

                val l: Int = (textCenterX - width / 2).toInt()
                val t: Int = (textCenterY - height / 2 + textPaint.descent() / 2).toInt()

                it.setBounds(
                    l - paddingLeft,
                    t - paddingTop,
                    l + width + paddingRight,
                    t + height + paddingBottom
                )
                it.draw(canvas)
            }
        }

        //绘制背景
        drawDrawable(backgroundDrawable)

        canvas.drawText(
            targetText,
            0,
            targetText.length,
            textX + textOffsetX,
            textY + textOffsetY,
            textPaint
        )

        //绘制前景
        drawDrawable(foregroundDrawable)

        canvas.restore()
    }

    fun Int.other(max: Int, min: Int): Int {
        return when {
            //MATCH_PARENT
            this == -1 -> max
            //WRAP_CONTENT
            this <= 0 -> min
            //EXACTLY
            else -> this
        }
    }

    fun paddingHorizontal(padding: Int) {
        paddingLeft = padding
        paddingRight = padding
    }

    fun paddingVertical(padding: Int) {
        paddingTop = padding
        paddingBottom = padding
    }

    fun marginHorizontal(margin: Int) {
        marginLeft = margin
        marginRight = margin
    }

    fun marginVertical(margin: Int) {
        marginTop = margin
        marginBottom = margin
    }
}