package com.angcyo.canvas.render.data

import android.graphics.PointF
import com.angcyo.library.canvas.annotation.CanvasOutsideCoordinate
import com.angcyo.canvas.render.renderer.BaseRenderer

/**
 * [com.angcyo.canvas.render.core.CanvasSelectorManager]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/18
 */
data class TouchSelectorInfo(
    /**按下时的点坐标, 相对于画板左上角*/
    @CanvasOutsideCoordinate
    val touchPoint: PointF,
    /**按下时, 底下有多少元素, 已逆序, 最上层的元素在列表最前面*/
    val touchRendererList: List<BaseRenderer>
)
