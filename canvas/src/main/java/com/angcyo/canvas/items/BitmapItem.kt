package com.angcyo.canvas.items

import android.graphics.Bitmap
import com.angcyo.canvas.utils.CanvasConstant

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/22
 */
class BitmapItem : BaseItem() {

    /**可绘制的对象*/
    var bitmap: Bitmap? = null

    init {
        itemLayerName = "Bitmap"
        dataType = CanvasConstant.DATA_TYPE_BITMAP
    }

}