package com.angcyo.canvas.utils

import android.graphics.Color
import android.graphics.Paint

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/01
 */

fun createPaint(color: Int = Color.GRAY) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    this.color = color
    strokeWidth = 1f
    style = Paint.Style.STROKE
    strokeJoin = Paint.Join.ROUND
    strokeCap = Paint.Cap.ROUND
}