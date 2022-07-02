package com.angcyo.canvas.utils

import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.widget.LinearLayout
import com.angcyo.canvas.CanvasView
import com.angcyo.canvas.LinePath
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

//<editor-fold desc="已废弃">

/**添加一个文本渲染器*/
@Deprecated("废弃")
fun CanvasView.addPictureTextRenderer(
    text: String,
    paint: TextPaint = createTextPaint(Color.BLACK).apply {
        //init
        textSize = 12 * dp
    }
): PictureTextItem {
    canvasDelegate.apply {
        val renderer = PictureTextItemRenderer(this)
        val result = renderer.addTextRender(text, paint)
        addCentreItemRenderer(renderer, Strategy.normal)
        selectedItem(renderer)
        return result
    }
}

/**添加一个文本渲染器*/
@Deprecated("废弃")
fun CanvasView.addTextRenderer(text: String): PictureTextItem {
    canvasDelegate.apply {
        val renderer = TextItemRenderer(this)
        renderer._rendererItem = PictureTextItem().apply { this.text = text }
        addCentreItemRenderer(renderer, Strategy.normal)
        selectedItem(renderer)
        return renderer._rendererItem!!
    }
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
    canvasDelegate.apply {
        val renderer = LineItemRenderer(this)
        renderer._rendererItem = LineItem().apply {
            this.length = length
            this.orientation = orientation
            this.dash = dash
            if (dash) {
                this.paint.style = Paint.Style.STROKE
                //因为是用矩形的方式绘制的线, 所以虚线的间隔和长度必须一致
                this.paint.pathEffect = DashPathEffect(floatArrayOf(4 * density, 5 * density), 0f)
            }
        }
        addCentreItemRenderer(renderer, Strategy.normal)
        selectedItem(renderer)
        return renderer._rendererItem!!
    }
}

/**添加一个形状渲染器*/
@Deprecated("废弃")
fun CanvasView.addShapeRenderer(path: Path, paint: TextPaint? = null): ShapeItem {
    canvasDelegate.apply {
        val renderer = ShapeItemRenderer(this)
        val result = renderer.addShape(path, paint)
        addCentreItemRenderer(renderer, Strategy.normal)
        selectedItem(renderer)
        return result
    }
}

//</editor-fold desc="已废弃">


//<editor-fold desc="DrawableItemRenderer">

/**添加一个[Drawable]渲染器*/
fun CanvasView.addDrawableRenderer(drawable: Drawable): DrawableItem {
    canvasDelegate.apply {
        val renderer = DrawableItemRenderer<DrawableItem>(this)
        renderer._rendererItem = DrawableItem().apply { this.drawable = drawable }
        addCentreItemRenderer(renderer, Strategy.normal)
        selectedItem(renderer)
        return renderer._rendererItem!!
    }
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
    canvasDelegate.apply {
        val renderer = DrawableItemRenderer<DrawableItem>(this)
        renderer._rendererItem = DrawableItem().apply {
            val width = paint.textWidth(text)
            val height = paint.textHeight()
            this.drawable = ScalePictureDrawable(withPicture(width.toInt(), height.toInt()) {
                drawText(text, 0f, height - paint.descent(), paint)
            })
        }
        addCentreItemRenderer(renderer, Strategy.normal)
        selectedItem(renderer)
        return renderer._rendererItem!!
    }
}

//</editor-fold desc="DrawableItemRenderer">

//<editor-fold desc="PictureItemRenderer">

/**添加一个文本渲染器*/
fun CanvasView.addPictureTextRender(text: String): PictureTextItem {
    canvasDelegate.apply {
        val renderer = PictureItemRenderer(this)
        val result = renderer.addTextRender(text)
        addCentreItemRenderer(renderer, Strategy.normal)
        selectedItem(renderer)
        return result
    }
}

/**添加一个形状渲染器*/
fun CanvasView.addPictureShapeRender(path: Path): PictureShapeItem {
    canvasDelegate.apply {
        val renderer = PictureItemRenderer(this)
        val result = renderer.addShapeRender(path)
        addCentreItemRenderer(renderer, Strategy.normal)
        selectedItem(renderer)
        return result
    }
}

/**当前渲染的是否是[LinePath]*/
fun BaseItemRenderer<*>.isLineShape(): Boolean {
    val item = getRendererItem()
    if (item is PictureShapeItem) {
        if (item.shapePath is LinePath) {
            return true
        }
    }
    return false
}

//</editor-fold desc="PictureItemRenderer">