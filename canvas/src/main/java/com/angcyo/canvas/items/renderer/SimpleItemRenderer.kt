package com.angcyo.canvas.items.renderer

import com.angcyo.canvas.core.ICanvasView
import com.angcyo.canvas.items.SimpleItem

/**
 * 简单的渲染器
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/01/07
 */
class SimpleItemRenderer(canvasView: ICanvasView, simpleItem: SimpleItem) :
    BaseItemRenderer<SimpleItem>(canvasView) {

    init {
        setRendererRenderItem(simpleItem)
    }

    override fun onUpdateRendererItem(item: SimpleItem?, oldItem: SimpleItem?) {
        item?.renderBounds?.let {
            initRendererBounds {
                set(it)
            }
        }
        super.onUpdateRendererItem(item, oldItem)
    }
}