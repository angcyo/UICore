package com.angcyo.doodle.element

import android.graphics.Canvas
import android.graphics.Paint
import com.angcyo.doodle.data.PathBrushElementData
import com.angcyo.doodle.layer.BaseLayer

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/08/11
 */
class ZenPathBrushElement(val pathBrushElementData: PathBrushElementData) :
    BaseBrushElement(pathBrushElementData) {

    override fun onDraw(layer: BaseLayer, canvas: Canvas) {
        pathBrushElementData.listPath.forEach {
            //在Draw方法中, 循环绘制特别耗性能
            paint.color = pathBrushElementData.paintColor
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = it.strokeWidth
            canvas.drawPath(it, paint)
        }
    }
}