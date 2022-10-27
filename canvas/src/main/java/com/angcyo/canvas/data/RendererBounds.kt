package com.angcyo.canvas.data

import android.graphics.RectF
import com.angcyo.canvas.items.renderer.BaseItemRenderer

/**
 * 用来保存[BaseItemRenderer]的Bounds
 * []
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/10/27
 */
class RendererBounds(val renderer: BaseItemRenderer<*>) {

    /**保存一份*/
    val bounds: RectF = RectF(renderer.getBounds())
}
