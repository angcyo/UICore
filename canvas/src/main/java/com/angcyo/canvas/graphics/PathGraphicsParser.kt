package com.angcyo.canvas.graphics

import android.graphics.*
import android.graphics.drawable.Drawable
import com.angcyo.canvas.LinePath
import com.angcyo.canvas.data.CanvasProjectItemBean
import com.angcyo.canvas.data.CanvasProjectItemBean.Companion.MM_UNIT
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
        const val MIN_PATH_SIZE = 1.0f
    }

    override fun parse(bean: CanvasProjectItemBean): DataItem? {
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
                item.drawable = createPathDrawable(item) ?: return null

                initDataMode(bean, item.paint)

                return item
            }
        }
        return super.parse(bean)
    }

    /**创建绘制矢量的[Drawable] */
    open fun createPathDrawable(item: DataPathItem): Drawable? {
        val paint = item.paint
        val drawPathList = item.drawPathList
        if (drawPathList.isEmpty()) {
            return null
        }

        val pathBounds = acquireTempRectF()
        drawPathList.computeBounds(pathBounds, true)

        //绘制缩放后的path, 至少需要1像素
        val shapeWidth = max(MIN_PATH_SIZE, pathBounds.width()).toInt()
        val shapeHeight = max(MIN_PATH_SIZE, pathBounds.height()).toInt()

        var cacheBitmap: Bitmap? = null
        var cacheCanvas: Canvas? = null
        if (item.dataBean._enableCacheBitmap == true) {
            cacheBitmap = Bitmap.createBitmap(shapeWidth, shapeHeight, Bitmap.Config.ARGB_8888)
            cacheCanvas = Canvas(cacheBitmap)
        }
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

            drawPathList.forEach { path ->
                //线段的描边用虚线处理处理
                if (path is LinePath) {
                    val linePaint = Paint(paint)
                    linePaint.style = Paint.Style.STROKE //线只能使用此模式¬
                    if (paint.style == Paint.Style.STROKE) {
                        linePaint.pathEffect = createDashPathEffect(item.dataBean) //虚线
                    } else {
                        linePaint.pathEffect = null //实线
                    }
                    drawPath(path, linePaint)
                    cacheCanvas?.drawPath(path, linePaint)
                } else {
                    drawPath(path, paint)
                    cacheCanvas?.drawPath(path, paint)
                }
            }
        }

        //cache
        item._cacheBitmap = cacheBitmap

        //draw
        val drawable = ScalePictureDrawable(picture)
        pathBounds.release()
        return drawable
    }

    /**创建一个虚线效果*/
    fun createDashPathEffect(itemDataBean: CanvasProjectItemBean): PathEffect {
        val dashWidth = MM_UNIT.convertValueToPixel(itemDataBean.dashWidth)
        val dashGap = MM_UNIT.convertValueToPixel(itemDataBean.dashGap)
        return DashPathEffect(floatArrayOf(dashWidth, dashGap), 0f)
    }
}