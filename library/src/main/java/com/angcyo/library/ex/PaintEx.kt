package com.angcyo.library.ex

import android.graphics.*
import android.os.Build
import android.os.Build.VERSION_CODES
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.angcyo.library.R
import com.angcyo.library.annotation.Pixel
import kotlin.math.max


/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/20
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

fun paint() = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
    isFilterBitmap = true
    style = Paint.Style.FILL
    textSize = 12 * dp
    color = _color(R.color.text_primary_color)
}

/**创建一个画笔*/
fun createPaint(color: Int = Color.BLACK, style: Paint.Style = Paint.Style.STROKE) =
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        this.style = style
        strokeWidth = 1f
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

fun createTextPaint(color: Int = Color.BLACK, textSize: Float = 12 * dp) =
    TextPaint(createPaint(color, Paint.Style.FILL)).apply {
        this.textSize = textSize
        this.textAlign = Paint.Align.LEFT
    }

/**文本的宽度*/
fun Paint.textWidth(text: String?): Float {
    if (text == null) {
        return 0f
    }
    return measureText(text)
}

/**文本的高度
 * [textBounds]*/
fun Paint?.textHeight(): Float = this?.run { descent() - ascent() } ?: 0f

/**文本绘制的高度, 在绘制文本时top-此高度值, 就是文本绘制时的左下角坐标*/
fun Paint?.textDrawHeight(): Float = this?.run { -ascent() } ?: 0f

/**文本绘制时的y坐标
 * [originY]原本y坐标¬*/
fun Paint.drawTextY(originY: Float): Float {
    return originY - descent()
}

/**获取文本的边界, 包含所有文本的最小矩形*/
fun Paint.textBounds(text: CharSequence?, result: Rect = Rect(0, 0, 0, 0)): Rect {
    if (text == null) {
        return result
    }
    if (Build.VERSION.SDK_INT >= VERSION_CODES.Q) {
        getTextBounds(text, 0, text.length, result)
    } else {
        getTextBounds(text.toString(), 0, text.length, result)
    }
    return result
}

/**获取文本描边的[Path]*/
fun Paint.textPath(text: String?, result: Path = Path()): Path {
    if (text == null) {
        return result
    }
    val textBounds = textBounds(text)
    getTextPath(
        text,
        0,
        text.length,
        -textBounds.left.toFloat(),
        -textBounds.top.toFloat(),
        result
    )
    return result
}

/**在指定的高度中, 查找最大的字体大小
 * 使用[TextPaint]测量高度, 所有传入的[height]应该也是使用[TextPaint]测量出来的*/
@Pixel
fun Paint.findNewTextSize(height: Float, minTextSize: Float = 1f): Float {

    //calc
    fun getTextHeight(
        paint: TextPaint,
        textSize: Float
    ): Float {
        paint.textSize = textSize
        return paint.textHeight()
    }

    //---
    val textPaint = TextPaint(this)
    var targetTextSize: Float = textPaint.textSize
    var textHeight = getTextHeight(textPaint, targetTextSize)
    while (textHeight > height && targetTextSize > minTextSize) {
        targetTextSize = max(targetTextSize - 1, minTextSize)
        textHeight = getTextHeight(textPaint, targetTextSize)
    }
    return targetTextSize
}

/**获取一个字体大小, 在指定的宽高中
 * 使用[StaticLayout]测量高度, 所有传入的[width] [height]应该也是使用[StaticLayout]测量出来的
 * */
@Pixel
fun Paint.findNewTextSizeByLayout(
    text: CharSequence?,
    width: Int = Int.MAX_VALUE,
    height: Int = Int.MAX_VALUE,
    minTextSize: Float = 1f,
    spacingMult: Float = 1f, //Text view line spacing multiplier
    spacingAdd: Float = 0f     //Text view additional line spacing
): Float {

    //calc
    fun getTextHeight(
        source: CharSequence,
        paint: TextPaint,
        width: Int,
        textSize: Float
    ): Int {
        paint.textSize = textSize
        val layout = StaticLayout(
            source,
            paint,
            width,
            Layout.Alignment.ALIGN_NORMAL,
            spacingMult,
            spacingAdd,
            true
        )
        return layout.height
    }

    //---
    val _text = text ?: ""
    val textPaint = TextPaint(this)
    var targetTextSize: Float = textPaint.textSize
    var textHeight = getTextHeight(_text, textPaint, width, targetTextSize)
    while (textHeight > height && targetTextSize > minTextSize) {
        targetTextSize = max(targetTextSize - 1, minTextSize)
        textHeight = getTextHeight(_text, textPaint, width, targetTextSize)
    }
    return targetTextSize
}

/**获取一个字体大小, 在指定的宽高中
 * 使用[getTextBounds]测量高度, 所有传入的[height]应该也是使用[getTextBounds]测量出来的
 *
 * [android.graphics.Paint.getTextBounds]
 * */
@Pixel
fun Paint.findNewTextSizeByBounds(
    text: CharSequence?,
    height: Int = Int.MAX_VALUE,
    minTextSize: Float = 1f,
): Float {

    val rect = Rect()

    //calc
    fun getTextHeight(
        source: CharSequence,
        paint: TextPaint,
        textSize: Float
    ): Int {
        paint.textSize = textSize
        if (Build.VERSION.SDK_INT >= VERSION_CODES.Q) {
            paint.getTextBounds(source, 0, source.length, rect)
        } else {
            paint.getTextBounds(source.toString(), 0, source.length, rect)
        }
        return rect.height()
    }

    //---
    val _text = text ?: ""
    val textPaint = TextPaint(this)
    var targetTextSize: Float = textPaint.textSize
    var textHeight = getTextHeight(_text, textPaint, targetTextSize)
    while (textHeight > height && targetTextSize > minTextSize) {
        targetTextSize = max(targetTextSize - 1, minTextSize)
        textHeight = getTextHeight(_text, textPaint, targetTextSize)
    }
    return targetTextSize
}

//---

/**从左上角开始绘制文本, 保证文本左上角对齐点坐标
 * [x] [y] 左上角坐标
 * */
fun Canvas.drawTextByLT(text: CharSequence, x: Float, y: Float, paint: Paint) {
    val drawX = x
    val drawY = y + paint.textHeight() - paint.descent()
    drawText(text, 0, text.length, drawX, drawY, paint)
}

/**
 * Convenience for [Canvas.saveLayer] but instead of taking a entire Paint
 * object it takes only the `alpha` parameter.
 * [alpha] [0~255]
 */
fun Canvas.saveLayerAlpha(alpha: Int = 0, bounds: RectF? = null): Int {
    return if (Build.VERSION.SDK_INT > VERSION_CODES.LOLLIPOP) {
        saveLayerAlpha(bounds, alpha)
    } else {
        saveLayerAlpha(bounds, alpha, Canvas.ALL_SAVE_FLAG)
    }
}

/**
 * Convenience for [.saveLayerAlpha] that takes the four float
 * coordinates of the bounds rectangle.
 */
fun Canvas.saveLayerAlpha(alpha: Int, left: Float, top: Float, right: Float, bottom: Float): Int {
    return if (Build.VERSION.SDK_INT > VERSION_CODES.LOLLIPOP) {
        saveLayerAlpha(left, top, right, bottom, alpha)
    } else {
        saveLayerAlpha(left, top, right, bottom, alpha, Canvas.ALL_SAVE_FLAG)
    }
}


