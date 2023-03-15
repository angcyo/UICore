package com.angcyo.canvas.render.data

import com.angcyo.canvas.render.core.CanvasRenderDelegate

/**
 * 渲染参数, 各取所需
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/03/06
 */
data class RenderParams(
    /**代理*/
    var delegate: CanvasRenderDelegate? = null,
    /**需要覆盖的输出宽高, 手动处理参数*/
    var overrideSize: Float? = null
)
