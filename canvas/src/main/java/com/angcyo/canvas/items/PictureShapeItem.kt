package com.angcyo.canvas.items

import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import com.angcyo.canvas.LinePath
import com.angcyo.canvas.ScalePictureDrawable
import com.angcyo.canvas.core.MmValueUnit
import com.angcyo.library.ex.*
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

    /**形状的真正的bounds*/
    val shapeBounds = RectF()

    /**线段描边时, 用虚线绘制*/
    val lineStrokeEffect = DashPathEffect(floatArrayOf(4 * density, 5 * density), 0f)

    init {
        paint.strokeWidth = 1 * dp
        paint.style = Paint.Style.FILL_AND_STROKE
        itemName = "Shape"
    }

    override fun updatePictureDrawable() {
        shapePath?.let { path ->
            val unit = MmValueUnit()
            val strokeWidth = paint.strokeWidth
            path.computeBounds(shapeBounds, true)

            val shapeWidth = if (itemWidth > 0) {
                itemWidth
            } else if (!shapeBounds.isNoSize()) {
                shapeBounds.width() + strokeWidth
            } else {
                unit.convertValueToPixel(SHAPE_DEFAULT_WIDTH) + strokeWidth
            }

            val shapeHeight = if (itemHeight > 0) {
                itemHeight
            } else if (!shapeBounds.isNoSize()) {
                shapeBounds.height() + strokeWidth
            } else {
                unit.convertValueToPixel(SHAPE_DEFAULT_HEIGHT) + strokeWidth
            }

            val drawableWidth = shapeWidth.ceil().roundToInt()
            val drawableHeight = shapeHeight.ceil().roundToInt()

            val drawable = ScalePictureDrawable(withPicture(drawableWidth, drawableHeight) {
                var dx = strokeWidth / 2
                var dy = dx
                if (!shapeBounds.isNoSize()) {
                    dx -= shapeBounds.left
                    dy -= shapeBounds.top
                }

                translate(dx, dy)
                //线段的描边用虚线处理处理
                if (path is LinePath) {
                    if (paint.style == Paint.Style.STROKE) {
                        paint.pathEffect = lineStrokeEffect
                    } else {
                        paint.pathEffect = null
                    }
                }
                drawPath(path, paint)
            })

            this.drawable = drawable
            this.itemWidth = drawable.minimumWidth.toFloat()
            this.itemHeight = drawable.minimumHeight.toFloat()
        }
    }

}