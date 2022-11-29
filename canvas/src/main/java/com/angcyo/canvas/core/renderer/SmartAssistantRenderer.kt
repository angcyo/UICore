package com.angcyo.canvas.core.renderer

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.graphics.withMatrix
import androidx.core.graphics.withTranslation
import com.angcyo.canvas.CanvasDelegate
import com.angcyo.canvas.R
import com.angcyo.canvas.core.ICanvasView
import com.angcyo.canvas.core.RenderParams
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

    val paint = createPaint(
        _color(R.color.canvas_assistant, smartAssistant.canvasDelegate.view.context),
        Paint.Style.FILL
    )

    /**提示线的宽度*/
    var strokeWidth = 1 * dp

    override fun render(canvas: Canvas, renderParams: RenderParams) {
        val view = canvasView
        if (view is CanvasDelegate) {
            if (!view.isTouchHold) {
                //手势没有按下时, 不绘制
                return
            }
        }
        val scale = canvasViewBox.getScaleX()
        paint.strokeWidth = strokeWidth / scale //抵消坐标系的缩放
        canvas.withMatrix(canvasViewBox.matrix) {
            canvas.withTranslation(
                canvasViewBox.getCoordinateSystemX(),
                canvasViewBox.getCoordinateSystemY()
            ) {
                eachAssistantRect {
                    canvas.drawLine(left, top, right, bottom, paint)
                }
            }
        }
    }

    /**枚举所有需要绘制的提示矩形*/
    fun eachAssistantRect(block: RectF.() -> Unit) {
        smartAssistant.lastXAssistant?.drawRect?.let(block)
        smartAssistant.lastYAssistant?.drawRect?.let(block)
        smartAssistant.lastRotateAssistant?.drawRect?.let(block)
        smartAssistant.lastWidthAssistant?.drawRect?.let(block)
        smartAssistant.lastHeightAssistant?.drawRect?.let(block)
    }
}