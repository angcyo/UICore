package com.angcyo.canvas.core.renderer

import com.angcyo.canvas.core.ICanvasView

/**
 * 覆盖在上层绘制的渲染器, 不受
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/01/07
 */
abstract class BaseOverlayRenderer(canvasView: ICanvasView) : BaseRenderer(canvasView) {

    init {
        overlayRender()
    }

}