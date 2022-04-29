package com.angcyo.canvas.utils

import android.graphics.Path
import com.angcyo.canvas.core.MmValueUnit
import com.angcyo.canvas.items.PictureShapeItem

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/29
 */
object ShapesHelper {

    val defaultWidth: Float
        get() = MmValueUnit().convertValueToPixel(PictureShapeItem.SHAPE_DEFAULT_WIDTH)

    val defaultHeight: Float
        get() = MmValueUnit().convertValueToPixel(PictureShapeItem.SHAPE_DEFAULT_HEIGHT)

    /**正方形Path*/
    fun squarePath(width: Float = defaultWidth, height: Float = defaultHeight): Path =
        Path().apply {
            moveTo(0f, 0f)
            lineTo(width, 0f)
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }

}