package com.angcyo.canvas.items

import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.Drawable
import com.angcyo.canvas.LinePath
import com.angcyo.library.component.ScalePictureDrawable
import com.angcyo.library.component.pool.acquireTempMatrix
import com.angcyo.library.component.pool.acquireTempRectF
import com.angcyo.library.component.pool.release
import com.angcyo.library.ex.computeBounds
import com.angcyo.library.ex.density
import com.angcyo.library.ex.withPicture
import kotlin.math.max

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/11
 */
open class DrawableItem : BaseItem() {

    /**可绘制的对象*/
    var drawable: Drawable? = null

    init {
        itemLayerName = "Drawable"
    }

    /**当[paint]更新时触发*/
    override fun updateItem(paint: Paint) {
        updateDrawable(drawable)
    }

    /**更新[drawable]*/
    open fun updateDrawable(drawable: Drawable?) {
        this.drawable = drawable
    }

    /**当[com.angcyo.canvas.items.renderer.DrawableItemRenderer]的bounds宽高改变时,
     * 需要重新更新绘制元素, 比如Path需要更新后再绘制, 防止失真*/
    open fun updateDrawable(paint: Paint, width: Float, height: Float) {

    }

    //region ---operate---

    /**线段描边时, 用虚线绘制*/
    val lineStrokeEffect = DashPathEffect(floatArrayOf(2 * density, 3 * density), 0f)

    fun createPathDrawable(
        path: Path,
        targetWidth: Float,
        targetHeight: Float,
        paint: Paint
    ): Drawable = createPathDrawable(listOf(path), targetWidth, targetHeight, paint)

    /**将[pathList]缩放到指定的宽高[targetWidth] [targetHeight]*/
    fun createPathDrawable(
        pathList: List<Path>,
        targetWidth: Float,
        targetHeight: Float,
        paint: Paint
    ): Drawable {
        val pathBounds = acquireTempRectF()
        pathList.computeBounds(pathBounds, true)
        val pathWidth = pathBounds.width()
        val pathHeight = pathBounds.height()

        //缩放矩阵
        val matrix = acquireTempMatrix()
        val scaleX = if (pathWidth > 1 && targetWidth > 1) {
            targetWidth / pathWidth
        } else {
            1f
        }
        val scaleY = if (pathHeight > 1 && targetHeight > 1) {
            targetHeight / pathHeight
        } else {
            1f
        }
        matrix.setScale(scaleX, scaleY, pathBounds.left, pathBounds.top)

        //缩放后的路径
        val newPathList = mutableListOf<Path>()
        pathList.forEach {
            it.computeBounds(pathBounds, true)
            if (pathBounds.width() > 0) {
                val newPath = if (pathBounds.height() == 0f) {
                    //识别成线段
                    LinePath()
                } else {
                    Path()
                }
                it.transform(matrix, newPath)
                newPathList.add(newPath)
            } else {
                //忽略
            }
        }
        newPathList.computeBounds(pathBounds, true)

        //绘制缩放后的path, 至少需要1像素
        val shapeWidth = max(1f, pathBounds.width()).toInt()
        val shapeHeight = max(1f, pathBounds.height()).toInt()

        val picture = withPicture(shapeWidth, shapeHeight) {
            val strokeWidth = paint.strokeWidth

            //偏移到路径开始的位置
            val dx = -strokeWidth / 2 - pathBounds.left
            val dy = -strokeWidth / 2 - pathBounds.top

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

            newPathList.forEach { path ->
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
        }

        //draw
        val drawable = ScalePictureDrawable(picture)

        pathBounds.release()
        matrix.release()

        return drawable
    }

    //endregion ---operate---

}