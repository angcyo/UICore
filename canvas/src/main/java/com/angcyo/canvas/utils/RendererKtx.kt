package com.angcyo.canvas.utils

import android.graphics.Bitmap
import android.graphics.Path
import android.graphics.drawable.Drawable
import com.angcyo.canvas.CanvasDelegate
import com.angcyo.canvas.LinePath
import com.angcyo.canvas.Strategy
import com.angcyo.canvas.items.*
import com.angcyo.canvas.items.renderer.BaseItemRenderer
import com.angcyo.canvas.items.renderer.DrawableItemRenderer
import com.angcyo.canvas.items.renderer.PictureItemRenderer
import com.angcyo.canvas.items.renderer.PictureTextItemRenderer
import com.angcyo.gcode.GCodeDrawable
import com.angcyo.library.ex.toBitmap
import com.pixplicity.sharp.SharpDrawable

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/29
 */

/**当前渲染的是否是[LinePath]*/
fun BaseItemRenderer<*>.isLineShape(): Boolean {
    val item = getRendererRenderItem()
    if (item is PictureShapeItem) {
        if (item.shapePath is LinePath) {
            return true
        }
    }
    return false
}

/**获取一个用于雕刻的图片数据*/
fun BaseItemRenderer<*>.getEngraveBitmap(): Bitmap? {
    val item = getRendererRenderItem()
    if (item is PictureBitmapItem) {
        return item.modifyBitmap ?: item.originBitmap
    }
    return preview()?.toBitmap()
}

/**获取GCode数据, 如固有*/
fun BaseItemRenderer<*>.getGCodeText(): String? {
    val item = getRendererRenderItem()
    if (item is PictureBitmapItem) {
        if (item.dataMode == CanvasConstant.DATA_MODE_GCODE) {
            return item.data as? String
        }
    } else if (item is PictureGCodeItem) {
        return item.gCode
    }
    return null
}

fun BaseItemRenderer<*>.getPathList(): List<Path>? {
    val item = getRendererRenderItem()
    if (item is PictureSharpItem) {
        return item.sharpDrawable.pathList
    }
    return null
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
    val renderer = PictureItemRenderer<PictureShapeItem>(this)
    val item = PictureShapeItem(path)
    renderer.setRendererRenderItem(item)
    addCentreItemRenderer(renderer, Strategy.normal)
    selectedItem(renderer)
    return item
}

/**添加一个绘制[bitmap]的渲染器*/
fun CanvasDelegate.addPictureBitmapRenderer(bitmap: Bitmap): PictureBitmapItem {
    val renderer = PictureItemRenderer<PictureBitmapItem>(this)
    val item = PictureBitmapItem(bitmap)
    renderer.setRendererRenderItem(item)
    addCentreItemRenderer(renderer, Strategy.normal)
    selectedItem(renderer)
    return item
}

/**添加一个绘制[drawable]的渲染器
 * [DrawableItemRenderer]
 * [com.angcyo.gcode.GCodeDrawable]
 * */
fun CanvasDelegate.addPictureDrawableRenderer(drawable: Drawable?): PictureDrawableItem {
    val renderer = PictureItemRenderer<PictureDrawableItem>(this)
    val item = PictureDrawableItem()
    item.drawable = drawable
    renderer.setRendererRenderItem(item)
    addCentreItemRenderer(renderer, Strategy.normal)
    selectedItem(renderer)
    return item
}

/**添加一个绘制[SharpDrawable]的渲染器
 * [com.pixplicity.sharp.SharpDrawable]
 * */
fun CanvasDelegate.addPictureSharpRenderer(
    svg: String,
    drawable: SharpDrawable
): PictureDrawableItem {
    if (drawable.pathList.isNullOrEmpty()) {
        //无path数据
        return addPictureDrawableRenderer(drawable)
    } else {
        val renderer = PictureItemRenderer<PictureSharpItem>(this)
        val item = PictureSharpItem(svg, drawable)
        renderer.setRendererRenderItem(item)
        addCentreItemRenderer(renderer, Strategy.normal)
        selectedItem(renderer)
        return item
    }
}

/**添加一个绘制[PictureGCodeItem]的渲染器
 * [com.angcyo.gcode.GCodeDrawable]
 * */
fun CanvasDelegate.addPictureGCodeRenderer(
    gCode: String,
    drawable: GCodeDrawable
): PictureGCodeItem {
    val renderer = PictureItemRenderer<PictureGCodeItem>(this)
    val item = PictureGCodeItem(gCode, drawable)
    renderer.setRendererRenderItem(item)
    addCentreItemRenderer(renderer, Strategy.normal)
    selectedItem(renderer)
    return item
}

//</editor-fold desc="PictureItemRenderer">