package com.angcyo.canvas.items.renderer

import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.graphics.withMatrix
import com.angcyo.canvas.CanvasView
import com.angcyo.canvas.ScalePictureDrawable
import com.angcyo.canvas.core.CanvasViewBox
import com.angcyo.canvas.items.DrawableItem
import com.angcyo.canvas.utils.createTextPaint
import com.angcyo.library.app
import com.angcyo.library.ex.*

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
            adjustSize(
                rendererItem?.drawable?.minimumWidth?.toFloat() ?: 0f,
                rendererItem?.drawable?.minimumHeight?.toFloat() ?: 0f,
                ADJUST_TYPE_LT
            )
        }
    }

    val flipMatrix = Matrix()
    val flipRect = RectF()

    override fun render(canvas: Canvas) {
        rendererItem?.drawable?.let { drawable ->
            val bounds = getRendererBounds()
            //需要处理矩形翻转的情况
            if (drawable is ScalePictureDrawable) {
                drawable.setBounds(
                    bounds.left.toInt(),
                    bounds.top.toInt(),
                    bounds.right.toInt(),
                    bounds.bottom.toInt()
                )
                drawable.draw(canvas)
            } else {
                bounds.adjustFlipRect(flipRect)
                var sx = 1f
                var sy = 1f
                if (getBounds().isFlipHorizontal) {
                    sx = -1f
                }
                if (getBounds().isFlipVertical) {
                    sy = -1f
                }
                flipMatrix.reset()
                flipMatrix.postScale(sx, sy, flipRect.centerX(), flipRect.centerY())
                canvas.withMatrix(flipMatrix) {
                    drawable.setBounds(
                        flipRect.left.toInt(),
                        flipRect.top.toInt(),
                        flipRect.right.toInt(),
                        flipRect.bottom.toInt()
                    )
                    drawable.draw(canvas)
                }
            }
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