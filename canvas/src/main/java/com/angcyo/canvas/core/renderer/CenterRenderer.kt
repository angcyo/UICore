package com.angcyo.canvas.core.renderer

import android.graphics.Canvas
import android.graphics.Color
import com.angcyo.canvas.CanvasView
import com.angcyo.canvas.core.CanvasViewBox
import com.angcyo.canvas.utils.createPaint

/**
 * 中点坐标提示渲染
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/08
 */
class CenterRenderer(val canvasView: CanvasView, canvasViewBox: CanvasViewBox) :
    BaseRenderer(canvasViewBox) {

    val paint = createPaint(Color.RED).apply {
        //init
    }

    override fun render(canvas: Canvas) {

        //绘制坐标轴中心
        paint.color = Color.GREEN
        var x = canvasViewBox.getContentCenterX()
        var y = canvasViewBox.getContentCenterY()

        //横线
        canvas.drawLine(0f, y, canvasViewBox.getContentRight(), y, paint)
        //竖线
        canvas.drawLine(x, 0f, x, canvasViewBox.getContentBottom(), paint)

        //绘制视图中心
        paint.color = Color.MAGENTA
        x = canvasView.measuredWidth / 2f
        y = canvasView.measuredHeight / 2f
        //横线
        canvas.drawLine(0f, y, canvasView.measuredWidth.toFloat(), y, paint)
        //竖线
        canvas.drawLine(x, 0f, x, canvasView.measuredHeight.toFloat(), paint)
    }
}