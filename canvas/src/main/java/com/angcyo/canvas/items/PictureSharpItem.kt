package com.angcyo.canvas.items

import android.graphics.Paint
import com.angcyo.canvas.utils.CanvasConstant
import com.pixplicity.sharp.SharpDrawable

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/08/26
 */
class PictureSharpItem(
    /**svg原始数据*/
    val svg: String,
    /**可视化对象*/
    val sharpDrawable: SharpDrawable
) : PictureDrawableItem() {

    init {
        itemLayerName = "SVG"
        dataType = CanvasConstant.DATA_TYPE_SVG
        dataMode = CanvasConstant.DATA_MODE_GCODE
    }

    override fun updateItem(paint: Paint) {
        val sharpDrawable = sharpDrawable
        if (!sharpDrawable.pathList.isNullOrEmpty()) {
            /*val newDrawable = Svg.loadPathList(
                sharpDrawable.pathList,
                sharpDrawable.pathBounds,
                paint.style,
                null,
                0,
                0
            )
            this.drawable = newDrawable*/
            this.dataMode = CanvasConstant.DATA_MODE_GCODE
            this.itemWidth = sharpDrawable.pathBounds.width()
            this.itemHeight = sharpDrawable.pathBounds.height()
            this.drawable = createPathDrawable(sharpDrawable.pathList, itemWidth, itemHeight, paint)
        } else {
            this.drawable = sharpDrawable
            this.dataMode = CanvasConstant.DATA_MODE_GREY
            this.itemWidth = sharpDrawable.intrinsicWidth.toFloat()
            this.itemHeight = sharpDrawable.intrinsicHeight.toFloat()
        }
    }

    override fun updateDrawable(paint: Paint, width: Float, height: Float) {
        drawable = createPathDrawable(sharpDrawable.pathList, width, height, paint)
    }
}