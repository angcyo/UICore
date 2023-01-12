package com.angcyo.canvas.graphics

import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.PathEffect
import android.graphics.drawable.Drawable
import com.angcyo.canvas.LinePath
import com.angcyo.canvas.core.ICanvasView
import com.angcyo.canvas.data.CanvasProjectItemBean
import com.angcyo.canvas.items.data.DataItem
import com.angcyo.canvas.items.data.DataPathItem
import com.angcyo.canvas.utils.CanvasConstant
import com.angcyo.canvas.utils.isLineShape
import com.angcyo.library.annotation.Pixel
import com.angcyo.library.component.ScalePictureDrawable
import com.angcyo.library.component.pool.acquireTempRectF
import com.angcyo.library.component.pool.release
import com.angcyo.library.ex.ceil
import com.angcyo.library.ex.computePathBounds
import com.angcyo.library.ex.withPicture
import com.angcyo.library.unit.IValueUnit.Companion.MM_UNIT
import com.angcyo.library.unit.toMm
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

    override fun parse(bean: CanvasProjectItemBean, canvasView: ICanvasView?): DataItem? {
        if (bean.mtype == CanvasConstant.DATA_TYPE_SINGLE_WORD /*单线字*/ ||
            bean.mtype == CanvasConstant.DATA_TYPE_PEN /*钢笔*/ ||
            (bean.mtype == CanvasConstant.DATA_TYPE_SVG && bean.data.isNullOrEmpty())/*svg*/
        ) {
            //
            val data = bean.path
            if (!data.isNullOrEmpty()) {
                return parsePathItem(bean, data, canvasView)
            }
        }
        return super.parse(bean, canvasView)
    }

    /**svg 纯路径数据, MXXX LXXX*/
    fun parsePathItem(
        bean: CanvasProjectItemBean,
        data: String,
        canvasView: ICanvasView?
    ): DataPathItem? {
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
        if (bean._width == 0f) {
            bean.width = pathBounds.width().toMm()
        }
        if (bean._height == 0f) {
            bean.height = pathBounds.height().toMm()
        }
        //
        pathBounds.release()

        //
        item.addDataPath(path.flipEngravePath(bean))
        createPathDrawable(item, canvasView) ?: return null

        initDataModeWithPaintStyle(bean, item.itemPaint)

        return item
    }

    /**检查[Path]是否需要fill*/
    fun checkPathGCodeFill(item: DataPathItem): Boolean {
        val paint = item.itemPaint
        val bean = item.dataBean
        if (IGraphicsParser.isNeedGCodeFill(bean)) {
            //需要填充的path
            if (item.pathFillGCodePath == null) {
                //生成新的路径算法
                item.pathFillToGCode(item.dataPathList, paint)?.let {
                    item.pathFillGCodePath = it
                }
            }
            item.pathFillGCodePath?.let {
                item.clearPathList()
                item.addDataPath(it)//替换path
                //item.dataPathList.resetAll(dataPathList) //need?
                return true
            }
        }
        return false
    }

    /**同时创建
     * [com.angcyo.canvas.items.data.DataItem.dataDrawable]
     * [com.angcyo.canvas.items.data.DataItem.renderDrawable]
     * */
    open fun createPathDrawable(item: DataPathItem, canvasView: ICanvasView?): Drawable? {
        val pathFill = checkPathGCodeFill(item)//fill
        val paint = item.itemPaint
        item.drawStrokeWidth = paint.strokeWidth
        if (canvasView != null && (paint.style == Paint.Style.STROKE || item.isLineShape() || pathFill)) {
            val scaleX = canvasView.getCanvasViewBox().getScaleX()//抵消坐标系的缩放
            val newPaint = Paint(paint)
            if (pathFill) {
                //强制使用描边
                newPaint.style = Paint.Style.STROKE
            }
            newPaint.strokeWidth = paint.strokeWidth / scaleX
            item.drawStrokeWidth = newPaint.strokeWidth//this

            item.dataDrawable = createPathDrawable(item, paint)
            item.renderDrawable = createPathDrawable(item, newPaint)
        } else {
            item.dataDrawable = null
            item.renderDrawable = createPathDrawable(item, paint)
        }
        return item.renderDrawable
    }

    /**创建绘制矢量的[Drawable] */
    open fun createPathDrawable(item: DataPathItem, paint: Paint): Drawable? {
        val drawPathList = item.drawPathList
        if (drawPathList.isEmpty()) {
            return null
        }
        val pathBounds = acquireTempRectF()
        drawPathList.computePathBounds(pathBounds)

        //绘制缩放后的path, 至少需要1像素
        val shapeWidth = max(MIN_PATH_SIZE, pathBounds.width()).ceil().toInt()
        val shapeHeight = max(MIN_PATH_SIZE, pathBounds.height()).ceil().toInt()

        val picture = withPicture(shapeWidth, shapeHeight) {
            val strokeWidth = paint.strokeWidth
            val lineShape = item.isLineShape()

            //偏移到路径开始的位置
            val dx = if (lineShape) -pathBounds.left else strokeWidth / 2 - pathBounds.left
            val dy = strokeWidth / 2 - pathBounds.top

            translate(dx, dy)

            //缩放到目标大小
            /*val drawWidth = shapeWidth * 1f - if (lineShape) 0f else strokeWidth
            val drawHeight = shapeHeight * 1f - strokeWidth
            val scaleX = drawWidth / shapeWidth
            val scaleY = if (lineShape) {
                1f
            } else {
                drawHeight / shapeHeight
            }
            scale(scaleX, scaleY, shapeWidth / 2f, shapeHeight / 2f)*/

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
                } else {
                    drawPath(path, paint)
                }
            }
        }
        pathBounds.release()

        //draw
        val drawable = ScalePictureDrawable(picture)
        return drawable
    }

    /**创建一个虚线效果*/
    fun createDashPathEffect(itemDataBean: CanvasProjectItemBean): PathEffect {
        val dashWidth = MM_UNIT.convertValueToPixel(itemDataBean.dashWidth)
        val dashGap = MM_UNIT.convertValueToPixel(itemDataBean.dashGap)
        return DashPathEffect(floatArrayOf(dashWidth, dashGap), 0f)
    }
}