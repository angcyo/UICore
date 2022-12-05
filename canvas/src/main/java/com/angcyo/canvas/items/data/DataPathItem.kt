package com.angcyo.canvas.items.data

import android.graphics.Color
import android.graphics.Path
import android.os.Debug
import com.angcyo.canvas.LinePath
import com.angcyo.canvas.data.CanvasProjectItemBean
import com.angcyo.canvas.data.toPaintStyle
import com.angcyo.library.L
import com.angcyo.library.component.pool.acquireTempMatrix
import com.angcyo.library.component.pool.acquireTempRectF
import com.angcyo.library.component.pool.release
import com.angcyo.library.ex.computeBounds
import com.angcyo.library.ex.toColor

/**
 * 矢量数据item
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/09/23
 */
open class DataPathItem(bean: CanvasProjectItemBean) : DataItem(bean) {

    //region ---属性---

    /**数据路径列表, 最原始的路径数据*/
    val dataPathList = mutableListOf<Path>()

    /**用来绘制的路径列表, 通常是[dataPathList]的缩放后的数据, 但是不包括旋转*/
    val drawPathList = mutableListOf<Path>()

    //endregion ---属性---

    //region ---方法---

    /**更新画笔属性*/
    fun updatePaint() {
        itemPaint.let {
            it.style = dataBean.paintStyle.toPaintStyle()
            //颜色
            if (Debug.isDebuggerConnected()) {
                it.color = Color.RED
            } else {
                it.color = dataBean.fill?.toColor() ?: Color.BLACK
            }
        }
    }

    /**清空数据*/
    fun clearPathList() {
        dataPathList.clear()
        drawPathList.clear()
    }

    /**添加一个数据路径, 同时添加一个可以绘制的路径, 返回可绘制路径*/
    fun addDataPath(path: Path): Path? {
        return addDataPath(listOf(path)).lastOrNull()
    }

    /**添加一组数据路径, 同时添加一组可以绘制的路径, 返回可绘制路径*/
    fun addDataPath(pathList: List<Path>): List<Path> {
        //需要缩放到的目标宽高
        val renderBounds = acquireTempRectF()
        dataBean.updateToRenderBounds(renderBounds)
        val targetWidth = renderBounds.width()
        val targetHeight = renderBounds.height()

        //path实际的宽高
        val pathBounds = acquireTempRectF()
        pathList.computeBounds(pathBounds, true)
        val pathWidth = pathBounds.width()
        val pathHeight = pathBounds.height()

        //计算出缩放的矩阵参数
        val matrix = acquireTempMatrix()
        matrix.reset()
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
                L.w("无效的宽度, 忽略path...")
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