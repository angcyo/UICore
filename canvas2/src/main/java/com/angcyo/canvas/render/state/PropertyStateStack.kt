package com.angcyo.canvas.render.state

import com.angcyo.canvas.render.core.CanvasRenderDelegate
import com.angcyo.canvas.render.core.Reason
import com.angcyo.canvas.render.core.Strategy
import com.angcyo.canvas.render.core.component.CanvasSelectorComponent
import com.angcyo.canvas.render.renderer.BaseRenderer
import com.angcyo.canvas.render.renderer.CanvasGroupRenderer
import com.angcyo.library.ex.resetAll

/**
 * 绘制属性的状态存储和恢复
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/03/09
 */
open class PropertyStateStack : IStateStack {

    /**存档信息*/
    protected val map = hashMapOf<String, RendererState>()

    operator fun get(uuid: String): RendererState? = map[uuid]

    operator fun get(renderer: BaseRenderer): RendererState? = get(renderer.uuid)

    /**保存状态*/
    override fun saveState(renderer: BaseRenderer) {
        val key = renderer.uuid

        val state = RendererState(
            renderer,
            renderer.renderProperty?.copyTo(),
            if (renderer is CanvasGroupRenderer) renderer.rendererList.toList() else null
        )

        map[key] = state
        if (renderer is CanvasGroupRenderer) {
            for (sub in renderer.rendererList) {
                saveState(sub)
            }
        }
    }

    /**恢复状态*/
    override fun restoreState(
        renderer: BaseRenderer,
        reason: Reason,
        strategy: Strategy,
        delegate: CanvasRenderDelegate?
    ) {
        map.forEach { entry ->
            val state = entry.value
            val stateRenderer = state.renderer

            if (stateRenderer is CanvasGroupRenderer) {
                state.rendererList?.let { list ->
                    if (stateRenderer is CanvasSelectorComponent &&
                        (strategy.type == Strategy.STRATEGY_TYPE_UNDO || strategy.type == Strategy.STRATEGY_TYPE_REDO)
                    ) {
                        //通知选中元素改变
                        val from = stateRenderer.rendererList.toList()
                        stateRenderer.rendererList.resetAll(list)
                        delegate?.dispatchSelectorRendererChange(from, list)
                    } else {
                        stateRenderer.rendererList.resetAll(list)
                    }
                }
            }

            stateRenderer.updateRenderProperty(state.renderProperty, reason, delegate)
        }
    }
}