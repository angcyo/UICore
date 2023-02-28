package com.angcyo.canvas.render.data

import com.angcyo.canvas.render.core.CanvasRenderDelegate
import com.angcyo.canvas.render.core.Reason
import com.angcyo.canvas.render.renderer.BaseRenderer
import com.angcyo.canvas.render.renderer.CanvasGroupRenderer
import com.angcyo.library.ex.resetAll

/**
 * 平移/旋转/缩放控制渲染器时的存档信息
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/24
 */
class ControlRendererInfo(val controlRenderer: BaseRenderer) {

    /**存档信息*/
    val map = hashMapOf<String, RendererState>()

    /**顶级元素的状态*/
    val state: RendererState
        get() = get(controlRenderer)!!

    init {
        saveState(controlRenderer)
    }

    operator fun get(uuid: String): RendererState? = map[uuid]

    operator fun get(renderer: BaseRenderer): RendererState? = get(renderer.uuid)

    /**存储状态到[map]*/
    fun saveState(renderer: BaseRenderer) {
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

    /**从[map]中恢复状态*/
    fun restoreState(reason: Reason, delegate: CanvasRenderDelegate?) {
        map.forEach { entry ->
            val state = entry.value
            val renderer = state.renderer

            if (renderer is CanvasGroupRenderer) {
                state.rendererList?.let { renderer.rendererList.resetAll(it) }
            }

            renderer.updateRenderProperty(state.renderProperty, reason, delegate)
        }
        delegate?.refresh()
    }

}