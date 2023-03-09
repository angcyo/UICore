package com.angcyo.canvas.render.data

import com.angcyo.canvas.render.core.CanvasRenderDelegate
import com.angcyo.canvas.render.core.Reason
import com.angcyo.canvas.render.renderer.BaseRenderer
import com.angcyo.canvas.render.renderer.CanvasGroupRenderer
import com.angcyo.library.ex.resetAll

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/03/09
 */
open class PropertyStateStack : IStateStack {

    /**存档信息*/
    protected val map = hashMapOf<String, RendererState>()

    operator fun get(uuid: String): RendererState? = map[uuid]

    operator fun get(renderer: BaseRenderer): RendererState? = get(renderer.uuid)

    /**保存状态*/
    open fun saveState(renderer: BaseRenderer) {
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
    override fun restoreState(reason: Reason, delegate: CanvasRenderDelegate?) {
        map.forEach { entry ->
            val state = entry.value
            val renderer = state.renderer

            if (renderer is CanvasGroupRenderer) {
                state.rendererList?.let { list ->
                    renderer.rendererList.resetAll(list)
                }
            }

            renderer.updateRenderProperty(state.renderProperty, reason, delegate)
        }
    }
}