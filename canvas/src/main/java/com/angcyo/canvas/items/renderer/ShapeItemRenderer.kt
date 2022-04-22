package com.angcyo.canvas.items.renderer

import android.graphics.Path
import android.graphics.RectF
import android.text.TextPaint
import com.angcyo.canvas.CanvasView
import com.angcyo.canvas.ScalePictureDrawable
import com.angcyo.canvas.core.CanvasViewBox
import com.angcyo.canvas.items.ShapeItem
import com.angcyo.library.ex.withPicture

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/22
 */
class ShapeItemRenderer(canvasViewBox: CanvasViewBox) :
    DrawableItemRenderer<ShapeItem>(canvasViewBox) {

    val shapeBounds = RectF()

    /**添加一个shape*/
    fun addShape(path: Path, paint: TextPaint? = null) {
        val bounds = getBounds()
        path.computeBounds(bounds, true)
        shapeBounds.set(bounds)
        rendererItem = ShapeItem().apply {
            this.path = path
            this.paint = paint ?: this.paint
            drawable =
                ScalePictureDrawable(withPicture(bounds.width().toInt(), bounds.height().toInt()) {
                    drawPath(path, this@apply.paint)
                })
        }
        refresh()
    }
}

/**添加一个形状渲染器*/
fun CanvasView.addShapeRenderer(path: Path, paint: TextPaint? = null) {
    val renderer = ShapeItemRenderer(canvasViewBox)
    renderer.addShape(path, paint)
    addCentreItemRenderer(renderer)
    selectedItem(renderer)
}