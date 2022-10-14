package com.angcyo.canvas.graphics

import android.graphics.Paint
import android.graphics.drawable.Drawable
import com.angcyo.canvas.LinePath
import com.angcyo.canvas.data.ItemDataBean
import com.angcyo.canvas.data.toMm
import com.angcyo.canvas.items.data.DataItem
import com.angcyo.canvas.items.data.DataPathItem
import com.angcyo.canvas.utils.CanvasConstant
import com.angcyo.library.annotation.Pixel
import com.angcyo.library.component.ScalePictureDrawable
import com.angcyo.library.component.pool.acquireTempRectF
import com.angcyo.library.component.pool.release
import com.angcyo.library.ex.computeBounds
import com.angcyo.library.ex.computePathBounds
import com.angcyo.library.ex.withPicture
import com.pixplicity.sharp.Sharp
import kotlin.math.max

/**
 * 矢量解析器
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/09/23
 */
open class PathGraphicsParser : IGraphicsParser {

    companion object {

        /**最小的绘制大小*/
        @Pixel
        const val MIN_PATH_SIZE = 1f
    }

    override fun parse(bean: ItemDataBean): DataItem? {
        if (bean.mtype == CanvasConstant.DATA_TYPE_SINGLE_WORD /*单线字*/ ||
            bean.mtype == CanvasConstant.DATA_TYPE_PEN /*钢笔*/ ||
            (bean.mtype == CanvasConstant.DATA_TYPE_SVG && bean.data.isNullOrEmpty())/*svg*/
        ) {
            //
            val data = bean.path
            if (!data.isNullOrEmpty()) {
                /*if (data.startsWith("[")) {
                    //svg数组
                } else {
                    //svg对象
                }*/

                val item = DataPathItem(bean)
                item.updatePaint()
                val path = Sharp.loadPath(data)

                //
                val pathBounds = acquireTempRectF()
                path.computePathBounds(pathBounds)
                if (bean.width == 0f) {
                    bean.width = pathBounds.width().toMm()
                }
                if (bean.height == 0f) {
                    bean.height = pathBounds.height().toMm()
                }
                //
                pathBounds.release()

                //
                item.addDataPath(path)
                item.drawable = createPathDrawable(item)

                initDataMode(bean, item.paint)

                return item
            }
        }
        return super.parse(bean)
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