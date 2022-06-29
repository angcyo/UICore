package com.angcyo.library.ex

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.os.Build
import android.text.TextPaint
import com.angcyo.library.R

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

/**文本的宽度*/
fun Paint.textWidth(text: String?): Float {
    if (text == null) {
        return 0f
    }
    return measureText(text)
}

/**文本的高度*/
fun Paint?.textHeight(): Float = this?.run { descent() - ascent() } ?: 0f

/**文本绘制时的y坐标
 * [originY]原本y坐标¬*/
fun Paint.drawTextY(originY: Float): Float {
    return originY - descent()
}

/**从左上角开始绘制文本, 保证文本左上角对齐点坐标
 * [x] [y] 左上角坐标
 * */
fun Canvas.drawTextByLT(text: CharSequence, x: Float, y: Float, paint: Paint) {
    val drawX = x
    val drawY = y + paint.textHeight() - paint.descent()
    drawText(text, 0, text.length, drawX, drawY, paint)
}

/**获取文本的边界, 包含所有文本的最小矩形*/
fun Paint.textBounds(text: CharSequence?, result: Rect = Rect(0, 0, 0, 0)): Rect {
    if (text == null) {
        return result
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
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