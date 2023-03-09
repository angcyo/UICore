package com.angcyo.canvas.render.core

import android.graphics.Canvas
import com.angcyo.canvas.render.annotation.CanvasInsideCoordinate
import com.angcyo.canvas.render.annotation.CanvasOutsideCoordinate
import com.angcyo.canvas.render.annotation.RenderFlag
import com.angcyo.canvas.render.data.RenderParams
import com.angcyo.library.annotation.CallPoint

/**
 * 声明一个可以渲染的组件
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/14
 */
interface IRenderer {

    companion object {

        /**激活回调[renderOnView]*/
        @RenderFlag
        const val RENDERER_FLAG_ON_VIEW = 0b1

        /**激活回调[renderOnInside]*/
        @RenderFlag
        const val RENDERER_FLAG_ON_INSIDE = RENDERER_FLAG_ON_VIEW shl 1

        /**激活回调[renderOnOutside]*/
        @RenderFlag
        const val RENDERER_FLAG_ON_OUTSIDE = RENDERER_FLAG_ON_INSIDE shl 1

        /**最后一个标识位*/
        @RenderFlag
        const val RENDERER_FLAG_LAST = RENDERER_FLAG_ON_OUTSIDE shl 1
    }

    /**当前类的flag标识位*/
    var renderFlags: Int

    /**绘制顺序1: 在画板内部绘制, 相对于画板原点坐标系绘制
     * 受[com.angcyo.canvas.render.core.CanvasRenderViewBox.renderBounds]影响
     * 受[com.angcyo.canvas.render.core.CanvasRenderViewBox.originGravity]影响
     * 受[com.angcyo.canvas.render.core.CanvasRenderViewBox.renderMatrix]影响
     *
     * [com.angcyo.canvas.render.core.CanvasRenderViewBox.transformToInside]
     * */
    @CallPoint
    @CanvasInsideCoordinate
    fun renderOnInside(canvas: Canvas, params: RenderParams) {
    }

    /**绘制顺序2: 在画板上面, 相对于画板左上角坐标系绘制
     * 受[com.angcyo.canvas.render.core.CanvasRenderViewBox.renderBounds]影响
     * 受[com.angcyo.canvas.render.core.CanvasRenderViewBox.originGravity]影响
     * 不受[com.angcyo.canvas.render.core.CanvasRenderViewBox.renderMatrix]影响
     *
     * [com.angcyo.canvas.render.core.CanvasRenderViewBox.transformToOutside]
     * */
    @CallPoint
    @CanvasOutsideCoordinate
    fun renderOnOutside(canvas: Canvas, params: RenderParams) {
    }

    /**绘制顺序3: 直接在[android.view.View]上绘制*/
    @CallPoint
    fun renderOnView(canvas: Canvas, params: RenderParams) {
    }

}