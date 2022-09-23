package com.angcyo.canvas.graphics

import android.graphics.Paint
import android.graphics.drawable.Drawable
import com.angcyo.canvas.LinePath
import com.angcyo.canvas.items.data.DataPathItem
import com.angcyo.library.annotation.Pixel
import com.angcyo.library.component.ScalePictureDrawable
import com.angcyo.library.component.pool.acquireTempRectF
import com.angcyo.library.component.pool.release
import com.angcyo.library.ex.computeBounds
import com.angcyo.library.ex.withPicture
import kotlin.math.max

/**
 * 矢量解析器
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/09/23
 */
abstract class PathGraphicsParser : IGraphicsParser {

    companion object {

        /**最小的绘制大小*/
        @Pixel
        const val MIN_PATH_SIZE = 1f
    }

    /**创建绘制矢量的[Drawable] */
    open fun createPathDrawable(item: DataPathItem): Drawable {
        val paint = item.paint
        val pathList = item.drawPathList

        val pathBounds = acquireTempRectF()
        pathList.computeBounds(pathBounds, true)

        //绘制缩放后的path, 至少需要1像素
        val shapeWidth = max(MIN_PATH_SIZE, pathBounds.width()).toInt()
        val shapeHeight = max(MIN_PATH_SIZE, pathBounds.height()).toInt()

        val picture = withPicture(shapeWidth, shapeHeight) {
            val strokeWidth = paint.strokeWidth

            //偏移到路径开始的位置
            val dx = -strokeWidth / 2 - pathBounds.left
            val dy = -strokeWidth / 2 - pathBounds.top

            translate(dx, dy)

            //缩放边框, 以便于不会被Bounds裁剪
            val drawWidth = shapeWidth - strokeWidth * 2
            val drawHeight = shapeHeight - strokeWidth * 2
            val scaleX = drawWidth / shapeWidth
            val scaleY = drawHeight / shapeHeight
            scale(scaleX, scaleY, shapeWidth / 2f, shapeHeight / 2f)

            pathList.forEach { path ->
                //线段的描边用虚线处理处理
                if (path is LinePath) {
                    val linePaint = Paint(paint)
                    linePaint.style = Paint.Style.STROKE //线只能使用此模式¬
                    if (paint.style == Paint.Style.STROKE) {
                        linePaint.pathEffect = item.lineStrokeEffect //虚线
                    } else {
                        linePaint.pathEffect = null //实线
                    }
                    drawPath(path, linePaint)
                } else {
                    drawPath(path, paint)
                }
            }
        }

        //draw
        val drawable = ScalePictureDrawable(picture)
        pathBounds.release()
        return drawable
    }

}