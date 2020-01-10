package com.angcyo.widget.span

import android.graphics.Canvas
import android.graphics.Paint
import android.text.TextPaint
import android.text.style.MetricAffectingSpan
import android.text.style.ReplacementSpan
import com.angcyo.drawable.dpi

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/08
 */

open class DslTextSpan : ReplacementSpan() {
    override fun getSize(
        paint: Paint,
        text: CharSequence?,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        return 10 * dpi
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
        canvas.drawText(text?.subSequence(start, end) ?: "", start, end, x, y.toFloat(), paint)
    }

    override fun updateMeasureState(p: TextPaint) {
        super.updateMeasureState(p)
    }

    override fun getUnderlying(): MetricAffectingSpan {
        return super.getUnderlying()
    }

    override fun updateDrawState(ds: TextPaint?) {
        super.updateDrawState(ds)
    }
}