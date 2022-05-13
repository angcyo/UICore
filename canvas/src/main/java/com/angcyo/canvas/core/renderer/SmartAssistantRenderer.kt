package com.angcyo.canvas.core.renderer

import android.graphics.Canvas
import android.graphics.Paint
import androidx.core.graphics.withMatrix
import com.angcyo.canvas.R
import com.angcyo.canvas.core.ICanvasView
import com.angcyo.canvas.core.component.SmartAssistant
import com.angcyo.canvas.utils.createPaint
import com.angcyo.library.ex._color
import com.angcyo.library.ex.dp

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/22
 */
class SmartAssistantRenderer(val smartAssistant: SmartAssistant, canvasView: ICanvasView) :
    BaseRenderer(canvasView) {

    val paint = createPaint(_color(R.color.canvas_assistant), Paint.Style.FILL)

    /**提示线的宽度*/
    var strokeWidth = 1 * dp

    override fun render(canvas: Canvas) {
        canvas.withMatrix(canvasViewBox.matrix) {
            smartAssistant.smartLineList.forEach {
                val scale = canvasViewBox.getScaleX()
                paint.strokeWidth = strokeWidth / scale //抵消坐标系的缩放
                canvas.drawLine(it.left, it.top, it.right, it.bottom, paint)
            }
        }
    }

}