package com.angcyo.canvas.utils

import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.widget.LinearLayout
import com.angcyo.canvas.CanvasView
import com.angcyo.canvas.ScalePictureDrawable
import com.angcyo.canvas.Strategy
import com.angcyo.canvas.items.*
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
): PictureTextItem {
    val renderer = PictureTextItemRenderer(canvasViewBox)
    val result = renderer.addTextRender(text, paint)
    addCentreItemRenderer(renderer, Strategy(Strategy.STRATEGY_TYPE_NORMAL))
    selectedItem(renderer)
    return result
}

/**添加一个文本渲染器*/
@Deprecated("废弃")
fun CanvasView.addTextRenderer(text: String): PictureTextItem {
    val renderer = TextItemRenderer(canvasViewBox)
    renderer.rendererItem = PictureTextItem().apply { this.text = text }
    addCentreItemRenderer(renderer, Strategy(Strategy.STRATEGY_TYPE_NORMAL))
    selectedItem(renderer)
    return renderer.rendererItem!!
}

/**添加一根横线
 * [length] 线的长度
 * [orientation] 线的方向
 * [dash] 是否是虚线*/
@Deprecated("废弃")
fun CanvasView.addLineRenderer(
    length: Float = 100f,
    orientation: Int = LinearLayout.VERTICAL,
    dash: Boolean = false
): LineItem {
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
    addCentreItemRenderer(renderer, Strategy(Strategy.STRATEGY_TYPE_NORMAL))
    selectedItem(renderer)
    return renderer.rendererItem!!
}

/**添加一个形状渲染器*/
@Deprecated("废弃")
fun CanvasView.addShapeRenderer(path: Path, paint: TextPaint? = null): ShapeItem {
    val renderer = ShapeItemRenderer(canvasViewBox)
    val result = renderer.addShape(path, paint)
    addCentreItemRenderer(renderer, Strategy(Strategy.STRATEGY_TYPE_NORMAL))
    selectedItem(renderer)
    return result
}

/**添加一个[Drawable]渲染器*/
fun CanvasView.addDrawableRenderer(drawable: Drawable): DrawableItem {
    val renderer = DrawableItemRenderer<DrawableItem>(canvasViewBox)
    renderer.rendererItem = DrawableItem().apply { this.drawable = drawable }
    addCentreItemRenderer(renderer, Strategy(Strategy.STRATEGY_TYPE_NORMAL))
    selectedItem(renderer)
    return renderer.rendererItem!!
}

/**添加一个[Bitmap]渲染器*/
fun CanvasView.addDrawableRenderer(bitmap: Bitmap, res: Resources = app().resources): DrawableItem {
    return addDrawableRenderer(BitmapDrawable(res, bitmap))
}

/**添加一个文本[Drawable]渲染器 */
@Deprecated("请使用[com.angcyo.canvas.items.renderer.PictureTextItemRendererKt.addPictureTextRenderer]")
fun CanvasView.addDrawableRenderer(
    text: String,
    paint: Paint = createTextPaint(Color.BLACK).apply {
        //init
        textSize = 12 * dp
    }
): DrawableItem {
    val renderer = DrawableItemRenderer<DrawableItem>(canvasViewBox)
    renderer.rendererItem = DrawableItem().apply {
        val width = paint.textWidth(text)
        val height = paint.textHeight()
        this.drawable = ScalePictureDrawable(withPicture(width.toInt(), height.toInt()) {
            drawText(text, 0f, height - paint.descent(), paint)
        })
    }
    addCentreItemRenderer(renderer, Strategy(Strategy.STRATEGY_TYPE_NORMAL))
    selectedItem(renderer)
    return renderer.rendererItem!!
}

/**添加一个[Bitmap]渲染器
 * [BitmapItemRenderer]*/
fun CanvasView.addBitmapRenderer(bitmap: Bitmap): BitmapItem {
    val renderer = BitmapItemRenderer(canvasViewBox)
    val result = renderer.updateBitmap(bitmap)
    addCentreItemRenderer(renderer, Strategy(Strategy.STRATEGY_TYPE_NORMAL))
    selectedItem(renderer)
    return result
}

/**添加一个[Bitmap]渲染器
 * [PictureItemRenderer]*/
fun CanvasView.addPictureBitmapRenderer(bitmap: Bitmap): PictureBitmapItem {
    val renderer = PictureItemRenderer(canvasViewBox)
    val result = renderer.addBitmapRender(bitmap)
    addCentreItemRenderer(renderer, Strategy(Strategy.STRATEGY_TYPE_NORMAL))
    selectedItem(renderer)
    return result
}

/**添加一个文本渲染器*/
fun CanvasView.addPictureTextRender(text: String): PictureTextItem {
    val renderer = PictureItemRenderer(canvasViewBox)
    val result = renderer.addTextRender(text)
    addCentreItemRenderer(renderer, Strategy(Strategy.STRATEGY_TYPE_NORMAL))
    selectedItem(renderer)
    return result
}

/**添加一个形状渲染器*/
fun CanvasView.addPictureShapeRender(path: Path): PictureShapeItem {
    val renderer = PictureItemRenderer(canvasViewBox)
    val result = renderer.addShapeRender(path)
    addCentreItemRenderer(renderer, Strategy(Strategy.STRATEGY_TYPE_NORMAL))
    selectedItem(renderer)
    return result
}