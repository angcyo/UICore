package com.angcyo.widget.span

import android.text.SpannableStringBuilder
import android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
import android.text.style.LeadingMarginSpan
import com.angcyo.library.ex.undefined_int

/**
 * span 操作类
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/08
 */

class DslSpan {
    companion object {
        private val LINE_SEPARATOR = System.getProperty("line.separator")
    }

    val builder = SpannableStringBuilder()

    fun doIt(): SpannableStringBuilder {
        return builder
    }

    fun append() {
        val csl :LeadingMarginSpan.LeadingMarginSpan2
    }

}

data class DslSpanConfig(
    var flag: Int = SPAN_EXCLUSIVE_EXCLUSIVE,
    var foregroundColor: Int = undefined_int,
    var backgroundColor: Int = undefined_int
    )

fun span(action: DslSpan.() -> Unit): SpannableStringBuilder {
    return DslSpan().apply {
        action()
    }.doIt()
}