package com.angcyo.canvas.core.renderer

import android.graphics.Color
import android.graphics.Paint
import com.angcyo.canvas.core.CanvasViewBox
import com.angcyo.canvas.utils.createPaint
import com.angcyo.library.ex.dp
import com.angcyo.library.ex.toColor

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/01
 */
abstract class BaseAxisRenderer(canvasViewBox: CanvasViewBox) : BaseRenderer(canvasViewBox) {

    /**主要线的画笔*/
    val lineProtrudePaint = createPaint(Color.GRAY)

    /**绘制刻度的画笔*/
    val linePaint = createPaint("#d0d0d0".toColor())

    /**主要网格线的画笔*/
    val gridProtrudePaint = createPaint(Color.GRAY)

    /**普通网格线的画笔*/
    val gridPaint = createPaint("#d0d0d0".toColor())

    /**绘制刻度文字的画笔*/
    val labelPaint = createPaint(Color.GRAY).apply {
        textSize = 9 * dp
        style = Paint.Style.FILL
    }

}