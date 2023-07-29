package com.angcyo.drawable.base

import android.graphics.ColorFilter
import android.graphics.Paint
import android.text.TextPaint
import com.angcyo.library.ex.dp

/**
 * 基类
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/08/03
 */
abstract class BaseDrawDrawable : BaseDrawable() {

    /**画笔*/
    val textPaint: TextPaint by lazy {
        TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            isFilterBitmap = true
            style = Paint.Style.FILL
            textSize = 12 * dp
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }
    }

    override fun setAlpha(alpha: Int) {
        if (textPaint.alpha != alpha) {
            textPaint.alpha = alpha
            invalidateSelf()
        }
    }

    override fun getAlpha(): Int {
        return textPaint.alpha
    }

    override fun getColorFilter(): ColorFilter? {
        return textPaint.colorFilter
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        textPaint.colorFilter = colorFilter
        invalidateSelf()
    }
}