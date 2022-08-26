package com.angcyo.canvas.utils

import android.graphics.Bitmap
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import com.angcyo.canvas.Strategy
import com.angcyo.canvas.core.IRenderer
import com.angcyo.canvas.items.BaseItem
import com.angcyo.canvas.items.DrawableItem
import com.angcyo.canvas.items.PictureBitmapItem
import com.angcyo.canvas.items.renderer.BaseItemRenderer
import com.angcyo.canvas.items.renderer.PictureBitmapItemRenderer
import com.angcyo.library.ex.toBitmap

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/05/13
 */

/**获取渲染的[Bitmap]对象, 如果有*/
fun IRenderer.getRenderBitmap(origin: Boolean = true): Bitmap? {
    if (this is PictureBitmapItemRenderer) {
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
    if (this is PictureBitmapItemRenderer) {
        val item = _rendererItem
        if (item is PictureBitmapItem) {
            item.bitmap = bitmap
        }
    }
}

fun IRenderer.onlySetRenderDrawable(drawable: Drawable?) {
    if (this is PictureBitmapItemRenderer) {
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
    return if (this is PictureBitmapItemRenderer) {
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
    return if (this is PictureBitmapItemRenderer) {
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



