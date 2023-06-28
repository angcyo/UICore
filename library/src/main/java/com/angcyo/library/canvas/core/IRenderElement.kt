package com.angcyo.library.canvas.core

import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import com.angcyo.library.canvas.annotation.CanvasInsideCoordinate
import com.angcyo.library.ex.contains

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/06/28
 */
interface IRenderElement : IRenderInside {

    /**渲染的位置*/
    @CanvasInsideCoordinate
    fun getRenderBounds(): RectF

    /**当前元素是否可以被选中*/
    fun canSelectElement(): Boolean = true

    /**元素的bounds是否完全包含point*/
    fun elementContainsPoint(
        renderDelegate: ICanvasView,
        @CanvasInsideCoordinate point: PointF
    ): Boolean {
        val bounds = getRenderBounds()
        val path = Path()
        path.addRect(bounds, Path.Direction.CW)
        return path.contains(point)
    }

    /**元素的bounds是否完全包含point*/
    fun elementContainsRect(
        renderDelegate: ICanvasView,
        @CanvasInsideCoordinate rect: RectF
    ): Boolean {
        val bounds = getRenderBounds()
        val path = Path()
        path.addRect(bounds, Path.Direction.CW)
        return path.contains(rect)
    }

}