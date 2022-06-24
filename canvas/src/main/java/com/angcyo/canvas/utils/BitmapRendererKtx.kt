package com.angcyo.canvas.utils

import android.graphics.Bitmap
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import com.angcyo.canvas.CanvasView
import com.angcyo.canvas.Strategy
import com.angcyo.canvas.core.IRenderer
import com.angcyo.canvas.items.BaseItem
import com.angcyo.canvas.items.BitmapItem
import com.angcyo.canvas.items.DrawableItem
import com.angcyo.canvas.items.PictureBitmapItem
import com.angcyo.canvas.items.renderer.BaseItemRenderer
import com.angcyo.canvas.items.renderer.BitmapItemRenderer
import com.angcyo.canvas.items.renderer.PictureItemRenderer
import com.angcyo.library.ex.toBitmap

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/05/13
 */

//<editor-fold desc="Bitmap渲染">

/**添加一个[Bitmap]渲染器
 * [BitmapItemRenderer]*/
fun CanvasView.addBitmapRenderer(bitmap: Bitmap): BitmapItem {
    canvasDelegate.apply {
        val renderer = BitmapItemRenderer(this)
        val result = renderer.updateBitmap(bitmap)
        addCentreItemRenderer(renderer, Strategy.normal)
        selectedItem(renderer)
        return result
    }
}

/**添加一个[Bitmap]渲染器
 * [PictureItemRenderer]*/
fun CanvasView.addPictureBitmapRenderer(bitmap: Bitmap): PictureBitmapItem {
    canvasDelegate.apply {
        val renderer = PictureItemRenderer(this)
        val result = renderer.addBitmapRender(bitmap)
        addCentreItemRenderer(renderer, Strategy.normal)
        selectedItem(renderer)
        return result
    }
}

//</editor-fold desc="Bitmap渲染">

/**获取渲染的[Bitmap]对象, 如果有*/
fun IRenderer.getRenderBitmap(origin: Boolean = true): Bitmap? {
    if (this is BitmapItemRenderer) {
        return _rendererItem?.bitmap
    } else if (this is PictureItemRenderer) {
        val item = _rendererItem
        if (item is PictureBitmapItem) {
            return if (origin) {
                item.originBitmap
            } else if (item.bitmap != null) {
                item.bitmap
            } else if (item.drawable is BitmapDrawable) {
                (item.drawable as BitmapDrawable).bitmap
            } else {
                item.drawable?.toBitmap()
            }
        }
    }
    return null
}

fun IRenderer.onlySetRenderBitmap(bitmap: Bitmap?) {
    if (this is BitmapItemRenderer) {
        _rendererItem?.bitmap = bitmap
    } else if (this is PictureItemRenderer) {
        val item = _rendererItem
        if (item is PictureBitmapItem) {
            item.bitmap = bitmap
        }
    }
}

fun IRenderer.onlySetRenderDrawable(drawable: Drawable?) {
    if (this is PictureItemRenderer) {
        val item = _rendererItem
        if (item is DrawableItem) {
            item.drawable = drawable
        }
    }
}

/**获取渲染的Drawable*/
fun IRenderer.getRenderDrawable(): Drawable? {
    if (this is BaseItemRenderer<*>) {
        val item = _rendererItem
        if (item is DrawableItem) {
            return item.drawable
        }
    }
    return null
}

/**更新渲染的[Bitmap]对象, 如果可以*/
fun IRenderer.updateRenderBitmap(
    bitmap: Bitmap,
    strategy: Strategy = Strategy.normal,
    keepBounds: RectF? = null,
    holdData: Map<String, Any?>? = null,
): BaseItem? {
    return if (this is BitmapItemRenderer) {
        updateBitmap(bitmap, strategy = strategy)
        _rendererItem
    } else if (this is PictureItemRenderer) {
        val item = _rendererItem
        if (item is PictureBitmapItem) {
            updateItemBitmap(bitmap, holdData, keepBounds, strategy)
            item
        } else {
            null
        }
    } else {
        null
    }
}

fun IRenderer.updateItemDrawable(
    drawable: Drawable?,
    strategy: Strategy = Strategy.normal,
    keepBounds: RectF? = null,
    holdData: Map<String, Any?>? = null,
): BaseItem? {
    return if (this is PictureItemRenderer) {
        val item = _rendererItem
        if (item is PictureBitmapItem) {
            updateItemDrawable(drawable, holdData, keepBounds, strategy)
            item
        } else {
            null
        }
    } else {
        null
    }
}



