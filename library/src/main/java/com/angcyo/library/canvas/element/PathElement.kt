package com.angcyo.library.canvas.element

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import androidx.core.graphics.withSave
import com.angcyo.library.annotation.CallPoint
import com.angcyo.library.canvas.core.ICanvasView
import com.angcyo.library.canvas.core.IRenderElement
import com.angcyo.library.ex.computePathBounds
import com.angcyo.library.ex.createPaint
import com.angcyo.library.ex.translateToOrigin

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/06/28
 */
open class PathElement : IRenderElement {

    /**是否可以被选中*/
    var canSelect = true


    /**画笔*/
    val paint = createPaint(Color.BLACK, Paint.Style.STROKE)

    protected val bounds = RectF()

    /**绘制的路径*/
    protected val path = Path()

    protected var _cachePath: Path? = null

    /**使用此方法更新[path]*/
    @CallPoint
    open fun updatePath(block: Path.() -> Unit) {
        path.rewind()
        path.block()

        _cachePath = path.translateToOrigin()
    }

    override fun canSelectElement(): Boolean = canSelect

    override fun getRenderBounds(): RectF {
        path.computePathBounds(bounds)
        return bounds
    }

    override fun renderOnInside(iCanvasView: ICanvasView, canvas: Canvas) {
        canvas.withSave {
            val renderBounds = getRenderBounds()
            translate(renderBounds.left, renderBounds.top)//平移到指定位置
            drawPath(_cachePath ?: path, paint)
        }
    }
}