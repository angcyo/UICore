package com.angcyo.canvas.core.renderer

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import com.angcyo.canvas.core.CanvasViewBox
import com.angcyo.canvas.utils.createPaint
import com.angcyo.library.ex.dp

/**
 * 打印限制框渲染
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/24
 */
class LimitRenderer(canvasViewBox: CanvasViewBox) : BaseRenderer(canvasViewBox) {

    /**画笔*/
    val paint = createPaint().apply {
        color = Color.RED
        strokeWidth = 1 * dp
        style = Paint.Style.STROKE
    }

    /**限制框*/
    val limitPath: Path = Path()

    override fun render(canvas: Canvas) {
        canvas.drawPath(limitPath, paint)
    }

    /**更新限制框*/
    fun updateLimit(block: Path.() -> Unit) {
        limitPath.reset()
        limitPath.block()
        refresh()
    }
}