package com.angcyo.canvas.items.renderer

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import com.angcyo.canvas.CanvasView
import com.angcyo.canvas.ScalePictureDrawable
import com.angcyo.canvas.core.CanvasViewBox
import com.angcyo.canvas.items.DrawableItem
import com.angcyo.canvas.utils.createTextPaint
import com.angcyo.library.ex.dp
import com.angcyo.library.ex.textHeight
import com.angcyo.library.ex.textWidth
import com.angcyo.library.ex.withPicture

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/11
 */
class DrawableItemRenderer(canvasViewBox: CanvasViewBox) :
    BaseItemRenderer<DrawableItem>(canvasViewBox) {

    override fun updateRendererItem(item: DrawableItem?, oldItem: DrawableItem?) {
        super.updateRendererItem(item, oldItem)
        if (item != oldItem) {
            val bounds = getBounds()
            if (bounds.isEmpty) {
                changeBounds {
                    set(
                        0f,
                        0f,
                        rendererItem?.drawable?.minimumWidth?.toFloat() ?: 0f,
                        rendererItem?.drawable?.minimumHeight?.toFloat() ?: 0f
                    )
                }
            }
        }
    }

    override fun render(canvas: Canvas) {
        rendererItem?.drawable?.let { drawable ->
            val bounds = getRendererBounds()
            drawable.setBounds(
                bounds.left.toInt(),
                bounds.top.toInt(),
                bounds.right.toInt(),
                bounds.bottom.toInt()
            )
            drawable.draw(canvas)
        }
    }
}

/**添加一个[Drawable]渲染器*/
fun CanvasView.addDrawableRenderer(drawable: Drawable) {
    val renderer = DrawableItemRenderer(canvasViewBox)
    renderer.rendererItem = DrawableItem().apply { this.drawable = drawable }
    addCentreItemRenderer(renderer)
    selectedItem(renderer)
}

/**添加一个文本[Drawable]渲染器*/
fun CanvasView.addDrawableRenderer(
    text: String,
    paint: Paint = createTextPaint(Color.BLACK).apply {
        //init
        textSize = 12 * dp
    }
) {
    val renderer = DrawableItemRenderer(canvasViewBox)
    renderer.rendererItem = DrawableItem().apply {
        val width = paint.textWidth(text)
        val height = paint.textHeight()
        this.drawable = ScalePictureDrawable(withPicture(width.toInt(), height.toInt()) {
            drawText(text, 0f, height - paint.descent(), paint)
        })
    }
    addCentreItemRenderer(renderer)
    selectedItem(renderer)
}