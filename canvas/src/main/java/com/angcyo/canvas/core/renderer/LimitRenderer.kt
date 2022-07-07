package com.angcyo.canvas.core.renderer

import android.graphics.*
import com.angcyo.canvas.core.ICanvasView
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

    /**用来[InitialPointHandler]恢复显示范围*/
    var limitBounds: RectF? = null

    val _limitPathBounds: RectF = RectF()

    override fun render(canvas: Canvas) {
        if (!limitPath.isEmpty) {
            limitPath.computeBounds(_limitPathBounds, true)
            val scale = canvasViewBox.getScaleX()
            paint.strokeWidth = limitStrokeWidth / scale //抵消坐标系的缩放
            canvas.drawPath(limitPath, paint)
        } else {
            _limitPathBounds.setEmpty()
        }
    }

    /**更新限制框*/
    fun updateLimit(block: Path.() -> Unit) {
        limitPath.rewind()
        limitPath.block()
        refresh()
    }

    /**清除限制框*/
    fun clear() {
        limitPath.rewind()
        _limitPathBounds.setEmpty()
        refresh()
    }
}