package com.angcyo.canvas.core.renderer

import android.graphics.Canvas
import android.graphics.Color
import com.angcyo.canvas.core.CanvasViewBox
import com.angcyo.canvas.core.Transformer
import com.angcyo.canvas.utils.createPaint

/**
 * 中点坐标提示渲染
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/08
 */
class CenterRenderer(canvasViewBox: CanvasViewBox, transformer: Transformer) :
    BaseRenderer(canvasViewBox, transformer) {

    val paint = createPaint(Color.RED).apply {
        //init
    }

    override fun render(canvas: Canvas) {
        val x = (canvasViewBox.getContentLeft() + canvasViewBox.getContentRight()) / 2
        val y = (canvasViewBox.getContentTop() + canvasViewBox.getContentBottom()) / 2

        //横线
        canvas.drawLine(0f, y, canvasViewBox.getContentRight(), y, paint)

        //竖线
        canvas.drawLine(x, 0f, x, canvasViewBox.getContentBottom(), paint)
    }
}