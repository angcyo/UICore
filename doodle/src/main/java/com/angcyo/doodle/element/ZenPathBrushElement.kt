package com.angcyo.doodle.element

import android.graphics.Canvas
import android.graphics.Paint
import com.angcyo.doodle.data.BrushElementData
import com.angcyo.doodle.layer.BaseLayer

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/08/02
 */
class ZenPathBrushElement(brushElementData: BrushElementData) : BaseBrushElement(brushElementData) {

    override fun onDraw(layer: BaseLayer, canvas: Canvas) {
        brushElementData.brushPath?.apply {
            paint.color = brushElementData.paintColor
            paint.style = Paint.Style.FILL
            canvas.drawPath(this, paint)
        }
    }
}