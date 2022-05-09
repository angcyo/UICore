package com.angcyo.canvas.core.renderer

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import com.angcyo.canvas.BuildConfig
import com.angcyo.canvas.core.ICanvasView
import com.angcyo.canvas.utils._tempRectF
import com.angcyo.canvas.utils.createPaint
import com.angcyo.library.ex.dp

/**
 * 打印限制提示框渲染
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/24
 */
class LimitRenderer(canvasView: ICanvasView) : BaseRenderer(canvasView) {

    /**限制框的宽度*/
    var limitStrokeWidth = 1 * dp

    /**画笔*/
    val paint = createPaint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
    }

    /**限制框*/
    val limitPath: Path = Path()

    override fun render(canvas: Canvas) {
        if (BuildConfig.DEBUG) {
            limitPath.computeBounds(_tempRectF, true)
        }
        val scale = canvasViewBox.getScaleX()
        paint.strokeWidth = limitStrokeWidth / scale //抵消坐标系的缩放
        canvas.drawPath(limitPath, paint)
    }

    /**更新限制框*/
    fun updateLimit(block: Path.() -> Unit) {
        limitPath.reset()
        limitPath.block()
        refresh()
    }
}