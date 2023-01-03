package com.angcyo.canvas.items.data

import com.angcyo.canvas.Strategy
import com.angcyo.canvas.data.CanvasProjectItemBean
import com.angcyo.library.annotation.Pixel
import com.angcyo.library.unit.toMm
import com.angcyo.library.unit.toPixel

/**
 * 形状item
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/09/23
 */
class DataShapeItem(bean: CanvasProjectItemBean) : DataPathItem(bean) {

    /**更新边数*/
    fun updateSide(
        side: Int,
        renderer: DataItemRenderer,
        strategy: Strategy = Strategy.normal
    ) {
        val old = dataBean.side
        val new = side
        if (old == new) {
            return
        }
        renderer.canvasView.getCanvasUndoManager().addAndRedo(strategy, {
            dataBean.side = old
            updateRenderItem(renderer)
        }) {
            dataBean.side = new
            updateRenderItem(renderer)
        }
    }

    /**更新深度*/
    fun updateDepth(
        depth: Int,
        renderer: DataItemRenderer,
        strategy: Strategy = Strategy.normal
    ) {
        val old = dataBean.depth
        val new = depth
        if (old == new) {
            return
        }
        renderer.canvasView.getCanvasUndoManager().addAndRedo(strategy, {
            dataBean.depth = old
            updateRenderItem(renderer)
        }) {
            dataBean.depth = new
            updateRenderItem(renderer)
        }
    }

    /**更新圆角*/
    fun updateCorner(
        @Pixel corner: Float,
        renderer: DataItemRenderer,
        strategy: Strategy = Strategy.normal
    ) {
        val old = dataBean.rx.toPixel()
        val new = corner
        if (old == new) {
            return
        }
        renderer.canvasView.getCanvasUndoManager().addAndRedo(strategy, {
            dataBean.rx = old.toMm()
            dataBean.ry = dataBean.rx
            updateRenderItem(renderer)
        }) {
            dataBean.rx = new.toMm()
            dataBean.ry = dataBean.rx
            updateRenderItem(renderer)
        }
    }
}