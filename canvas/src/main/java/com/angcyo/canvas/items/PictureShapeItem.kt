package com.angcyo.canvas.items

import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import com.angcyo.canvas.LinePath
import com.angcyo.canvas.utils.CanvasConstant
import com.angcyo.library.component.ScalePictureDrawable
import com.angcyo.library.ex.*
import kotlin.math.max

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
    val lineStrokeEffect = DashPathEffect(floatArrayOf(2 * density, 3 * density), 0f)

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

        val shapeWidth = max(1, shapeBounds.width().toInt())
        var shapeHeight = max(1, shapeBounds.height().toInt())
        if (path is LinePath) {
            shapeHeight = paint.strokeWidth.toInt()
        }

        val picture = withPicture(shapeWidth, shapeHeight) {
            val strokeWidth = paint.strokeWidth

            //偏移到路径开始的位置
            val dx = -strokeWidth / 2 - shapeBounds.left
            val dy = -strokeWidth / 2 - shapeBounds.top

            translate(dx, dy)

            //缩放边框, 以便于不会被Bounds裁剪
            val drawWidth = shapeWidth - strokeWidth * 2
            val drawHeight = shapeHeight - strokeWidth * 2
            scale(
                drawWidth / shapeWidth,
                drawHeight / shapeHeight,
                shapeWidth / 2f,
                shapeHeight / 2f
            )

            //线段的描边用虚线处理处理
            if (path is LinePath) {
                val linePaint = Paint(paint)
                linePaint.style = Paint.Style.STROKE //线只能使用此模式¬
                if (paint.style == Paint.Style.STROKE) {
                    linePaint.pathEffect = lineStrokeEffect //虚线
                } else {
                    linePaint.pathEffect = null //实线
                }
                drawPath(path, linePaint)
            } else {
                drawPath(path, paint)
            }
        }

        //draw
        val drawable = ScalePictureDrawable(picture)

        this.drawable = drawable
        this.itemWidth = shapeWidth.toFloat()
        this.itemHeight = shapeHeight.toFloat()
    }

}