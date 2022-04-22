package com.angcyo.canvas.items.renderer

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import com.angcyo.canvas.CanvasView
import com.angcyo.canvas.ScalePictureDrawable
import com.angcyo.canvas.core.CanvasViewBox
import com.angcyo.canvas.items.DrawableItem
import com.angcyo.canvas.utils.createTextPaint
import com.angcyo.library.app
import com.angcyo.library.ex.dp
import com.angcyo.library.ex.textHeight
import com.angcyo.library.ex.textWidth
import com.angcyo.library.ex.withPicture

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/11
 */
open class DrawableItemRenderer<T : DrawableItem>(canvasViewBox: CanvasViewBox) :
    BaseItemRenderer<T>(canvasViewBox) {

    override fun onUpdateRendererItem(item: T?, oldItem: T?) {
        super.onUpdateRendererItem(item, oldItem)
        if (item != oldItem) {
            val bounds = getBounds()
            if (bounds.isEmpty) {
                initBounds()
            }
        }
    }

    open fun initBounds() {
        changeBounds {
            set(
                0f,
                0f,
                rendererItem?.drawable?.minimumWidth?.toFloat() ?: 0f,
                rendererItem?.drawable?.minimumHeight?.toFloat() ?: 0f
            )
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
    val renderer = DrawableItemRenderer<DrawableItem>(canvasViewBox)
    renderer.rendererItem = DrawableItem().apply { this.drawable = drawable }
    addCentreItemRenderer(renderer)
    selectedItem(renderer)
}

/**添加一个[Bitmap]渲染器*/
fun CanvasView.addDrawableRenderer(bitmap: Bitmap, res: Resources = app().resources) {
    addDrawableRenderer(BitmapDrawable(res, bitmap))
}

/**添加一个文本[Drawable]渲染器 */
@Deprecated("请使用[com.angcyo.canvas.items.renderer.PictureTextItemRendererKt.addPictureTextRenderer]")
fun CanvasView.addDrawableRenderer(
    text: String,
    paint: Paint = createTextPaint(Color.BLACK).apply {
        //init
        textSize = 12 * dp
    }
) {
    val renderer = DrawableItemRenderer<DrawableItem>(canvasViewBox)
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