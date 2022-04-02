package com.angcyo.canvas.core.renderer

import android.graphics.Paint
import com.angcyo.canvas.core.CanvasViewBox
import com.angcyo.canvas.core.Transformer
import com.angcyo.canvas.utils.createPaint
import com.angcyo.library.ex.dp
import com.angcyo.library.ex.toColor

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/01
 */
abstract class BaseAxisRenderer(canvasViewBox: CanvasViewBox, transformer: Transformer) :
    BaseRenderer(canvasViewBox, transformer) {

    /**绘制刻度的画笔*/
    val linePaint = createPaint()

    /**主要网格线的画笔*/
    val gridProtrudePaint = createPaint("#d0d0d0".toColor())

    /**普通网格线的画笔*/
    val gridPaint = createPaint("#dedede".toColor())

    /**绘制刻度文字的画笔*/
    val labelPaint = createPaint().apply {
        textSize = 9 * dp
        style = Paint.Style.FILL
    }

}