package com.angcyo.canvas.utils

import android.graphics.Bitmap
import com.angcyo.canvas.CanvasView
import com.angcyo.canvas.Strategy
import com.angcyo.canvas.core.IRenderer
import com.angcyo.canvas.items.BaseItem
import com.angcyo.canvas.items.BitmapItem
import com.angcyo.canvas.items.PictureBitmapItem
import com.angcyo.canvas.items.renderer.BitmapItemRenderer
import com.angcyo.canvas.items.renderer.PictureItemRenderer

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
fun IRenderer.getRenderBitmap(): Bitmap? {
    return if (this is BitmapItemRenderer) {
        _rendererItem?.bitmap
    } else if (this is PictureItemRenderer) {
        val item = _rendererItem
        if (item is PictureBitmapItem) {
            item.bitmap
        } else {
            null
        }
    } else {
        null
    }
}

/**更新渲染的[Bitmap]对象, 如果可以*/
fun IRenderer.updateRenderBitmap(
    bitmap: Bitmap,
    strategy: Strategy = Strategy.normal
): BaseItem? {
    return if (this is BitmapItemRenderer) {
        updateBitmap(bitmap, strategy = strategy)
        _rendererItem
    } else if (this is PictureItemRenderer) {
        val item = _rendererItem
        if (item is PictureBitmapItem) {
            updateItemBitmap(bitmap, strategy = strategy)
            item
        } else {
            null
        }
    } else {
        null
    }
}



