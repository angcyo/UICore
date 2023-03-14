package com.angcyo.canvas.render.state

import com.angcyo.canvas.render.core.component.CanvasRenderProperty
import com.angcyo.canvas.render.renderer.BaseRenderer

/**
 * 渲染器的状态, 保存[com.angcyo.canvas.render.renderer.BaseRenderer]的状态, 用于恢复
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/25
 */
data class RendererState(
    /**渲染器*/
    val renderer: BaseRenderer,
    /**[com.angcyo.canvas.render.renderer.BaseRenderer.renderProperty]*/
    val renderProperty: CanvasRenderProperty?,
    /**[com.angcyo.canvas.render.renderer.CanvasGroupRenderer.rendererList]*/
    val rendererList: List<BaseRenderer>?
)