package com.angcyo.drawable.base

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.view.View
import com.angcyo.library.ex.drawRect
import kotlin.math.min

/**
 * 绘制[Path]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/08/12
 */
class PathDrawable : AbsDslDrawable() {

    /**需要绘制的路径*/
    val path: Path = Path()

    init {
        textPaint.style = Paint.Style.FILL
    }

    override fun draw(canvas: Canvas) {
        canvas.drawPath(path, textPaint)
    }

    /**更新[path]并触发更新*/
    fun updatePath(block: Path.() -> Unit) {
        path.rewind()
        path.block()
        invalidateSelf()
    }

    val _tempRect = Rect()

    /**添加一个圆*/
    fun addCircle(view: View?, radius: Float? = null) {
        view?.let {
            updatePath {
                it.drawRect(_tempRect)
                val viewRadius = min(_tempRect.width(), _tempRect.height()) / 2f
                addCircle(
                    _tempRect.centerX().toFloat(),
                    _tempRect.centerY().toFloat(),
                    radius ?: viewRadius,
                    Path.Direction.CW
                )
            }
        }
    }
}