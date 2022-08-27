package com.angcyo.canvas.items

import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import com.angcyo.canvas.LinePath
import com.angcyo.canvas.utils.CanvasConstant
import com.angcyo.library.component.ScalePictureDrawable
import com.angcyo.library.ex.*

/**
 * 形状组件渲染数据
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/29
 */
class PictureShapeItem(
    /**需要绘制的形状[Path]数据*/
    val shapePath: Path
) : PictureDrawableItem() {

    companion object {
        /**默认的形状宽度, cm单位*/
        const val SHAPE_DEFAULT_WIDTH = 10f

        /**默认的形状高度, cm单位*/
        const val SHAPE_DEFAULT_HEIGHT = 10f
    }

    /**线段描边时, 用虚线绘制*/
    val lineStrokeEffect = DashPathEffect(floatArrayOf(4 * density, 5 * density), 0f)

    init {
        itemLayerName = "Shape"
        dataType = CanvasConstant.DATA_TYPE_PATH
        dataMode = CanvasConstant.DATA_MODE_GCODE
    }

    /**将[shapePath]转换成可以渲染的[Drawable]*/
    override fun updateItem(paint: Paint) {
        val path = shapePath
        val shapeBounds = RectF()
        path.computeBounds(shapeBounds, true)
        val strokeWidth = paint.strokeWidth

        val shapeWidth = (shapeBounds.width() + strokeWidth).toInt()
        val shapeHeight = (shapeBounds.height() + strokeWidth).toInt()

        val picture = withPicture(shapeWidth, shapeHeight) {

            val dx = strokeWidth / 2 - shapeBounds.left
            val dy = strokeWidth / 2 - shapeBounds.top

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
        }

        //draw
        val drawable = ScalePictureDrawable(picture)

        this.drawable = drawable
        this.itemWidth = shapeWidth.toFloat()
        this.itemHeight = shapeHeight.toFloat()
    }

}