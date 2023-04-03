package com.angcyo.canvas.render.renderer

import android.graphics.Canvas
import android.graphics.RectF
import android.graphics.drawable.Drawable
import com.angcyo.canvas.render.core.CanvasRenderDelegate
import com.angcyo.canvas.render.core.IRenderer
import com.angcyo.canvas.render.core.component.CanvasRenderProperty
import com.angcyo.canvas.render.data.RenderParams
import com.angcyo.library.ex.remove

/**
 * 简单的渲染器
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/03/30
 */
class SimpleInsideRenderer(bounds: RectF, drawable: Drawable?) : BaseRenderer() {

    init {
        renderProperty = CanvasRenderProperty().apply {
            initWithRect(bounds, 0f)
        }
        renderDrawable = drawable
        renderFlags = renderFlags.remove(IRenderer.RENDERER_FLAG_ON_OUTSIDE)
            .remove(IRenderer.RENDERER_FLAG_ON_VIEW)
    }

    override fun isVisibleInRender(
        delegate: CanvasRenderDelegate?,
        fullIn: Boolean,
        def: Boolean
    ): Boolean {
        return super.isVisibleInRender(delegate, fullIn, def)
    }

    override fun renderOnInside(canvas: Canvas, params: RenderParams) {
        super.renderOnInside(canvas, params)
    }

}