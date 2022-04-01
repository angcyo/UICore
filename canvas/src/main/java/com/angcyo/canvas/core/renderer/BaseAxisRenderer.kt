package com.angcyo.canvas.core.renderer

import android.graphics.Paint
import com.angcyo.canvas.core.Transformer
import com.angcyo.canvas.core.ViewBox
import com.angcyo.canvas.utils.createPaint
import com.angcyo.library.ex.dp

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/01
 */
abstract class BaseAxisRenderer(viewBox: ViewBox, transformer: Transformer) :
    BaseRenderer(viewBox, transformer) {

    /**绘制刻度的画笔*/
    val linePaint = createPaint()

    /**绘制刻度文字的画笔*/
    val labelPaint = createPaint().apply {
        textSize = 9 * dp
        style = Paint.Style.FILL
    }

}