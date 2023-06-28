package com.angcyo.canvas.render.renderer

import android.graphics.Canvas
import android.graphics.Path
import com.angcyo.library.canvas.annotation.CanvasInsideCoordinate
import com.angcyo.canvas.render.core.CanvasRenderDelegate
import com.angcyo.canvas.render.core.component.CanvasRenderProperty
import com.angcyo.canvas.render.data.RenderParams
import com.angcyo.canvas.render.util.createRenderPaint
import com.angcyo.library.annotation.Pixel
import com.angcyo.library.ex.computePathBounds

/**
 * 简单的[Path]绘制渲染器
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2023/06/02
 */
class PathRenderer(@CanvasInsideCoordinate @Pixel var path: Path? = null) : BaseRenderer() {

    /**画笔*/
    var pathPaint = createRenderPaint()

    init {
        onlyRenderOnInside()
        updatePath(path)
    }

    /**更新路径*/
    fun updatePath(path: Path?, delegate: CanvasRenderDelegate? = null) {
        this.path = path
        renderProperty = if (path == null) {
            null
        } else {
            CanvasRenderProperty().apply {
                path.computePathBounds(_renderBounds)
                initWithRect(_renderBounds, 0f)
            }
        }
        delegate?.refresh()
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
        path?.let {
            canvas.drawPath(it, pathPaint)
        }
    }

    override fun renderOnOutside(canvas: Canvas, params: RenderParams) {
        super.renderOnOutside(canvas, params)
        path?.let {
            canvas.drawPath(it, pathPaint)
        }
    }

    override fun renderOnView(canvas: Canvas, params: RenderParams) {
        super.renderOnView(canvas, params)
        path?.let {
            canvas.drawPath(it, pathPaint)
        }
    }

}