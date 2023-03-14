package com.angcyo.canvas.render.state

import com.angcyo.canvas.render.core.CanvasRenderDelegate
import com.angcyo.canvas.render.core.Reason
import com.angcyo.canvas.render.core.Strategy
import com.angcyo.canvas.render.element.BitmapElement
import com.angcyo.canvas.render.renderer.BaseRenderer
import com.angcyo.canvas.render.util.renderElement

/**
 * 图片状态存储, 用来恢复/重做
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/03/14
 */
open class BitmapStateStack(val renderer: BaseRenderer) : PropertyStateStack() {

    protected val bitmapElement: BitmapElement?
        get() {
            val element = renderer.renderElement
            if (element is BitmapElement) {
                return element
            }
            return null
        }

    var operateBitmap = bitmapElement?.originBitmap

    var renderBitmap = bitmapElement?.renderBitmap

    override fun saveState(renderer: BaseRenderer) {
        super.saveState(renderer)
        operateBitmap = bitmapElement?.originBitmap
        renderBitmap = bitmapElement?.renderBitmap
    }

    override fun restoreState(reason: Reason, strategy: Strategy, delegate: CanvasRenderDelegate?) {
        bitmapElement?.originBitmap = operateBitmap
        bitmapElement?.renderBitmap = renderBitmap
        super.restoreState(reason, strategy, delegate)
    }

}