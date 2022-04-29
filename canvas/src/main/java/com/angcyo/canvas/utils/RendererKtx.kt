package com.angcyo.canvas.utils

import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.widget.LinearLayout
import com.angcyo.canvas.CanvasView
import com.angcyo.canvas.ScalePictureDrawable
import com.angcyo.canvas.items.DrawableItem
import com.angcyo.canvas.items.LineItem
import com.angcyo.canvas.items.PictureTextItem
import com.angcyo.canvas.items.renderer.*
import com.angcyo.library.app
import com.angcyo.library.ex.*

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/29
 */

/**添加一个文本渲染器*/
@Deprecated("废弃")
fun CanvasView.addPictureTextRenderer(
    text: String,
    paint: TextPaint = createTextPaint(Color.BLACK).apply {
        //init
        textSize = 12 * dp
    }
) {
    val renderer = PictureTextItemRenderer(canvasViewBox)
    renderer.addTextRender(text, paint)
    addCentreItemRenderer(renderer)
    selectedItem(renderer)
}

/**添加一个文本渲染器*/
@Deprecated("废弃")
fun CanvasView.addTextRenderer(text: String) {
    val renderer = TextItemRenderer(canvasViewBox)
    renderer.rendererItem = PictureTextItem().apply { this.text = text }
    addCentreItemRenderer(renderer)
    selectedItem(renderer)
}

/**添加一个文本渲染器*/
fun CanvasView.addPictureTextRender(text: String) {
    val renderer = PictureItemRenderer(canvasViewBox)
    renderer.addTextRender(text)
    addCentreItemRenderer(renderer)
    selectedItem(renderer)
}

/**添加一根横线
 * [length] 线的长度
 * [orientation] 线的方向
 * [dash] 是否是虚线*/
fun CanvasView.addLineRenderer(
    length: Float = 100f,
    orientation: Int = LinearLayout.VERTICAL,
    dash: Boolean = false
) {
    val renderer = LineItemRenderer(canvasViewBox)
    renderer.rendererItem = LineItem().apply {
        this.length = length
        this.orientation = orientation
        this.dash = dash
        if (dash) {
            this.paint.style = Paint.Style.STROKE
            //因为是用矩形的方式绘制的线, 所以虚线的间隔和长度必须一致
            this.paint.pathEffect = DashPathEffect(floatArrayOf(4 * density, 5 * density), 0f)
        }
    }
    addCentreItemRenderer(renderer)
    selectedItem(renderer)
}

/**添加一个形状渲染器*/
@Deprecated("废弃")
fun CanvasView.addShapeRenderer(path: Path, paint: TextPaint? = null) {
    val renderer = ShapeItemRenderer(canvasViewBox)
    renderer.addShape(path, paint)
    addCentreItemRenderer(renderer)
    selectedItem(renderer)
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

/**添加一个[Bitmap]渲染器*/
fun CanvasView.addBitmapRenderer(bitmap: Bitmap) {
    val renderer = BitmapItemRenderer(canvasViewBox)
    renderer.updateBitmap(bitmap)
    addCentreItemRenderer(renderer)
    selectedItem(renderer)
}
