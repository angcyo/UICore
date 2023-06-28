package com.angcyo.library.canvas.element

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import androidx.core.graphics.withSave
import com.angcyo.library.canvas.core.IRenderElement
import com.angcyo.library.ex.computePathBounds
import com.angcyo.library.ex.createPaint

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/06/28
 */
open class PathElement : IRenderElement {

    /**是否可以被选中*/
    var canSelect = true

    /**绘制的路径*/
    val path = Path()

    /**画笔*/
    val paint = createPaint(Color.BLACK, Paint.Style.STROKE)

    val bounds = RectF()

    override fun canSelectElement(): Boolean = canSelect

    override fun getRenderBounds(): RectF {
        path.computePathBounds(bounds)
        return bounds
    }

    override fun renderOnInside(canvas: Canvas) {
        canvas.withSave {
            val renderBounds = getRenderBounds()
            //translate(renderBounds.left, renderBounds.top)//平移到指定位置
            drawPath(path, paint)
        }
    }
}