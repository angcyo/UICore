package com.angcyo.canvas.items.data

import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import com.angcyo.canvas.LinePath
import com.angcyo.canvas.Reason
import com.angcyo.canvas.data.ItemDataBean
import com.angcyo.canvas.data.toPaintStyle
import com.angcyo.library.annotation.Implementation
import com.angcyo.library.component.pool.acquireTempMatrix
import com.angcyo.library.component.pool.acquireTempRectF
import com.angcyo.library.component.pool.release
import com.angcyo.library.ex.computeBounds
import com.angcyo.library.ex.density
import com.angcyo.library.ex.have
import com.angcyo.library.ex.toColor

/**
 * 矢量数据item
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/09/23
 */
class DataPathItem(bean: ItemDataBean) : DataItem(bean) {

    //region ---属性---

    /**画笔*/
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 1f
        style = Paint.Style.FILL
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    /**线段描边时, 用虚线绘制.
     * 因为要数据存储, 所以这个功能待定
     * */
    @Implementation
    val lineStrokeEffect = DashPathEffect(floatArrayOf(2 * density, 3 * density), 0f)

    /**数据路径列表, 最原始的路径数据*/
    val dataPathList = mutableListOf<Path>()

    /**用来绘制的路径列表, 通常是[dataPathList]的缩放后的数据, 但是不包括旋转*/
    val drawPathList = mutableListOf<Path>()

    //endregion ---属性---

    //region ---方法---

    override fun needUpdateOfBoundsChanged(reason: Reason): Boolean {
        return reason.reason == Reason.REASON_USER && reason.flag.have(Reason.REASON_FLAG_BOUNDS)
    }

    /**更新画笔属性*/
    fun updatePaint() {
        paint.let {
            it.style = dataBean.paintStyle.toPaintStyle()
            //颜色
            it.color = dataBean.fill?.toColor() ?: Color.BLACK
        }
    }

    /**清空数据*/
    fun clearPathList() {
        dataPathList.clear()
        drawPathList.clear()
    }

    /**添加一个数据路径, 同时添加一个可以绘制的路径, 返回可绘制路径*/
    fun addDataPath(path: Path): Path {
        return addDataPathList(listOf(path)).last()
    }

    /**添加一组数据路径, 同时添加一组可以绘制的路径, 返回可绘制路径*/
    fun addDataPathList(pathList: List<Path>): List<Path> {
        //需要缩放到的目标宽高
        val renderBounds = acquireTempRectF()
        dataBean.setRenderBounds(renderBounds)
        val targetWidth = renderBounds.width()
        val targetHeight = renderBounds.height()

        //path实际的宽高
        val pathBounds = acquireTempRectF()
        pathList.computeBounds(pathBounds, true)
        val pathWidth = pathBounds.width()
        val pathHeight = pathBounds.height()

        //计算出缩放的矩阵参数
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

        //开始映射缩放后的路径
        val newPathList = mutableListOf<Path>()
        pathList.forEach {
            it.computeBounds(pathBounds, true)
            if (pathBounds.width() > 0) {
                //宽度必须大于0
                val newPath = if (pathBounds.height() == 0f) {
                    //识别成线段
                    LinePath()
                } else {
                    Path()
                }
                it.transform(matrix, newPath)
                newPathList.add(newPath)
            } else {
                //没有宽度的路径直接忽略
            }
        }

        //add
        dataPathList.addAll(pathList)//原始的数据
        drawPathList.addAll(newPathList)//缩放的数据

        //release
        renderBounds.release()
        pathBounds.release()
        matrix.release()
        return newPathList
    }

    //endregion ---方法---
}