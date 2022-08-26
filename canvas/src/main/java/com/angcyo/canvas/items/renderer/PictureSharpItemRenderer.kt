package com.angcyo.canvas.items.renderer

import com.angcyo.canvas.core.ICanvasView
import com.angcyo.canvas.items.PictureSharpItem
import com.angcyo.canvas.items.setHoldData
import com.angcyo.canvas.utils.CanvasDataHandleOperate
import com.pixplicity.sharp.SharpDrawable

/**
 * svg [SharpDrawable] 渲染器
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/08/26
 */
class PictureSharpItemRenderer(canvasView: ICanvasView) :
    PictureItemRenderer<PictureSharpItem>(canvasView) {

    /**设置渲染的图片*/
    fun setRenderSharp(drawable: SharpDrawable?): PictureSharpItem {
        val item = PictureSharpItem()
        item.sharpDrawable = drawable
        drawable?.let {
            item.setHoldData(CanvasDataHandleOperate.KEY_SVG, drawable.pathList)
        }
        _rendererItem = item
        onRendererItemUpdate()
        return item
    }

}