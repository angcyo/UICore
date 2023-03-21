package com.angcyo.canvas.render.data

import com.angcyo.canvas.render.renderer.BaseRenderer
import com.angcyo.canvas.render.state.PropertyStateStack
import com.angcyo.canvas.render.state.RendererState

/**
 * 平移/旋转/缩放控制渲染器时的存档信息
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/24
 */
class ControlRendererInfo(val controlRenderer: BaseRenderer) : PropertyStateStack() {

    /**顶级元素的状态*/
    val state: RendererState
        get() = get(controlRenderer)!!

    init {
        saveState(controlRenderer, null)
    }
}