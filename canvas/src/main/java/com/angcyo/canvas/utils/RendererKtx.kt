package com.angcyo.canvas.utils

import android.graphics.Bitmap
import android.graphics.Path
import android.graphics.drawable.Drawable
import com.angcyo.canvas.CanvasDelegate
import com.angcyo.canvas.LinePath
import com.angcyo.canvas.Strategy
import com.angcyo.canvas.items.PictureBitmapItem
import com.angcyo.canvas.items.PictureItem
import com.angcyo.canvas.items.PictureShapeItem
import com.angcyo.canvas.items.PictureTextItem
import com.angcyo.canvas.items.renderer.*
import com.pixplicity.sharp.SharpDrawable

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/29
 */

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

//<editor-fold desc="PictureItemRenderer">

/**添加一个绘制[text]渲染器*/
fun CanvasDelegate.addPictureTextRender(text: String): PictureTextItem {
    val renderer = PictureTextItemRenderer(this)
    val item = renderer.setRenderText(text)
    addCentreItemRenderer(renderer, Strategy.normal)
    selectedItem(renderer)
    return item
}

/**添加一个绘制[path]的渲染器*/
fun CanvasDelegate.addPictureShapeRenderer(path: Path): PictureShapeItem {
    val renderer = PictureShapeItemRenderer(this)
    val item = renderer.setRenderShapePath(path)
    addCentreItemRenderer(renderer, Strategy.normal)
    selectedItem(renderer)
    return item
}

/**添加一个绘制[bitmap]的渲染器*/
fun CanvasDelegate.addPictureBitmapRenderer(bitmap: Bitmap): PictureBitmapItem {
    val renderer = PictureBitmapItemRenderer(this)
    val item = renderer.setRenderBitmap(bitmap)
    addCentreItemRenderer(renderer, Strategy.normal)
    selectedItem(renderer)
    return item
}

/**添加一个绘制[drawable]的渲染器
 * [DrawableItemRenderer]
 * [com.angcyo.gcode.GCodeDrawable]
 * */
fun CanvasDelegate.addPictureDrawableRenderer(drawable: Drawable?): PictureItem {
    val renderer = PictureDrawableItemRenderer(this)
    val item = renderer.setRenderDrawable(drawable)
    addCentreItemRenderer(renderer, Strategy.normal)
    selectedItem(renderer)
    return item
}

/**添加一个绘制[SharpDrawable]的渲染器
 * [com.pixplicity.sharp.SharpDrawable]
 * */
fun CanvasDelegate.addPictureSharpRenderer(drawable: SharpDrawable?): PictureItem {
    if (drawable?.pathList.isNullOrEmpty()) {
        //无path数据
        return addPictureDrawableRenderer(drawable)
    } else {
        val renderer = PictureSharpItemRenderer(this)
        val item = renderer.setRenderSharp(drawable)
        addCentreItemRenderer(renderer, Strategy.normal)
        selectedItem(renderer)
        return item
    }
}

//</editor-fold desc="PictureItemRenderer">