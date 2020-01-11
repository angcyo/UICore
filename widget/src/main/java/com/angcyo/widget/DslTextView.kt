package com.angcyo.widget

import android.content.Context
import android.text.Spanned
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.angcyo.widget.span.IWeightSpan

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/08
 */
open class DslTextView(context: Context, attributeSet: AttributeSet? = null) :
    AppCompatTextView(context, attributeSet) {

    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        return super.onCreateDrawableState(extraSpace)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)

        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        _measureWeightSpan(widthSize, heightSize)

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    fun _measureWeightSpan(widthSize: Int, heightSize: Int) {
        val text = text
        if (text is Spanned) {
            val spans = text.getSpans(0, text.length, Any::class.java)
            for (span in spans) {
                if (span is IWeightSpan) {
                    span.onMeasure(widthSize, heightSize)
                }
            }
        }
    }
}