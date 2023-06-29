package com.angcyo.library.canvas.element

import android.graphics.Canvas
import android.graphics.Paint
import com.angcyo.library.R
import com.angcyo.library.canvas.core.ICanvasComponent
import com.angcyo.library.canvas.core.ICanvasView
import com.angcyo.library.canvas.core.IRenderOutside
import com.angcyo.library.ex._color
import com.angcyo.library.ex.createPaint
import com.angcyo.library.ex.dp
import kotlin.math.roundToInt

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/06/29
 */
class MonitorRenderElement : IRenderOutside, ICanvasComponent {

    val paint = createPaint(_color(R.color.lib_theme_black)).apply {
        textSize = 9 * dp
    }

    /**偏移量*/
    var offsetX = 0f
    var offsetY = 0f

    override var isEnableComponent: Boolean = true

    override fun renderOnOutside(iCanvasView: ICanvasView, canvas: Canvas) {
        if (isEnableComponent) {
            drawScaleText(iCanvasView, canvas, iCanvasView.getRawView().measuredHeight.toFloat())
        }
    }

    /**绘制缩放比例文本*/
    private fun drawScaleText(iCanvasView: ICanvasView, canvas: Canvas, bottom: Float) {
        val box = iCanvasView.getCanvasViewBox()
        val text = "${(box.getScale() * 100).roundToInt()}%"
        paint.style = Paint.Style.FILL

        val x = offsetX
        val y = bottom - paint.descent() - offsetY
        canvas.drawText(text, x, y, paint)
    }
}