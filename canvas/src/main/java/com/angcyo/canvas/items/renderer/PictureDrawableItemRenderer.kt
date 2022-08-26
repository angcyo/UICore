package com.angcyo.canvas.items.renderer

import android.graphics.drawable.Drawable
import com.angcyo.canvas.core.ICanvasView
import com.angcyo.canvas.items.PictureItem
import com.angcyo.canvas.items.setHoldData
import com.angcyo.canvas.utils.CanvasConstant
import com.angcyo.canvas.utils.CanvasDataHandleOperate
import com.angcyo.gcode.GCodeDrawable
import com.angcyo.library.component.ScalePictureDrawable

/**
 * 渲染 [ScalePictureDrawable]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/08/26
 */
class PictureDrawableItemRenderer(canvasView: ICanvasView) :
    PictureItemRenderer<PictureItem>(canvasView) {

    override fun setRenderDrawable(drawable: Drawable?): PictureItem {
        val item = PictureItem()
        item.drawable = drawable
        if (drawable is GCodeDrawable) {
            item.dataType = CanvasConstant.DATA_TYPE_GCODE
            item.dataMode = CanvasConstant.DATA_MODE_GCODE

            item.setHoldData(CanvasDataHandleOperate.KEY_GCODE, drawable.gCodeData)
        } else {
            item.dataType = CanvasConstant.DATA_TYPE_BITMAP
            item.dataMode = CanvasConstant.DATA_MODE_GREY
        }
        _rendererItem = item
        onRendererItemUpdate()
        return item
    }

}