package com.angcyo.widget.span

import android.graphics.Canvas
import android.graphics.Paint
import android.text.style.ReplacementSpan
import com.angcyo.library.ex.textWidth

/**
 * 随机文本的[CharacterStyle]
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2023/06/10
 */
class RandomTextSpan(val randomTextList: List<String>) : ReplacementSpan() {

    private var drawText: String? = null

    override fun getSize(
        paint: Paint,
        text: CharSequence,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        drawText = randomTextList.randomOrNull()
        return paint.textWidth(drawText).toInt()
    }

    override fun draw(
        canvas: Canvas,
        text: CharSequence?,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        drawText?.let {
            canvas.drawText(it, x, y.toFloat(), paint)
        }
    }
}