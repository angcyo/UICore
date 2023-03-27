package com.angcyo.canvas.render.data

import android.graphics.RectF
import com.angcyo.canvas.render.annotation.CanvasInsideCoordinate
import com.angcyo.canvas.render.renderer.CanvasElementRenderer

/**
 * 智能推荐的参考值
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/03/27
 */
data class SmartAssistantReferenceValue(
    /**值, 比例x/y轴的坐标, 需要旋转的角度等*/
    @CanvasInsideCoordinate
    val value: Float,

    /**[value]给出的参考[bounds], 绘制提示线的时候, 可以参考这个
     * 不指定时使用[com.angcyo.canvas.render.core.CanvasRenderViewBox.visibleBoundsInside]作为绘制的参考*/
    @CanvasInsideCoordinate
    val bounds: RectF?,

    /**这个值, 来自那个对象, 可以是对象, 也可以是刻度尺
     * [com.angcyo.canvas.render.core.IRenderer]
     * [com.angcyo.canvas.render.core.CanvasAxisManager]
     * */
    val obj: Any?
) {

    /**引用的元素Bounds*/
    val refElementBounds: RectF?
        get() = if (obj is CanvasElementRenderer) {
            obj.renderProperty?.getRenderBounds()
        } else {
            null
        }
}
