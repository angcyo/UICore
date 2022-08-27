package com.angcyo.canvas.items

import android.graphics.Paint
import com.angcyo.canvas.utils.CanvasConstant
import com.angcyo.gcode.GCodeDrawable

/**
 * GCode可渲染Item
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/08/27
 */
class PictureGCodeItem(
    /**gcode原始数据*/
    val gCode: String,
    /**可视化对象*/
    val gCodeDrawable: GCodeDrawable
) : PictureDrawableItem() {

    init {
        itemLayerName = "GCode"
        dataType = CanvasConstant.DATA_TYPE_GCODE
        dataMode = CanvasConstant.DATA_MODE_GCODE
    }

    override fun updateItem(paint: Paint) {
        this.drawable = gCodeDrawable
        this.itemWidth = gCodeDrawable.gCodeBound.width()
        this.itemHeight = gCodeDrawable.gCodeBound.height()
    }

}