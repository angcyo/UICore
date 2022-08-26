package com.angcyo.canvas.items

import android.graphics.Paint
import com.angcyo.canvas.utils.CanvasConstant
import com.angcyo.canvas.utils.CanvasDataHandleOperate
import com.angcyo.svg.Svg
import com.pixplicity.sharp.SharpDrawable

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/08/26
 */
class PictureSharpItem : PictureItem() {

    var sharpDrawable: SharpDrawable? = null

    init {
        itemLayerName = "SVG"
        dataType = CanvasConstant.DATA_TYPE_SVG
        dataMode = CanvasConstant.DATA_MODE_GCODE
    }

    override fun updateItem(paint: Paint) {
        val sharpDrawable = sharpDrawable ?: return
        if (sharpDrawable.pathList.isNotEmpty()) {
            val newDrawable = Svg.loadPathList(
                sharpDrawable.pathList,
                sharpDrawable.pathBounds,
                paint.style,
                null,
                0,
                0
            )
            setHoldData(CanvasDataHandleOperate.KEY_SVG, newDrawable.pathList)

            this.drawable = newDrawable
            this.itemWidth = sharpDrawable.pathBounds.width()
            this.itemHeight = sharpDrawable.pathBounds.height()
        }
    }
}