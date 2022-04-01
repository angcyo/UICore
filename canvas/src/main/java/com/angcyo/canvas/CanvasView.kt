package com.angcyo.canvas

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import com.angcyo.canvas.core.CanvasViewBox
import com.angcyo.canvas.core.ICanvasView
import com.angcyo.canvas.core.Transformer
import com.angcyo.canvas.core.component.XAxis
import com.angcyo.canvas.core.component.YAxis
import com.angcyo.canvas.core.renderer.XAxisRenderer
import com.angcyo.canvas.core.renderer.YAxisRenderer

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/03/29
 */
class CanvasView(context: Context, attributeSet: AttributeSet? = null) :
    View(context, attributeSet), ICanvasView {

    //<editor-fold desc="成员变量">

    val canvasViewBox = CanvasViewBox(this)

    //</editor-fold desc="成员变量">

    //<editor-fold desc="横纵坐标轴">

    /**绘制在顶上的x轴*/
    val xAxisRender = XAxisRenderer(XAxis(), canvasViewBox, Transformer(canvasViewBox))

    /**绘制在左边的y轴*/
    val yAxisRender = YAxisRenderer(YAxis(), canvasViewBox, Transformer(canvasViewBox))

    //</editor-fold desc="横纵坐标轴">

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (xAxisRender.xAxis.enable) {
            xAxisRender.updateRenderBounds(this)
        }
        if (yAxisRender.yAxis.enable) {
            yAxisRender.updateRenderBounds(this)
        }
        canvasViewBox.updateContentBox()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (xAxisRender.xAxis.enable) {
            xAxisRender.render(canvas)
        }

        if (yAxisRender.yAxis.enable) {
            yAxisRender.render(canvas)
        }

        //canvas.drawColor(Color)
    }

}