package com.angcyo.canvas.items

import android.graphics.Path
import com.angcyo.canvas.ScalePictureDrawable
import com.angcyo.canvas.core.MmValueUnit
import com.angcyo.library.ex.ceil
import com.angcyo.library.ex.dp
import com.angcyo.library.ex.withPicture
import kotlin.math.roundToInt

/**
 * 形状组件渲染数据
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/29
 */
class PictureShapeItem : PictureItem() {

    companion object {
        /**默认的形状宽度, cm单位*/
        const val SHAPE_DEFAULT_WIDTH = 10f

        /**默认的形状高度, cm单位*/
        const val SHAPE_DEFAULT_HEIGHT = 10f
    }

    /**需要绘制的形状[Path]*/
    var shapePath: Path? = null

    init {
        paint.strokeWidth = 1 * dp
    }

    override fun updatePictureDrawable() {
        shapePath?.let { path ->
            val unit = MmValueUnit()

            val shapeWidth = if (itemWidth > 0) {
                itemWidth
            } else {
                unit.convertValueToPixel(SHAPE_DEFAULT_WIDTH)
            }

            val shapeHeight = if (itemHeight > 0) {
                itemHeight
            } else {
                unit.convertValueToPixel(SHAPE_DEFAULT_HEIGHT)
            }

            val drawable = ScalePictureDrawable(
                withPicture(shapeWidth.ceil().roundToInt(), shapeHeight.ceil().roundToInt()) {
                    drawPath(path, paint)
                })

            this.drawable = drawable
            this.itemWidth = shapeWidth
            this.itemHeight = shapeHeight
        }
    }

}