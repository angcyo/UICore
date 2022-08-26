package com.angcyo.canvas.items.renderer

import android.graphics.Paint
import android.graphics.Path
import com.angcyo.canvas.core.ICanvasView
import com.angcyo.canvas.items.PictureShapeItem

/**
 * [PictureShapeItem]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/08/23
 */
class PictureShapeItemRenderer(canvasView: ICanvasView) :
    PictureItemRenderer<PictureShapeItem>(canvasView) {

    init {
        paint.strokeWidth = 1f //* dp
        paint.style = Paint.Style.FILL
    }

    /**设置[shapePath]*/
    fun setRenderShapePath(path: Path): PictureShapeItem {
        val item = PictureShapeItem()
        item.shapePath = path
        _rendererItem = item
        onRendererItemUpdate()
        return item
    }
}