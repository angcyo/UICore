package com.angcyo.canvas.items

import android.graphics.Paint
import android.graphics.Path

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/22
 */

@Deprecated("废弃")
class ShapeItem : DrawableItem() {

    var path: Path? = null

    init {
        paint.style = Paint.Style.FILL
        itemName = "Shape"
    }
}